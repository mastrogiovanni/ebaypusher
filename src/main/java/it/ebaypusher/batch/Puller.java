package it.ebaypusher.batch;

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
	
	private static final String CAMBIO_STATO_MESSAGE = "Elaborazione: %s (%s) %s %s%% -> %s %s%%";
	private static final String STESSO_STATO_MESSAGE = "Elaborazione: %s (%s) %s %s%%";
	
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

		// Numero massimo di azioni "a vuoto" che il puller può fare
		int retry = 0;
		
		logger.info("Puller begin to work...");

		// Stato del job ebay
		JobProfile jobProfile = null;
		
		while ( !Thread.interrupted() && ! shouldInterrupt && retry <= Configurazione.getIntValue("retry.puller", 15)) {

			shouldInterrupt = true;

			// Cerca di aggiornare lo stato di ogni elaborazione non terminata
			for ( SnzhElaborazioniebay elaborazione : dao.findAll()) {

				// Stacca l'elaborazione dal persistence context
				dao.detach(elaborazione);

				// Se il processo è terminato non ha senso andare avanti
				if ( Utility.isProcessoTerminated(elaborazione)) {
					continue;
				}

				// Vecchio stato del job
				JobStatus oldStatus = JobStatus.valueOf(elaborazione.getJobStatus());

				try {

					switch (oldStatus) {

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
									logger.info("File di input spostato in: " + Utility.getFileLabel(Utility.getSentFile(elaborazione)));
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
						
						shouldInterrupt = false;

					case IN_PROCESS:
						
						// Chiede lo stato attuale dell'elaborazione
						jobProfile = connector.getJobProfile(elaborazione.getJobId());
						
						// Stato del job non cambiato
						if (!Utility.statusChanged(jobProfile, elaborazione)) {
							
							if (JobStatus.IN_PROCESS.toString().equals(elaborazione.getJobStatus())) {

								logger.info(
										String.format(
												STESSO_STATO_MESSAGE,
												elaborazione.getIdElaborazione(),
												elaborazione.getFilename(),
												elaborazione.getJobStatus(),
												elaborazione.getJobPercCompl()));

							}
							
							break;
						}
						
						logger.info(
								String.format(
										CAMBIO_STATO_MESSAGE,
										elaborazione.getIdElaborazione(),
										elaborazione.getFilename(),
										elaborazione.getJobStatus(),
										elaborazione.getJobPercCompl(),
										jobProfile.getJobStatus().toString(),
										jobProfile.getPercentComplete()));

						// Aggiorna stato
						Utility.updateWith(elaborazione, jobProfile);

						// Verifica di terminazione (eventualmente per scaricare il file di esito)
						if ( Utility.isJobCompleted(elaborazione)) {
							
							if ( Utility.isJobCompledGood(elaborazione)) {
								elaborazione.setFaseJob(Stato.TERMINATO_CON_SUCCESSO.toString());
								elaborazione.setDataElaborazione(new Timestamp(System.currentTimeMillis()));
							}
							else if ( Utility.isJobCompledBad(elaborazione)) {
								if (elaborazione.getNumTentativi() >= Configurazione.getIntValue(Configurazione.NUM_MAX_INVII, 3)) {
									elaborazione.setFaseJob(Stato.SUPERATO_NUMERO_MASSIMO_INVII.toString());
								}
								else {
									elaborazione.setFaseJob(Stato.TERMINATO_CON_ERRORE.toString());
								}
								elaborazione.setDataElaborazione(new Timestamp(System.currentTimeMillis()));
							}
							else {
								throw new EbayConnectorException("Impossibile determinare se il job ebay è terminato o no: cambiato il protocollo ?");
							}

							// C'è un file di esito da scaricare
							if ( jobProfile.getFileReferenceId() != null ) {

								// Salva l'output
								if (!connector.saveResponseFile(elaborazione)) {

									// Non salva lo stato aggiornato finchè non riesce anche a scaricare il file
									break;

								}

							}
							else {
								
								logger.info("Elaborazione " + elaborazione.getIdElaborazione() + ": nessun file di esito da scaricare: termina");
																
							}

						}

						// Aggiorna lo stato dell'elaborazione
						dao.update(elaborazione);

						break;
						
					case COMPLETED:
					case ABORTED:
					case FAILED:
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

}
