package it.ebaypusher;

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
import it.ebaypusher.dao.ElaborazioniEbay;
import it.ebaypusher.utility.Configurazione;

/**
 * CREATE UPLOAD -> sposta il file in sent START - PROGRESS
 * 
 * 
 */
public class App {

	private static Log logger = LogFactory.getLog(App.class);

	public static void main(String[] args) throws Exception {
		
    	System.setProperty("http.proxySet", "true");
    	System.setProperty("http.proxyHost", "10.100.114.67");
    	System.setProperty("http.proxyPort", "8080");

    	System.setProperty("https.proxySet", "true");
    	System.setProperty("https.proxyHost", "10.100.114.67");
    	System.setProperty("https.proxyPort", "8080");
    	
    	// startCreated();
//    	showStatus();
//    	System.exit(0);
    	
		Dao dao = new Dao();

		EbayController connector = new EbayControllerImpl();

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

	private static void startCreated() throws ClassNotFoundException, EbayConnectorException {
		Dao dao = new Dao();
		EbayController connector = new EbayControllerImpl();
		for (ElaborazioniEbay e : dao.findAll()) {
			connector.start(e);
		}
	}

}
