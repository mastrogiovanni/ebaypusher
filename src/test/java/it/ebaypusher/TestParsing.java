package it.ebaypusher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import it.ebaypusher.dao.SnzhEsitiebay;
import it.ebaypusher.utility.EsitoParser;

public class TestParsing {

	public static void main(String[] args) throws XPathExpressionException, FileNotFoundException, IOException, ParserConfigurationException, SAXException, ParseException {
		
		// InputStream in = new FileInputStream("test/OK_14_20160519223437_DEL_catalogo_ballo_ebay.txt.xml_5827624571_20160519_223518.xml");
		// InputStream in = new FileInputStream("test/OK_4_20160519215847_ADD_catalogo_ballo_ebay.txt.xml_5827599791_20160519_215914.xml");
		// InputStream in = new FileInputStream("test/25_20160522143030_REV_catalogo_ballo_ebay.txt.xml_5830190831_20160522_143136.xml");
		InputStream in = new FileInputStream("test/OK_5_20160519220504_MOD_catalogo_ballo_ebay.txt.xml_5827604421_20160519_220539.xml");
		
		List<SnzhEsitiebay> esiti = EsitoParser.parse(in);
		
		for ( SnzhEsitiebay esito : esiti ) {
			
			System.out.println(esito);
			
		}
		
	}
	
}
