package it.ebaypusher.batch;

import java.io.File;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.JobProfile;
import com.ebay.marketplace.services.JobStatus;

import it.ebaypusher.constants.Stato;
import it.ebaypusher.controller.EbayConnectorException;
import it.ebaypusher.controller.EbayController;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.SnzhElaborazioniebay;
import it.ebaypusher.utility.Configurazione;
import it.ebaypusher.utility.Utility;

/**
 * Questo batch si occupa di portare avanti il processo di upload
 * di un file XML presso il connettore di EBay e di mantenere
 * aggiornato il record sul database che rappresenta tale upload.
 *
 * Il processo continua a girare finchè non ci sono più job
 * che debbano inviare il file e avviare il processing.
 */
public class Puller implements Runnable { 

	private Log logger = LogFactory.getLog(Puller.class);

	private EbayController connector;

	private Dao dao;

	public Puller(Dao dao, EbayController connector) {
		this.connector = connector;
		this.dao = dao;
	}

	@Override
	public void run() {

		// Indica che il ciclo deve essere interrotto.
		// Il ciclo è non viene interrotto se ci sono job da avviare 
		// o file da inviare e EBay
		boolean shouldInterrupt = false;
		
		int retry = 0;

		logger.info("Puller begin to work...");
		
		while ( !Thread.interrupted() && ! shouldInterrupt && retry <= Configurazione.getIntValue("retry.puller", 15)) {

			shouldInterrupt = true;

			for ( SnzhElaborazioniebay elaborazione : dao.findAll()) {

				dao.detach(elaborazione);

				try {
					
					JobStatus oldStatus = JobStatus.valueOf(elaborazione.getJobStatus());
					
					// Aggiorna lo stato attuale del job
					JobStatus status = updateElaborazioneStatus(elaborazione);
					
					switch (status) {

					// Batch ebay creato
					case CREATED:

						// Questo
						shouldInterrupt = false;
						
						// Batch ebay creato ma file non inviato
						if (Stato.IN_CORSO_DI_INVIO.toString().equals(elaborazione.getFaseJob())) {

							logger.debug("Job " + elaborazione.getJobId() + " file da inviare");

							// Il file deve essere trasferito su ebay
							connector.upload(elaborazione);
							
							// Solo la prima volta sposta il file: se è un tentativo di re-invio
							// non è necessario spostarlo
							if ( elaborazione.getNumTentativi() == 0 ) {
								
								// Sposta il file da OUTPUT a SENT
								if (!Utility.getInputFile(elaborazione).renameTo(Utility.getSentFile(elaborazione))) {
									logger.error("Non posso spostare il file nella cartella SENT: " + elaborazione.getFilename());
								}
								else {
									logger.info("File di input spostato in SENT: " + Utility.getSentFile(elaborazione));
								}
								elaborazione.setPathFileInput(Utility.getSentFile(elaborazione).getAbsolutePath());
							}

							elaborazione.setFaseJob(Stato.INVIATO_EBAY.toString());
							dao.update(elaborazione);

						}
						
						// File inviato: schedula l'ingestion
						else if (Stato.INVIATO_EBAY.toString().equals(elaborazione.getFaseJob())) { 
							
							connector.start(elaborazione);
							elaborazione.setJobStatus(JobStatus.SCHEDULED.toString());
							dao.update(elaborazione);
							
						}
						break;

					// Accodato il lavoro ma ancora non avviato
					case SCHEDULED:
						dao.update(elaborazione);
						shouldInterrupt = false;
						break;

					case IN_PROCESS:
						dao.update(elaborazione);
						break;
						
					case COMPLETED:
						
						// Job completed but not 100%
						if ( ! canDownloadFile(elaborazione) ) {
							dao.update(elaborazione);
							break;
						}
						
						// Richiede aggiornamento di stato a Ebay
						connector.updateProgressAndStatus(elaborazione);

						if (JobStatus.COMPLETED.toString().equals(elaborazione.getJobStatus()) && elaborazione.getJobPercCompl() == 100.0) {

							// Salva l'output
							if ( connector.saveResponseFile(elaborazione)) {
								dao.update(elaborazione);
								break;
							}
							
							// Non aggiorna lo stato se non è stato possibile scaricare il file
							break;
						}

						// Aggiorna lo stato dell'elaborazione
						dao.update(elaborazione);

						break;
						
					case ABORTED:
					case FAILED:
						
						// Elaborazione terminata in passato
						if ( oldStatus.equals(status)) {
							if ( Stato.TERMINATO_CON_SUCCESSO.toString().equals(elaborazione.getFaseJob())) {
								break;
							}
							if ( Stato.TERMINATO_CON_ERRORE.toString().equals(elaborazione.getFaseJob())) {
								break;
							}
							if ( Stato.SUPERATO_NUMERO_MASSIMO_INVII.toString().equals(elaborazione.getFaseJob())) {
								break;
							}
						}
												
						// Richiede aggiornamento di stato a Ebay
						connector.updateProgressAndStatus(elaborazione);
						
						try {
							// Tenta il salvataggio della response (se esiste)
							connector.saveResponseFile(elaborazione);
						}
						catch (Throwable t) {
							logger.error("Impossibile salvare l'output per l'elaborazione: " + elaborazione.getIdElaborazione() + ": " + t.getMessage());
						}							
						
						// Aggiorna l'elaborazione
						dao.update(elaborazione);
						
						break;
												
					}

				} catch (EbayConnectorException e) {

					// In caso di errore tipizzato, riporta il solo messaggio
					if ( e.getErrorData() != null ) {
						logger.error("Errore nell chiamata di un servizio ebay: " + e.getMessage());
					}
					// In caso di altro errore non catturato, stampa lo stack trace
					else {
						logger.error("Errore nell chiamata di un servizio ebay", e);
					}
					
					// Aggiorna l'elaborazione salvando l'ultimo messaggio errore
					elaborazione = dao.findById(elaborazione.getIdElaborazione());
					elaborazione.setErroreJob(e.getMessage());
					dao.update(elaborazione);
					
				}

			}
			
			if ( !shouldInterrupt ) {
				retry ++;
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				logger.error("Batch stoppato");
				break;
			}

		}

		logger.info("Puller terminated to work");

	}

