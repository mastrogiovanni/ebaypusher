package it.ebaypusher;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;

import com.ebay.marketplace.services.GetJobsResponse;
import com.ebay.marketplace.services.JobProfile;

import ebay.dts.client.BulkDataExchangeActions;
import it.ebaypusher.batch.Puller;
import it.ebaypusher.batch.Pusher;
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

	public static void main(String[] args) throws Exception {

		if ( args.length > 0 ) {
			if ( "status".equals(args[0])) {
				logger.info("Show status of job on EBay");
				showStatus();
				System.exit(0);
			}
		}
		
		// Load log4j from file
		LogManager.resetConfiguration();
		DOMConfigurator.configure(new File("conf", "log4j.xml").getPath());

		copyInSystem("http.proxySet");
		copyInSystem("http.proxyHost");
		copyInSystem("http.proxyPort");

		copyInSystem("https.proxySet");
		copyInSystem("https.proxyHost");
		copyInSystem("https.proxyPort");

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("persistenceUnit", Configurazione.getConfiguration());
		EntityManager manager = factory.createEntityManager();
		Dao dao = new Dao(manager);

		logger.info("Database connected");

		EbayController connector = new EbayControllerImpl();

		logger.info("Ebay connection setup");

		Pusher pusher = new Pusher(dao, connector);
		pusher.run();

		Puller puller = new Puller(dao, connector);
		puller.run();

	}

	private static void showStatus() throws Exception {
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
		GetJobsResponse response = bdeActions.getJobs(null);
		for (JobProfile profile : response.getJobProfile()) {
			System.out.println(profile.getJobStatus() + ": " + profile.getJobId() + ", " + profile.getFileReferenceId()
			+ ", " + profile.getPercentComplete() + "%");
		}
		System.out.println(response);
	}

	private static void copyInSystem(String property) {
		String value = Configurazione.getText(property);
		if ( value == null ) {
			return;
		}
		System.setProperty(property, value);
	}

}
