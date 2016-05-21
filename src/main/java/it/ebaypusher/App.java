package it.ebaypusher;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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
import it.ebaypusher.batch.Puller;
import it.ebaypusher.batch.Pusher;
import it.ebaypusher.controller.EbayConnectorException;
import it.ebaypusher.controller.EbayController;
import it.ebaypusher.controller.EbayControllerImpl;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.utility.Configurazione;

/**
 * CREATE UPLOAD -> sposta il file in sent START - PROGRESS
 * 
 * 
 */
public class App {

	private static Log logger = LogFactory.getLog(App.class);
	
	private static final void usage() {
		System.out.println("Usage: java ebaypusher.jar <command> ... options");
		System.out.println("");
		
		// [ batch | status <DateFrom> <DateTo> <JobId> <JobStatus> | abort <JobId> 		
		
	}
	
	public static void main(String[] args) throws Exception {

		// Load log4j from file
		LogManager.resetConfiguration();
		DOMConfigurator.configure(new File("conf", "log4j.xml").getPath());
		
		copyInSystem("http.proxySet");
		copyInSystem("http.proxyHost");
		copyInSystem("http.proxyPort");

		copyInSystem("https.proxySet");
		copyInSystem("https.proxyHost");
		copyInSystem("https.proxyPort");
		
		if ( args.length > 0 ) {
			if ( "status".equals(args[0])) {
				logger.info("Show status of job on EBay");
				showStatus();
				System.exit(0);
			}
			else if ("abort".equals(args[0])) {
				if ( args.length > 1 ) {
					String jobId = args[1];
					logger.info("Stopping job: " + jobId);
					EbayController connector = new EbayControllerImpl();
					connector.abort(jobId);
					System.exit(0);
				}
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

	private static void workCycle(Dao dao, EbayController connector) throws FactoryConfigurationError, Exception, EbayConnectorException, ClassNotFoundException {
				
		if ( Configurazione.getConfiguration().getProperty("killall") != null ) {
			connector.killAll();
		}

		Pusher pusher = new Pusher(dao, connector);
		pusher.run();

		Puller puller = new Puller(dao, connector);
		puller.run();
		
	}

	private static void showStatus() throws Exception {
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
		StringBuilder builder = new StringBuilder();
		
		builder.append("creationTimeFrom=2016-05-18");
		
		builder.append("&creationTimeTo=2016-05-19");
		
		GetJobsResponse response = bdeActions.getJobs(builder.toString());
		OutputStreamWriter out = new OutputStreamWriter(System.out);
		for (JobProfile profile : response.getJobProfile()) {
			
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
			
			out.append("\n");
			
//			System.out.println(profile.getJobStatus() + ": " +  + ", " + profile.getFileReferenceId()
//			+ ", " + profile.getPercentComplete() + "%");
		}
		
		out.flush();

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
