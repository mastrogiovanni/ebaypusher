/**
 * � 2009-2013 eBay Inc., All Rights Reserved
 * Licensed under CDDL 1.0 -  http://opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author zhuyang
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

	private static Log logger = LogFactory.getLog(LoggingHandler.class);

	public Set<QName> getHeaders() {
		return null;
	}

	public boolean handleMessage(SOAPMessageContext context) {
		log(context);
		return true;
	}

	public boolean handleFault(SOAPMessageContext context) {
		log(context);
		return true;
	}

	public void close(MessageContext messageContext) {
	}

	private void log(SOAPMessageContext messageContext) {
		boolean request = ((Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();
		
		SOAPMessage meg = messageContext.getMessage();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			meg.writeTo(out);
		} catch (Exception e) {
		}
		
		if (request) {
			logger.trace("SOAP Request message: " + new String(out.toByteArray()));
		} else {
			logger.trace("SOAP Response message: " + new String(out.toByteArray()));
		}
		
	}
}
