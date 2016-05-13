package it.ebaypusher.utility;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateUtility {

	private static Log logger = LogFactory.getLog(DateUtility.class);

	public static Date parseDate(String string, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.parse(string);
		} catch (Throwable t) {
			logger.warn("Error in date format: " + t.getMessage());
		}
		return null;
	}

	/**
	 * Format a date
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		try {
			if (date == null)
				return null;
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(date);
		} catch (Throwable t) {
			logger.warn("Error in date format: " + t.getMessage());
		}
		return null;
	}

}
