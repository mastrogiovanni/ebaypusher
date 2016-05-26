/**
 * © 2009-2013 eBay Inc., All Rights Reserved
 * Licensed under CDDL 1.0 -  http://opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.jar.Attributes;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author zhuyang
 */
public class CreateLMSParser {

    private static Log logger = LogFactory.getLog(CreateLMSParser.class);

	private CreateLMSParser.LMSRequestHandler handler;
	// private DefaultHandler handler;
	
	private SAXParser saxParser;

	/**
	 * Constructor
	 * @param handler - DefaultHandler for the SAX parser
	 */
	public CreateLMSParser() {
		this.handler = this.new LMSRequestHandler();
		create();
	}
	/**
	 * Create the SAX parser
	 */
	private void create() {
		try {
			// Obtain a new instance of a SAXParserFactory.
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Specifies that the parser produced by this code will provide support for XML namespaces.
			factory.setNamespaceAware(true);
			// Specifies that the parser produced by this code will validate documents as they are parsed.
			factory.setValidating(true);
			// Creates a new instance of a SAXParser using the currently configured factory parameters.
			saxParser = factory.newSAXParser();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Parse a File
	 * @param file - File
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void parse(File file) throws SAXException, IOException {
		logger.info(" CreateLMSParser.parse() ==> " + file.getAbsolutePath().toString());
		// try {
			saxParser.parse(file, this.handler);
		// } catch (Throwable t) {
		// logger.warning(t.getMessage());
		// return false;
		// }
		// return true;
	}

	public String getJobType() {
		return this.handler.getJobType();
	}
	//inner class
	class LMSRequestHandler extends DefaultHandler {

		private String jobType = null;
		private boolean isFound = false;
		public final HashMap<String, String> TAGS_AND_TYPE = new HashMap<String, String>() {

			private static final long serialVersionUID = 3660595392637116335L;

			{
				// initiate the HashMap with teh valid Job Types
				put("AddFixedPriceItemRequest", "AddFixedPriceItem");
				put("AddItemRequest","AddItem");
				put("EndFixedPriceItemRequest", "EndFixedPriceItem");
				put("EndItemRequest", "EndItem");
				put("OrderAckRequest", "OrderAck");
				put("RelistFixedPriceItemRequest", "RelistFixedPriceItem");
				put("RelistItemRequest", "RelistItem");
				put("ReviseFixedPriceItemRequest", "ReviseFixedPriceItem");
				put("ReviseInventoryStatusRequest", "ReviseInventoryStatus");
				put("ReviseItemRequest", "ReviseItem");
				put("SetShipmentTrackingInfoRequest", "SetShipmentTrackingInfo");               
				put("UploadSiteHostedPicturesRequest","UploadSiteHostedPictures");
				put("VerifyAddFixedPriceItemRequest","VerifyAddFixedPriceItem");
				put("VerifyAddItemRequest","VerifyAddItem");
			}
		};

		/**
		 * Returns the jobType read from the BDX request XML file
		 *
		 * @return jobType - String
		 */
		public String getJobType() {
			logger.info("jobType ====>>>> " + jobType);
			return jobType;
		}

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			if (isFound) {
				return;
			}
			logger.trace("startElement() ====>>>> namespaceURI=" + namespaceURI + "   :   localName=" + localName);
			if (TAGS_AND_TYPE.containsKey(localName)) {
				jobType = TAGS_AND_TYPE.get(localName);

				isFound = true;
			}
		}

		public void endElement(String uri, String localName,
				String qName)
						throws SAXException {

			// logger.debug("End Element :" + qName);
			if (isFound) {
				return;
			}

			if (TAGS_AND_TYPE.containsKey(localName)) {
				jobType = TAGS_AND_TYPE.get(localName);
				isFound = true;
			}

		}

		public boolean isFound() {
			return isFound;
		}
	}
}
