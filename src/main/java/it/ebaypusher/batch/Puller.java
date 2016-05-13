package it.ebaypusher.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.ebaypusher.constants.Stato;
import it.ebaypusher.controller.EbayConnectorException;
import it.ebaypusher.controller.EbayController;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.ElaborazioniEbay;
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

			for ( ElaborazioniEbay elaborazione : dao.findAll()) {
				
				if ( elaborazione.getStato() == Stato.INVIATO_EBAY ) {
					
					try {

						// Richiede aggiornamento di stato a Ebay
						connector.updateStatus(elaborazione);
						
						// Aggiorna stato sul sistema
						dao.update(elaborazione);
						
					} catch (EbayConnectorException e) {
						
						logger.error("Errore nella verifica dello stato dell'elaborazione: " + elaborazione, e);
						
					}
				
					continue;
					
				}
				
				if ( elaborazione.getStato() == Stato.TERMINATO_CON_ERRORE || elaborazione.getStato() == Stato.TERMINATO_CON_SUCCESSO ) {
					
					Utility.getReportFile(elaborazione);
					
					try {
						
						connector.saveResponseFile(elaborazione);
						
					} catch (EbayConnectorException e) {
						
						logger.error("Errore nel salvataggio della risposta di Ebay per l'elaborazione: " + elaborazione, e);
						
					}

					continue;

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
