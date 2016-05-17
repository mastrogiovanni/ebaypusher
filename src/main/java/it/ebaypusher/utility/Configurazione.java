package it.ebaypusher.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Configurazione {
	
	private static Log logger = LogFactory.getLog(Configurazione.class);

	public static final String DB_DRIVER = "db.driver";

	public static final String DB_HOST = "db.host";
	public static final String DB_PORT = "db.port";
	public static final String DB_USERNAME = "db.username";
	public static final String DB_PASSWORD = "db.password";
	public static final String DB_SCHEMA = "db.schema";
	public static final String DB_TABLE = "db.table";

	public static final String OUTGOING_DIR = "outgoing.dir";
	public static final String SENT_DIR = "sent.dir";
	public static final String ERROR_DIR = "error.dir";
	public static final String REPORT_DIR = "report.dir";

	public static final String OUTGOING_FILE_EXTENSION = "outgoing.file.extension";

	public static final String NUM_MAX_INVII = "num.max.invii";

	private static Properties props;
	
	static {
		
		props = new Properties();
		try {
			props.load(new FileInputStream("conf/config.properties"));
		} catch (IOException e) {
			logger.error("Impossibile caricare il file di configurazione: conf/config.properties", e);
		}
		
	}
	
	public static Properties getConfiguration() {
		return props;
	}
	
	public static String getText(String property) {
		String value = props.getProperty(property);
		if ( value == null ) {
			return null;
		}
		value = value.trim();
		if ( value.length() == 0 ) {
			return null;
		}
		return value;
	}
		
	public static int getIntValue(String property, int defaultValue) {
		String value = getText(property);
		if ( value == null ) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		}
		catch ( NumberFormatException e) {
			logger.error("Propertietà " + property + " non è un intero: uso valore di default " + defaultValue);
			return defaultValue;
		}
	}
	
	
	
}
