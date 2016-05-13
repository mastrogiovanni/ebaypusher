package it.ebaypusher.controller;

import it.ebaypusher.dao.ElaborazioniEbay;

public interface EbayController {
	
	/**
	 * Crea un job di import
	 * 
	 * @param file
	 * @return
	 * @throws EbayConnectorException 
	 */
	public void create(ElaborazioniEbay elaborazione) throws EbayConnectorException;

	/**
	 * Upload il payload del job di import
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	void upload(ElaborazioniEbay elaborazione) throws EbayConnectorException;

	/**
	 * Inizia il processing di un job di import
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	void start(ElaborazioniEbay elaborazione) throws EbayConnectorException;

	/**
	 * Aggiorna lo stato dell'elaborazione in base alla situazione del job
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	void updateStatus(ElaborazioniEbay elaborazione) throws EbayConnectorException;

	/**
	 * Salva sul file collegato all'elaborazione la risposta di ebay per questa elaborazione
	 * 
	 * @param elaborazione
	 * @throws EbayConnectorException
	 */
	void saveResponseFile(ElaborazioniEbay elaborazione) throws EbayConnectorException;

}


