package it.ebaypusher.batch;

import java.io.File;
import java.sql.Timestamp;

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

		boolean shouldInterrupt = false;

		while ( !Thread.interrupted() && ! shouldInterrupt ) {

			shouldInterrupt = true;

			logger.info("Puller begin to work...");

			for ( SnzhElaborazioniebay elaborazione : dao.findAll()) {

				boolean downloadFile = false;
				
				try {
					
					JobStatus currentStatus = JobStatus.valueOf(elaborazione.getJobStatus());
					JobStatus status = currentStatus;
					switch (currentStatus) {
					case ABORTED:
					case FAILED:
						break;

					case COMPLETED:
						if (elaborazione.getJobPercCompl() == 100.0) {
							break;
						}

					default:
						JobProfile jobProfile = connector.getJobProfile(elaborazione.getJobId());
						status = jobProfile.getJobStatus();
						elaborazione.setJobStatus(status.toString());
						if ( jobProfile.getPercentComplete() != null ) {
							elaborazione.setJobPercCompl((int) Math.round(jobProfile.getPercentComplete()));
							downloadFile = true;
						}
						logger.info("Job " + elaborazione.getJobId() + "(" + elaborazione.getFilename() + ") status: " + status.toString());
					
					}

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
							
							// Sposta il file da OUTPUT a SENT
							if (!Utility.getInputFile(elaborazione).renameTo(Utility.getSentFile(elaborazione))) {
								logger.error("Non posso spostare il file nella cartella SENT: " + elaborazione.getFilename());
							}
							else {
								logger.info("File di input spostato in SENT: " + Utility.getSentFile(elaborazione));
							}

							elaborazione.setPathFileInput(Utility.getSentFile(elaborazione).getAbsolutePath());
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

					// Deve essere avviata la schedulazione
					case SCHEDULED:

						// Questo
						shouldInterrupt = false;

					case IN_PROCESS:
					case COMPLETED:
						
						if ( status != JobStatus.COMPLETED || !downloadFile ) {
							dao.update(elaborazione);
							break;
						}
						
						// Richiede aggiornamento di stato a Ebay
						connector.updateProgressAndStatus(elaborazione);

						if (JobStatus.COMPLETED.toString().equals(elaborazione.getJobStatus()) 
								&& elaborazione.getJobPercCompl() == 100.0) {
							
							if ( connector.saveResponseFile(elaborazione)) {

								dao.update(elaborazione);

							}
							
						}
						else {

							dao.update(elaborazione);

						}
						break;
						
					case ABORTED:
					case FAILED:

						if (elaborazione.getNumTentativi() >= Configurazione.getIntValue(Configurazione.NUM_MAX_INVII, 3)) {

							new File(elaborazione.getPathFileInput()).renameTo(Utility.getErrorFile(elaborazione));
							
							elaborazione.setPathFileEsito(Utility.getErrorFile(elaborazione).getAbsolutePath());
							elaborazione.setErroreJob(status.toString());
							elaborazione.setFaseJob(Stato.SUPERATO_NUMERO_MASSIMO_INVII.toString());
							dao.update(elaborazione);
							continue;
						}

						// Crea un batch di inserimento ebay
						connector.create(elaborazione);
						
						elaborazione.setNumTentativi(elaborazione.getNumTentativi() + 1);
						elaborazione.setJobStatus(JobStatus.CREATED.toString());
						elaborazione.setFaseJob(Stato.IN_CORSO_DI_INVIO.toString());
						elaborazione.setDataInserimento(new Timestamp(System.currentTimeMillis()));
						elaborazione.setDataElaborazione(null);
						dao.update(elaborazione);
						
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

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				logger.error("Batch stoppato");
				break;
			}

		}

		logger.info("Puller terminated to work");

	}	


}
