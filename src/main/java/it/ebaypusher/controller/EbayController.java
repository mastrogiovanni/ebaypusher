package it.ebaypusher.controller;

import it.ebaypusher.dao.SnzhElaborazioniebay;

public interface EbayController {
	
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

	/**
	 * Aggiorna lo stato dell'elaborazione in base alla situazione del job
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	void updateProgressAndStatus(SnzhElaborazioniebay elaborazione) throws EbayConnectorException;

	/**
	 * Salva sul file collegato all'elaborazione la risposta di ebay per questa elaborazione
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	void saveResponseFile(SnzhElaborazioniebay elaborazione) throws EbayConnectorException;

	/**
	 * Stoppa una elaborazione specificando il Job id
	 * 
	 * @param jobId
	 * @throws EbayConnectorException
	 */
	void abort(String jobId) throws EbayConnectorException;

}


