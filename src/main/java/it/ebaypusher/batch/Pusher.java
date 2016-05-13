package it.ebaypusher.batch;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.ebaypusher.controller.EbayController;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.ElaborazioniEbay;
import it.ebaypusher.utility.Configurazione;
import it.ebaypusher.utility.Utility;

/**
 * Questo processo si occupa di sottomettere a ebay per la pubblicazione
 * i file contenuti nella cartella OUTPUT.
 * 
 * Il processo inserisce un rigo in tabella e sottomette il job a ebay.
 *  
 * @author Michele Mastrogiovanni
 */
public class Pusher implements Runnable {

	private Log logger = LogFactory.getLog(Pusher.class);
	
	private EbayController connector;
	
	private Dao dao;
	
	public Pusher(Dao dao, EbayController connector) {
		this.connector = connector;
		this.dao = dao;
	}
	
	@Override
	public void run() {
						
		File root = new File(Configurazione.getText(Configurazione.OUTGOING_DIR));
		logger.info("Scanning della cartella di output: " + root.getAbsolutePath());
		
		final String extension = getExtension();
		logger.info("Considero i file con estensione: " + extension + " (case sensitive)");
		
		while ( ! Thread.interrupted() ) {
			
			logger.info("Pusher round");
			
			for ( File file : root.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					
					if (!pathname.isFile()) {
						return false;
					}

					return ( pathname.getName().endsWith(extension));
					
				}
				
			})) {
				
				try {
				
					// Aggiunge il file da elaborare al database (evita i duplicati)
					ElaborazioniEbay elaborazione = dao.create(file.getName());
					if ( elaborazione == null ) {
						logger.info("File già presente: " + file.getName());
						continue;
					}

					// Crea un batch di inserimento ebay
					connector.create(elaborazione);
					
					// Salva Id Ebay per l'elaborazione
					dao.update(elaborazione);

					// Invia il file ad ebay per il processamento
					connector.upload(elaborazione);

					// Avvia l'elaborazione del batch
					connector.start(elaborazione);
					
					// Salva Stato INVIATO_EBAY
					dao.update(elaborazione);

					// Sposta il file da OUTPUT a SENT
					File sentFile = Utility.getSentFile(elaborazione);
					if (!file.renameTo(sentFile)) {
						logger.error("Non posso spostare il file nella cartella SENT");
					}
					
				}
				catch (Throwable t) {
					
					logger.error("Errore di upload batch del file: " + file, t);
					
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

	private String getExtension() {
		String extension = Configurazione.getText(Configurazione.OUTGOING_FILE_EXTENSION);
		if ( extension == null ) {
			return "xml";
		}
		return extension;
	}

}
