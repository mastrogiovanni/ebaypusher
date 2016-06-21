package it.ebaypusher.controller;

import java.io.File;

import com.ebay.marketplace.services.JobProfile;

import it.ebaypusher.dao.SnzhElaborazioniebay;

public interface EbayController {

	/**
	 * Test connection to Ebay
	 */
	boolean test();

	/**
	 * Crea un job di import
	 * 
	 * @param file
	 * @return
	 * @throws EbayConnectorException 
	 */
	public void create(SnzhElaborazioniebay elaborazione) throws EbayConnectorException;

	/**
	 * Upload il payload del job di import
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	void upload(SnzhElaborazioniebay elaborazione) throws EbayConnectorException;

	/**
	 * Inizia il processing di un job di import
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	void start(SnzhElaborazioniebay elaborazione) throws EbayConnectorException;

	void startActiveInventoryReport(SnzhElaborazioniebay elaborazione) throws Exception;

	/**
	 * Salva sul file collegato all'elaborazione la risposta di ebay per questa elaborazione
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	boolean saveResponseFile(SnzhElaborazioniebay elaborazione) throws EbayConnectorException;

	/**
	 * Stoppa una elaborazione specificando il Job id
	 * 
	 * @param jobId
	 * @throws EbayConnectorException
	 */
	void abort(String jobId) throws EbayConnectorException;

	/**
	 * Killa tutti i job che sono online in stato CREATED
	 * @throws EbayConnectorException 
	 */
	void killAll() throws EbayConnectorException;

	boolean downloadResponse(String jobId, File output) throws EbayConnectorException;

	/**
	 * @param jobId Identifier of a job
	 * @return Status of a job
	 * @throws EbayConnectorException
	 */
	JobProfile getJobProfile(String jobId) throws EbayConnectorException;

	/**
	 * Effettua il parsing del file XML e ritorna il tipo di Job
	 * 
	 * @param file
	 * @return
	 * @throws EbayConnectorException
	 */
	String getJobTypeFromXML(File file) throws EbayConnectorException;

}


