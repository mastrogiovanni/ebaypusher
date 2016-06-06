package it.ebaypusher.batch;

import java.io.File;
import java.io.FileFilter;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.JobStatus;

import it.ebaypusher.constants.Stato;
import it.ebaypusher.controller.EbayConnectorException;
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

	private static final String EMPTY_VALUE = "EMPTY";

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
		
		File[] files = root.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {

				if (!pathname.isFile()) {
					return false;
				}

				return ( pathname.getName().endsWith(extension));

			}

		});

		Map<String, Integer> jobs = new TreeMap<String, Integer>() {
			private static final long serialVersionUID = 1L;
			public Integer get(Object key) {
				Integer value = super.get(key);
				if ( value == null ) {
					return 0;
				}
				return value;
			};
		};
		
		for ( SnzhElaborazioniebay elaborazione : dao.findAll()) {
			JobStatus status = JobStatus.valueOf(elaborazione.getJobStatus());
			if (EnumSet.of(JobStatus.CREATED, JobStatus.IN_PROCESS, JobStatus.SCHEDULED).contains(status)) {
				jobs.put(elaborazione.getJobType(), jobs.get(elaborazione.getJobType()) + 1);
			}
		}

		if ( files.length > 0 ) {

			for ( File file : files ) {

				String jobType = null;

				// Cattura il tipo di job
				try {
					
					jobType = connector.getJobTypeFromXML(file);
					
				} catch (Throwable e) {
					
					logger.error("Errore nella sottomissione del file: " + Utility.getFileLabel(file) + ";" + e.getMessage());

					SnzhElaborazioniebay elaborazione = new SnzhElaborazioniebay();
					elaborazione.setDataInserimento(new Date(System.currentTimeMillis()));
					elaborazione.setFilename(file.getName());
					elaborazione.setPathFileInput(file.getAbsolutePath());
					elaborazione.setFileReferenceId(EMPTY_VALUE);
					elaborazione.setJobId(EMPTY_VALUE);
					elaborazione.setJobType(EMPTY_VALUE);
					elaborazione.setErroreJob("Errore di parsing:\n" + Utility.getExceptionText(e));
					elaborazione.setEsitoParsed(true);
					elaborazione.setNumTentativi(Configurazione.getIntValue("num.max.invii", 3));
					elaborazione.setJobStatus(JobStatus.FAILED.toString());
					elaborazione.setFaseJob(Stato.SUPERATO_NUMERO_MASSIMO_INVII.toString());
					elaborazione.setDataInserimento(new Timestamp(System.currentTimeMillis()));
					dao.insert(elaborazione);

					String errorPath = Utility.getErrorFile(elaborazione).getAbsolutePath();
					elaborazione.setPathFileInput(errorPath);
					elaborazione.setPathFileEsito(errorPath);
					dao.update(elaborazione);

					logger.info("Sto per spostare: " + file.getAbsolutePath() + " in " + errorPath);
					// Sposta il file negli errori
					if (!file.renameTo(new File(errorPath)) ) {
						dao.remove(elaborazione);
						logger.error("Impossibile spostare il file malformato (forse problemi di permessi?)");
					}

					continue;
					
				}

				try {

					logger.info("Il tipo di job del file '" + file.getName() + "' è: " + jobType);

					if ( tooManyJobs(jobType, jobs) ) {
						int count = jobs.get(jobType);
						logger.info("Ci sono già " + count + " job di tipo " + jobType + ": rimando sottomissione");
						continue;
					}

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

					jobs.put(elaborazione.getJobType(), jobs.get(elaborazione.getJobType()) + 1);
					logger.info("Job sottomesso con successo: '" + file.getName() + "':" + elaborazione.getIdElaborazione());

				}
				catch (Throwable t) {
					logger.error("Errore nella sottomissione del file: " + Utility.getFileLabel(file) + ";" + t.getMessage());
				}

			}
		}

		// Prova a rinviare alcuni file
		for ( SnzhElaborazioniebay elaborazione : dao.findAll()) {
			
			// Rimuove l'elaborazione dal persistence context
			dao.detach(elaborazione);
			
			if (!Stato.TERMINATO_CON_ERRORE.toString().equals(elaborazione.getFaseJob())) {
				continue;
			}
			
			if (elaborazione.getNumTentativi() >= Configurazione.getIntValue(Configurazione.NUM_MAX_INVII, 3)) {
				elaborazione.setFaseJob(Stato.SUPERATO_NUMERO_MASSIMO_INVII.toString());
				dao.update(elaborazione);
				continue;
			}
			
			if ( tooManyJobs(elaborazione.getJobType(), jobs) ) {
				int count = jobs.get(elaborazione.getJobType());
				logger.debug("Ci sono già " + count + " job di tipo " + elaborazione.getJobType() + ": rimando sottomissione");
				continue;
			}

			try {
				
				logger.debug("Retry of elaborazione: " + elaborazione.getIdElaborazione());
				dao.detach(elaborazione);

				elaborazione.setDataInserimento(new Date(System.currentTimeMillis()));
				elaborazione.setNumTentativi(elaborazione.getNumTentativi() + 1);

				// Crea un batch di inserimento ebay
				connector.create(elaborazione);

				elaborazione.setJobStatus(JobStatus.CREATED.toString());
				elaborazione.setFaseJob(Stato.IN_CORSO_DI_INVIO.toString());
				elaborazione.setDataInserimento(new Timestamp(System.currentTimeMillis()));
				dao.update(elaborazione);

				jobs.put(elaborazione.getJobType(), jobs.get(elaborazione.getJobType()) + 1);

			}
			catch (EbayConnectorException e) {
				logger.error("Errore nella ri-sottomissione del file: " + e.getMessage());
			}
			catch (Throwable t) {
				logger.error("Errore di ri-sottomissione della elaborazione: " + elaborazione.getIdElaborazione(), t);
			}
		}

		logger.info("Pusher terminated to work");

	}
	
	private boolean tooManyJobs(String jobType, Map<String, Integer> jobs) {

		int count = jobs.get(jobType);

		if ("AddFixedPriceItem".equals(jobType) && count >= Configurazione.getIntValue("max.add", Integer.MAX_VALUE) ) {
			return true;
		}

		if ("EndFixedPriceItem".equals(jobType) && count >= Configurazione.getIntValue("max.delete", Integer.MAX_VALUE) ) {
			return true;
		}

		if ("ReviseFixedPriceItem".equals(jobType) && count >= Configurazione.getIntValue("max.modify", Integer.MAX_VALUE) ) {
			return true;
		}

		if ("RelistFixedPriceItem".equals(jobType) && count >= Configurazione.getIntValue("max.relist", Integer.MAX_VALUE) ) {
			return true;
		}
		
		return false;

	}

	private String getExtension() {
		String extension = Configurazione.getText(Configurazione.OUTGOING_FILE_EXTENSION);
		if ( extension == null ) {
			return "xml";
		}
		return extension;
	}

}
