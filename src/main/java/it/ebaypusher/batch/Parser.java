package it.ebaypusher.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.JobStatus;

import it.ebaypusher.constants.Stato;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.SnzhElaborazioniebay;
import it.ebaypusher.dao.SnzhEsitiebay;
import it.ebaypusher.utility.EsitoParser;

public class Parser implements Runnable {

	private Log logger = LogFactory.getLog(Puller.class);

	private Dao dao;

	public Parser(Dao dao) {
		this.dao = dao;
	}

	@Override
	public void run() {
		
		logger.info("Parser begin to work...");
		
		for ( SnzhElaborazioniebay elaborazione : dao.findAll()) {
			
			JobStatus currentStatus = JobStatus.valueOf(elaborazione.getJobStatus());
			
			if ( !EnumSet.of(JobStatus.ABORTED, JobStatus.COMPLETED, JobStatus.FAILED).contains(currentStatus)) {
				continue;
			}

			if (!Stato.SUPERATO_NUMERO_MASSIMO_INVII.toString().equals(elaborazione.getFaseJob()) &&
					!Stato.TERMINATO_CON_SUCCESSO.toString().equals(elaborazione.getFaseJob())) {
				continue;
			}
			
			if (elaborazione.isEsitoParsed()) {
				continue;
			}
			
			if ( elaborazione.getPathFileEsito() == null ) {
				logger.debug("Esito scartato perchè mancante. Elaborazione: " + elaborazione.getIdElaborazione());
				continue;
			}
			
			File fileEsito = new File(elaborazione.getPathFileEsito());
			if ( ! fileEsito.exists() || !fileEsito.isFile()) {
				logger.debug("Esito scartato perchè inesistente o non è un file: " + fileEsito);
				continue;
			}

			try {
				
				List<SnzhEsitiebay> esiti = EsitoParser.parse(new FileInputStream(fileEsito));
				if ( esiti == null || esiti.size() == 0 ) {
					dao.updateParsed(elaborazione, true);
					continue;
				}
				
				for ( SnzhEsitiebay esito : esiti ) {
					esito.setIdElaborazione(elaborazione.getIdElaborazione());
				}
				
				dao.insert(esiti);
				dao.updateParsed(elaborazione, true);
				
			} catch (FileNotFoundException e) {
				// Swallow
			}

		}

		logger.info("Parser terminated to work");

	}	

}
