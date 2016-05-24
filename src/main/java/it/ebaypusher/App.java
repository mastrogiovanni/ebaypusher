package it.ebaypusher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONWriter;

import com.ebay.marketplace.services.GetJobsResponse;
import com.ebay.marketplace.services.JobProfile;

import ebay.dts.client.BulkDataExchangeActions;
import it.ebaypusher.batch.Parser;
import it.ebaypusher.batch.Puller;
import it.ebaypusher.batch.Pusher;
import it.ebaypusher.controller.EbayConnectorException;
import it.ebaypusher.controller.EbayController;
import it.ebaypusher.controller.EbayControllerImpl;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.utility.Configurazione;
import it.ebaypusher.utility.DateUtility;

/**
 * CREATE UPLOAD -> sposta il file in sent START - PROGRESS
 * 
 * 
 */
public class App {

	private static Log logger = LogFactory.getLog(App.class);
	
	private static final void usage() {
		System.out.println("Usage: java ebaypusher.jar [OPTION]...");
		System.out.println("");
		System.out.println("-status dd/mm/yyyy dd/mm/yyyy        Stampa informazioni sui job con data di creazione");
		System.out.println("                                     compresa fra la prima data inclusa e la seconda esclusa");
		System.out.println("-kill JobId                          Effettua il kill del job identitificato con quel JobId");
		
		// [ batch | status <DateFrom> <DateTo> <JobId> <JobStatus> | abort <JobId> 		
		
	}
	
	public static void main(String[] args) throws Exception {

		// Setup System Configuration
		setupSystem();
		
		if ( args.length > 0 ) {

			if ( args[0].toLowerCase().contains("help")) {
				usage();
				System.exit(0);
			}

			if ( "-status".equals(args[0])) {
				
				if ( args.length < 3 ) {
					usage();
					System.exit(0);
				}
				
				Date from = DateUtility.parseDate(args[1], "dd/MM/yyyy");
				Date to = DateUtility.parseDate(args[2], "dd/MM/yyyy");
				
				if ( from == null || to == null ) {
					usage();
					System.exit(0);
				}
				
				if ( from.after(to)) {
					Date tmp = from;
					from = to;
					to = tmp;
				}
				
				logger.info("Show status of job on EBay between: " + DateUtility.formatDate(from, "dd/MM/yyyy") + " and " + DateUtility.formatDate(to, "dd/MM/yyyy"));
				showStatus(from, to);
				System.exit(0);
			}
			
			else if ("-kill".equals(args[0])) {
				
				if ( args.length > 1 ) {
					String jobId = args[1];
					logger.info("Stopping job: " + jobId);
					EbayController connector = new EbayControllerImpl();
					try {
						connector.abort(jobId);
					}
					catch (EbayConnectorException e) {
						System.err.println(e.getMessage());
					}
					System.exit(0);
				}

				usage();
				System.exit(0);

			}
		}

		Properties props = Configurazione.getConfiguration();
		props.setProperty("eclipselink.ddl-generation", "create-tables");
		props.setProperty("eclipselink.ddl-generation.output-mode", "database");
		props.setProperty("eclipselink.logging.level", "SEVERE");

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("persistenceUnit", props);
		EntityManager manager = factory.createEntityManager();
		Dao dao = new Dao(manager);
		logger.info("Database connected");
		
		EbayController connector = new EbayControllerImpl();
		logger.info("Ebay connection setup");
		
//		connector.downloadResponse("5827135321", new File("/home/michele/ebay/risposta.xml"));

		if ( Configurazione.getConfiguration().get("perpetual") != null ) {
			while ( true ) {
				workCycle(dao, connector);
			}
		}
		else {
			workCycle(dao, connector);
		}

	}

	private static void setupSystem() throws FactoryConfigurationError {
		// Load log4j from file
		LogManager.resetConfiguration();
		DOMConfigurator.configure(new File("conf", "log4j.xml").getPath());
		
		copyInSystem("http.proxySet");
		copyInSystem("http.proxyHost");
		copyInSystem("http.proxyPort");

		copyInSystem("https.proxySet");
		copyInSystem("https.proxyHost");
		copyInSystem("https.proxyPort");
	}

	private static void workCycle(Dao dao, EbayController connector) throws FactoryConfigurationError, Exception, EbayConnectorException, ClassNotFoundException {
				
		if ( Configurazione.getConfiguration().getProperty("killall") != null ) {
			connector.killAll();
		}

		Pusher pusher = new Pusher(dao, connector);
		pusher.run();

		Puller puller = new Puller(dao, connector);
		puller.run();

		Parser parser = new Parser(dao);
		parser.run();

	}

	private static void showStatus(Date from, Date to) throws Exception {
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
		StringBuilder builder = new StringBuilder();
		
		builder.append("creationTimeFrom=" + DateUtility.formatDate(from, "yyyy-MM-dd"));
		builder.append("&creationTimeTo=" + DateUtility.formatDate(to, "yyyy-MM-dd"));
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream(); 
		
		GetJobsResponse response = bdeActions.getJobs(builder.toString());
		OutputStreamWriter out = new OutputStreamWriter(bout);
		for (JobProfile profile : response.getJobProfile()) {

			out.append("\n");

			JSONWriter writer = new JSONWriter(out);
			writer
				.object()
					.key("id").value(profile.getJobId())
					.key("status").value(profile.getJobStatus())
					.key("type").value(profile.getJobType())
					.key("percent").value(profile.getPercentComplete())
					.key("creation time").value(profile.getCreationTime())
					.key("completion time").value(profile.getCompletionTime())
					.key("file id").value(profile.getFileReferenceId())
					.key("input file id").value(profile.getInputFileReferenceId())
				.endObject();
									
		}
		
		out.append("\n");

		out.flush();

		logger.info(new String(bout.toByteArray()));

	}

//	public static <T> Map<String, Field> getAllFields(Class<T> clazz) {
//		Map<String, Field> result = new TreeMap<String, Field>();
//		Class<?> tmpClass = clazz;
//		while (tmpClass != null) {
//			for ( Field field : tmpClass.getDeclaredFields() ) {
//				result.put(field.getName(), field);	    		
//			}
//			tmpClass = tmpClass .getSuperclass();
//		}
//		return result;
//	}
	
	private static void copyInSystem(String property) {
		String value = Configurazione.getText(property);
		if ( value == null ) {
			return;
		}
		System.setProperty(property, value);
	}
	
}
