/**
 * © 2009-2013 eBay Inc., All Rights Reserved
 * Licensed under CDDL 1.0 -  http://opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.FileTransferService;
import com.ebay.marketplace.services.FileTransferServicePort;
import com.sun.xml.ws.developer.JAXWSProperties;

import it.ebaypusher.controller.EbayConnectorException;


/**
 *
 * @author zhuyang
 */
public class FileTransferCall {

	public FileTransferCall() {
	}

	private static Log logger = LogFactory.getLog(FileTransferCall.class);

	public FileTransferCall(String serverURL, String userToken, String callName) {
		this.serverURL = serverURL;
		this.userToken = userToken;
		this.callName = callName;
	}

	public FileTransferCall(String serverURL, String userToken) {
		this.serverURL = serverURL;
		this.userToken = userToken;
	}

	public String getCallName() {
		return callName;
	}

	public void setCallName(String callName) {
		this.callName = callName;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
	private String callName;
	private String userToken;
	private String serverURL;

	public FileTransferServicePort setFTSMessageContext() throws EbayConnectorException {
		FileTransferServicePort port = null;
		FileTransferService service = new FileTransferService();
		
		try {
			
			port = service.getFileTransferServiceSOAP();
			BindingProvider bp = (BindingProvider) port;
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.serverURL);
			List handlerList = bp.getBinding().getHandlerChain();
			if (handlerList == null) {
				handlerList = new ArrayList();
			}
			LoggingHandler loggingHandler = new LoggingHandler();
			handlerList.add(loggingHandler);
			// register the handerList
			bp.getBinding().setHandlerChain(handlerList);
			// initialize WS operation arguments here
			Map requestProperties = bp.getRequestContext();
			// set http address
			if (this.serverURL == null) {
				throw new Exception(" serverURL can't be null ");

			}
			int timeout = 100;
			requestProperties.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serverURL);
			requestProperties.put(JAXWSProperties.CONNECT_TIMEOUT, timeout);


			Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
			httpHeaders.put("X-EBAY-SOA-MESSAGE-PROTOCOL", Collections.singletonList("SOAP12"));
			httpHeaders.put("X-EBAY-SOA-OPERATION-NAME", Collections.singletonList(this.callName));
			httpHeaders.put("X-EBAY-SOA-SECURITY-TOKEN", Collections.singletonList(this.userToken));

			requestProperties.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
			
		} catch (Exception e) {
        	throw new EbayConnectorException("Errore nel setup uplink: " + e.getMessage(), e);
		}
		
		return port;

	}

	public FileTransferServicePort setFTSMessageContext(String callName) throws EbayConnectorException {
		
		logger.trace("FileTransferActions.setFTSMessageContext(String callName ) ...... ");
		
		FileTransferServicePort port = null;
		FileTransferService service = new FileTransferService();

		port = service.getFileTransferServiceSOAP();
		BindingProvider bp = (BindingProvider) port;

		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serverURL);
		List handlerList = bp.getBinding().getHandlerChain();
		if (handlerList == null) {
			handlerList = new ArrayList();
		}
		LoggingHandler loggingHandler = new LoggingHandler();
		handlerList.add(loggingHandler);
		// register the handerList
		bp.getBinding().setHandlerChain(handlerList);
		// initialize WS operation arguments here
		Map requestProperties = bp.getRequestContext();

		// set http address
		logger.trace("serverURL :" + this.serverURL);

		if (this.serverURL == null) {
			throw new EbayConnectorException(" serverURL can't be null ");
		}
		if (this.userToken == null) {
			throw new EbayConnectorException(" User Token can't be null ");
		}
		
		requestProperties.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serverURL);
		Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
		httpHeaders.put("X-EBAY-SOA-MESSAGE-PROTOCOL", Collections.singletonList("SOAP12"));
		httpHeaders.put("X-EBAY-SOA-OPERATION-NAME", Collections.singletonList(callName));
		httpHeaders.put("X-EBAY-SOA-SECURITY-TOKEN", Collections.singletonList(this.userToken));
		requestProperties.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);

		return port;

	}
}
