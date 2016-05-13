package it.ebaypusher.controller;

public class EbayConnectorException extends Exception {

	private static final long serialVersionUID = 1L;

	public EbayConnectorException() {
	}

	public EbayConnectorException(String message) {
		super(message);
	}

	public EbayConnectorException(Throwable cause) {
		super(cause);
	}

	public EbayConnectorException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
