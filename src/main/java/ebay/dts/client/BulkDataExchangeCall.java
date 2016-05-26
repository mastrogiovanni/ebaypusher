/**
 * © 2010-2013 eBay Inc., All Rights Reserved
 * Licensed under CDDL 1.0 -  http://opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.BulkDataExchangeService;
import com.ebay.marketplace.services.BulkDataExchangeServicePort;

import it.ebaypusher.controller.EbayConnectorException;

/**
 *
 * @author zhuyang
 */
public class BulkDataExchangeCall {

    private static Log logger = LogFactory.getLog(BulkDataExchangeCall.class);

    private BindingProvider bp;

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

    public BulkDataExchangeCall(String serverURL, String userToken) {
        this.serverURL = serverURL;
        this.userToken = userToken;
    }

    public BulkDataExchangeCall(String callName, String userToken, String serverURL) {
        this.callName = callName;
        this.userToken = userToken;
        this.serverURL = serverURL;
    }

    public BulkDataExchangeCall() {
    }

    public BulkDataExchangeServicePort setRequestContext(String callName) throws EbayConnectorException {

        if (this.serverURL == null && this.serverURL.length() == 0) {
            logger.error("BulkDataExchangeService endpoint URL is not set");
            return null;
        }

        BulkDataExchangeServicePort port = null;
        try { // Call Web Service Operation
            BulkDataExchangeService service = new BulkDataExchangeService();
            port = service.getBulkDataExchangeServiceSOAP();
            bp = (BindingProvider) port;
            // Add the logging handler
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
            requestProperties.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.serverURL);
            if (this.userToken == null) {
                throw new Exception(" userToken can't be null ");
            }
            Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
            httpHeaders.put("X-EBAY-SOA-MESSAGE-PROTOCOL", Collections.singletonList("SOAP11"));
            httpHeaders.put("X-EBAY-SOA-OPERATION-NAME", Collections.singletonList(callName));
            httpHeaders.put("X-EBAY-SOA-SECURITY-TOKEN", Collections.singletonList(this.userToken));
            requestProperties.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
        //http://developer.ebay.com/DevZone/bulk-data-exchange/CallRef/createUploadJob.html#Request.uploadJobType

        } catch (Exception ex) {
        	logger.error(ex.getMessage());
        	throw new EbayConnectorException(ex.getMessage(), ex);
        }

        return port;
        
    }

    public BulkDataExchangeServicePort setRequestContext() throws EbayConnectorException {
        BulkDataExchangeServicePort port = null;
        try { // Call Web Service Operation
            BulkDataExchangeService service = new BulkDataExchangeService();
            port = service.getBulkDataExchangeServiceSOAP();
            bp = (BindingProvider) port;
            // Add the logging handler
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
            requestProperties.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serverURL);

            Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
            httpHeaders.put("X-EBAY-SOA-MESSAGE-PROTOCOL", Collections.singletonList("SOAP11"));
            httpHeaders.put("X-EBAY-SOA-OPERATION-NAME", Collections.singletonList(this.callName));
            httpHeaders.put("X-EBAY-SOA-SECURITY-TOKEN", Collections.singletonList(this.userToken));

            requestProperties.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
            retrieveHttpHeaders(bp,"Response");
        } catch (Exception ex) {
        	logger.error(ex.getMessage());
        	throw new EbayConnectorException(ex.getMessage(), ex);
        }
        return port;
    }

    private Map<String, Object> retrieveHttpHeaders(BindingProvider bp, String headerType) {
    	
        logger.trace("headerType " + headerType);

        Map<String, Object> headerM = null;
        Map<String, Object> contextMap = null;
        String headerTypeName = null;
        if (headerType.equalsIgnoreCase("request")) {
            headerTypeName = "javax.xml.ws.http.request.headers";
            contextMap =
                    bp.getRequestContext();
        } else {
            headerTypeName = "javax.xml.ws.http.response.headers";
            contextMap =
                    bp.getResponseContext();
        }

        if (contextMap != null) {
            dumpMap(headerType + " context", contextMap);
            Map requestHeaders = (Map<String, List<String>>) contextMap.get(headerTypeName);
            if (requestHeaders != null) {
                headerM = insertHttpsHeadersMap(headerType, requestHeaders);
            }

        }
        return headerM;
    }

    public static Map insertHttpsHeadersMap(String name, Map<String, List<String>> maplist) {
    	StringBuilder builder = new StringBuilder();
    	builder.append("=== " + name + "\n");
        Map headers = new HashMap<String, Object>();
        Iterator headerIter = null;
        if (maplist != null) {
            maplist.entrySet();
            headerIter = maplist.keySet().iterator();
            while (headerIter.hasNext()) {
                String key = (String) headerIter.next();
                builder.append("Key: " + key);
                List l = (List<String>) maplist.get(key);
                Iterator iter = l.iterator();
                String value = null;
                while (iter.hasNext()) {
                    value = (String) iter.next();
                    builder.append("; Value: " + value + "\n");
                }
                headers.put(key, value);
            }
        }
        logger.debug(builder.toString());
        return headers;
    }

    public static void dumpMap(String name, Map<String, Object> map) {
    	logger.trace("=== " + name);
        for (Map.Entry e : map.entrySet()) {
        	logger.trace(e.getKey() + " : " + e.getValue());
        }
    }
}
