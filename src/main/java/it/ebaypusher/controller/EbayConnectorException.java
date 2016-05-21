package it.ebaypusher.controller;

import com.ebay.marketplace.services.ErrorData;

public class EbayConnectorException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private ErrorData errorData;

	public EbayConnectorException(ErrorData errorData) {
		super(errorData.getSeverity() + " --> ErrorID=" + errorData.getErrorId() + " ; ErrorMessage=\"" + errorData.getMessage() + "\"");
		this.errorData = errorData;
	}

	public EbayConnectorException(String message) {
		super(message);
	}

	public EbayConnectorException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public ErrorData getErrorData() {
		return errorData;
	}
	
}
