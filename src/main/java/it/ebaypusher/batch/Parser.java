package it.ebaypusher.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
import it.ebaypusher.utility.ParserConfiguration;
import it.ebaypusher.utility.Utility;

public class Parser implements Runnable {

	private Log logger = LogFactory.getLog(Puller.class);

	private Dao dao;

	public Parser(Dao dao) {
		this.dao = dao;
	}

	@Override
	public void run() {

		List<String> enabled = enabled();
		
		if ( enabled.size() == 0 ) {
			return;
		}
		
		boolean started = false;
						
		for ( SnzhElaborazioniebay elaborazione : dao.findAll()) {
			
			dao.detach(elaborazione);

			if (!enabledDownload(elaborazione)) {
				continue;
			}
			
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

			if ( ! started ) {
				logger.info("Parser begin to work...");
				started = true;
			}

			if ( elaborazione.getPathFileEsito() == null ) {
				logger.debug("Esito scartato perchè mancante. Elaborazione: " + elaborazione.getIdElaborazione());
				continue;
			}

			logger.info("Parsing esito in corso file: " + Utility.getFileLabel(elaborazione.getPathFileEsito()));
			
			File fileEsito = new File(elaborazione.getPathFileEsito());
			if ( ! fileEsito.exists() || !fileEsito.isFile()) {
				logger.error("Esito scartato perchè inesistente o non è un file: " + fileEsito);
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
			finally {

				logger.info("Parsing esito terminato file: " + Utility.getFileLabel(elaborazione.getPathFileEsito()));

			}

		}

		if ( started ) {
			logger.info("Parser terminated to work");
		}
		
	}
	
	private List<String> enabled() {
		
		List<String> enabled = new ArrayList<String>();
		
		if (ParserConfiguration.instance().isParseAdd()) {
			enabled.add("AddFixedPriceItem");
		}
		
		if (ParserConfiguration.instance().isParseDel()) {
			enabled.add("EndFixedPriceItem");
		}

		if (ParserConfiguration.instance().isParseMod()) {
			enabled.add("ReviseFixedPriceItem");
		}

		if (ParserConfiguration.instance().isParseRev()) {
			enabled.add("RelistFixedPriceItem");
		}

		return enabled;
		
	}
	
	private boolean enabledDownload(SnzhElaborazioniebay elaborazione) {
		
		if ("AddFixedPriceItem".equals(elaborazione.getJobType())) {
			return ParserConfiguration.instance().isParseAdd();
		}
				
		if ("EndFixedPriceItem".equals(elaborazione.getJobType())) {
			return ParserConfiguration.instance().isParseDel();
		}

		if ("ReviseFixedPriceItem".equals(elaborazione.getJobType())) {
			return ParserConfiguration.instance().isParseMod();
		}

		if ("RelistFixedPriceItem".equals(elaborazione.getJobType())) {
			return ParserConfiguration.instance().isParseRev();
		}
		
		logger.error("Tipo elaborazione non ammessa per il parsing: " + elaborazione.getIdElaborazione() + ": " + elaborazione.getJobType());
		return false;

	}

}
