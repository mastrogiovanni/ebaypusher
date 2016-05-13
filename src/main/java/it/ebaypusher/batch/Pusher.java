package it.ebaypusher.batch;

import java.io.File;
import java.io.FileFilter;
import java.sql.Date;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.JobStatus;

import it.ebaypusher.constants.Stato;
import it.ebaypusher.controller.EbayController;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.SnzhElaborazioniebay;
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
					
					SnzhElaborazioniebay elaborazione = new SnzhElaborazioniebay();
					elaborazione.setDataInserimento(new Date(System.currentTimeMillis()));
					
					elaborazione.setFilename(file.getName());
					elaborazione.setPathFileInput(file.getAbsolutePath());
					elaborazione.setNumTentativi(0);
					
					// Crea un batch di inserimento ebay
					connector.create(elaborazione);
					
					elaborazione.setJobStatus(JobStatus.CREATED.toString());
					elaborazione.setFaseJob(Stato.IN_CORSO_DI_INVIO.toString());
					elaborazione.setDataInserimento(new Timestamp(System.currentTimeMillis()));
					dao.insert(elaborazione);
					
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
