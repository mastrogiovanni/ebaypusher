package it.ebaypusher.utility;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.ebaypusher.dao.SnzhEsitiebay;

public class EsitoParser {

	private static XPath xPath;

	private static XPathExpression xpNode;

	private static XPathExpression time;
	private static XPathExpression ack;
	private static XPathExpression startTime;
	private static XPathExpression endTime;
	private static XPathExpression sku;

	private static XPathExpression segnalazioni;
	private static XPathExpression longMessage;
	private static XPathExpression errorCode;

	static {
		try {

			xPath = XPathFactory.newInstance().newXPath();

			xpNode = xPath.compile("/BulkDataExchangeResponses/*");
			time = xPath.compile("Timestamp");
			ack = xPath.compile("Ack");
			startTime = xPath.compile("StartTime");
			endTime = xPath.compile("EndTime");
			sku = xPath.compile("SKU");

			segnalazioni = xPath.compile("Errors");
			longMessage = xPath.compile("LongMessage");
			errorCode = xPath.compile("ErrorCode");
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

	}

	public static List<SnzhEsitiebay> parse(InputStream in) {

		try {

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();

			Document document = documentBuilder.parse(in);
			
			NodeList nodes = (NodeList) xpNode.evaluate(document, XPathConstants.NODESET);

			List<SnzhEsitiebay> esiti = new LinkedList<SnzhEsitiebay>();

			for ( int i = 0; i < nodes.getLength(); i ++ ) {

				Node item = nodes.item(i);
				
				SnzhEsitiebay esito = parseEsito(item);
				
				esito.setResponseType(item.getNodeName());

				esiti.add(esito);

			}
			
			return esiti;

		}
		catch (Throwable t) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			SnzhEsitiebay esito = new SnzhEsitiebay();
			esito.setSegnalazioni(sw.toString());
			return Arrays.asList(esito);
		}
		
	}

	private static SnzhEsitiebay parseEsito(Node item) throws XPathExpressionException, ParseException {

		SnzhEsitiebay esito = new SnzhEsitiebay();

		esito.setDataEsito(parse((String) time.evaluate(item, XPathConstants.STRING)));
		esito.setEndTime(parse((String) endTime.evaluate(item, XPathConstants.STRING)));
		esito.setStartTime(parse((String) startTime.evaluate(item, XPathConstants.STRING)));

		esito.setAck((String) ack.evaluate(item, XPathConstants.STRING));
		esito.setSku((String) sku.evaluate(item, XPathConstants.STRING));

		NodeList errors = (NodeList) segnalazioni.evaluate(item, XPathConstants.NODESET);

		StringBuilder builder = new StringBuilder();

		for ( int j = 0; j < errors.getLength(); j ++ ) {

			String msg = (String) longMessage.evaluate(errors.item(j), XPathConstants.STRING);
			String error = (String) errorCode.evaluate(errors.item(j), XPathConstants.STRING);

			if ( builder.length() > 0 ) {
				builder.append(", ");
			}
			builder.append(error + ":" + msg);

		}

		if ( builder.length() > 0 ) {
			esito.setSegnalazioni(builder.toString());
		}

		return esito;

	}

	private static Date parse(String time) throws ParseException {
		return DateUtility.parseDate(time, "yyyy-MM-dd'T'HH:mm:ss.S'Z'");
	}

}
