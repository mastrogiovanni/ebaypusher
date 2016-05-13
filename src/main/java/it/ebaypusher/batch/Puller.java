package it.ebaypusher.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.JobStatus;

import it.ebaypusher.constants.Stato;
import it.ebaypusher.controller.EbayConnectorException;
import it.ebaypusher.controller.EbayController;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.SnzhElaborazioniebay;
import it.ebaypusher.utility.Utility;

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

		while ( !Thread.interrupted() ) {

			logger.info("Puller round");

			for ( SnzhElaborazioniebay elaborazione : dao.findAll()) {

				try {

					JobStatus status = JobStatus.valueOf(elaborazione.getJobStatus());
					switch (status) {

					// Batch ebay creato
					case CREATED:

						// Batch ebay creato ma file non inviato
						if (Stato.IN_CORSO_DI_INVIO.toString().equals(elaborazione.getFaseJob())) {

							// Il file deve essere trasferito su ebay
							connector.upload(elaborazione);
							
							// Sposta il file da OUTPUT a SENT
							if (!Utility.getInputFile(elaborazione).renameTo(Utility.getSentFile(elaborazione))) {
								logger.error("Non posso spostare il file nella cartella SENT: " + elaborazione.getFilename());
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
					case IN_PROCESS:
						
						// Richiede aggiornamento di stato a Ebay
						connector.updateProgressAndStatus(elaborazione);

						// Se il batch non è più schedulato o in progress
						// salva la response di ebay
						if (!JobStatus.IN_PROCESS.toString().equals(elaborazione.getJobStatus()) &&
								!JobStatus.SCHEDULED.toString().equals(elaborazione.getJobStatus())) {
							
							connector.saveResponseFile(elaborazione);
							
						}

						dao.update(elaborazione);
						break;

					case ABORTED:
					case COMPLETED:
					case FAILED:
						
					}

				} catch (EbayConnectorException e) {
					logger.error("Errore nella chiamata di un servizio ebay", e);
				}

			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				logger.error("Batch stoppato");
				break;
			}

		}

	}	


}
