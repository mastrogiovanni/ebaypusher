package it.ebaypusher;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.GetJobsResponse;
import com.ebay.marketplace.services.JobProfile;

import ebay.dts.client.BulkDataExchangeActions;
import it.ebaypusher.batch.Puller;
import it.ebaypusher.batch.Pusher;
import it.ebaypusher.controller.EbayConnectorException;
import it.ebaypusher.controller.EbayController;
import it.ebaypusher.controller.EbayControllerImpl;
import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.SnzhElaborazioniebay;
import it.ebaypusher.utility.Configurazione;

/**
 * CREATE UPLOAD -> sposta il file in sent START - PROGRESS
 * 
 * 
 */
public class App {

	private static Log logger = LogFactory.getLog(App.class);

	public static void main(String[] args) throws Exception {
		
		copyInSystem("http.proxySet");
		copyInSystem("http.proxyHost");
		copyInSystem("http.proxyPort");

		copyInSystem("https.proxySet");
		copyInSystem("https.proxyHost");
		copyInSystem("https.proxyPort");
    	
    	// startCreated();
		// showStatus();
		// System.exit(0);
    	    	
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("persistenceUnit", Configurazione.getConfiguration());
		EntityManager manager = factory.createEntityManager();
		Dao dao = new Dao(manager);
		
		logger.info("Database connected");
		
		EbayController connector = new EbayControllerImpl();

		logger.info("Ebay connection setup");

		Pusher pusher = new Pusher(dao, connector);
		Thread threadPusher = new Thread(pusher);
		threadPusher.start();
		
		Puller puller = new Puller(dao, connector);
		Thread threadPuller = new Thread(puller);
		threadPuller.start();
	
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

	private static void startCreated() throws ClassNotFoundException, EbayConnectorException {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("persistenceUnit");
		EntityManager manager = factory.createEntityManager();
		Dao dao = new Dao(manager);
		EbayController connector = new EbayControllerImpl();
		for (SnzhElaborazioniebay e : dao.findAll()) {
			connector.start(e);
		}
	}

}