	/**
	 * Backup della procedura di download esito per le volte che il download non è andato a buon fine
	 * 
	 * @param elaborazione
	 * @return
	 * @throws EbayConnectorException
	 */
	private boolean canDownloadFile(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {
		
		if (!JobStatus.COMPLETED.toString().equals(elaborazione.getJobStatus()) || elaborazione.getJobPercCompl() != 100.0) {
			return false;
		}

		if ( elaborazione.getPathFileEsito() == null ) {
			return true;
		}
		
		if (!new File(elaborazione.getPathFileEsito()).exists()) {
			return true;
		}

		JobProfile jobProfile = connector.getJobProfile(elaborazione.getJobId());

		return ( jobProfile.getFileReferenceId() != null );

	}

	/**
	 * Aggiorna lo stato attuale del job: non considera job terminati 
	 * a meno che non abbiano una percentuale di completamento
	 * minore di 100%
	 * 
	 * @param elaborazione
	 * @param downloadFile
	 * @return
	 * @throws EbayConnectorException
	 */
	private JobStatus updateElaborazioneStatus(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {
		
		JobStatus currentStatus = JobStatus.valueOf(elaborazione.getJobStatus());

		if ( Utility.isTerminated(elaborazione)) {
			return currentStatus;
		}
		
		// Aggiorna lo stato attuale dell'elaborazione
		JobProfile jobProfile = connector.getJobProfile(elaborazione.getJobId());
		currentStatus = jobProfile.getJobStatus();
		elaborazione.setJobStatus(currentStatus.toString());
		if ( jobProfile.getPercentComplete() != null ) {
			elaborazione.setJobPercCompl((int) Math.round(jobProfile.getPercentComplete()));
		}
		
		logger.info("Job " + elaborazione.getJobId() + "(" + elaborazione.getFilename() + ") status: " + currentStatus.toString());
		
		return currentStatus;
	}	


}
