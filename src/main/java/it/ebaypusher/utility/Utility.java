package it.ebaypusher.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import it.ebaypusher.dao.SnzhElaborazioniebay;

public class Utility {
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer, 0, buffer.length)) > 0) {
			out.write(buffer, 0, len);
		}
		out.flush();
	}
	
	public static File getInputFile(SnzhElaborazioniebay elaborazione) {
		File root = new File(Configurazione.getText(Configurazione.OUTGOING_DIR));
		return new File(root, elaborazione.getFilename());
	}
	
	public static File getSentFile(SnzhElaborazioniebay elaborazione) {
		File root = new File(Configurazione.getText(Configurazione.SENT_DIR));
		String fileName = getNewName(elaborazione) + ".xml";
		return new File(root, fileName);
	}

	public static File getErrorFile(SnzhElaborazioniebay elaborazione) {
		File root = new File(Configurazione.getText(Configurazione.ERROR_DIR));
		String fileName = getNewName(elaborazione) + ".xml";
		return new File(root, fileName);
	}

	public static File getReportFile(SnzhElaborazioniebay elaborazione) {
		File root = new File(Configurazione.getText(Configurazione.REPORT_DIR));
		String fileName = getNewName(elaborazione) + ".xml.gz";
		return new File(root, fileName);
	}

	/**
	 * @param elaborazione
	 * @return Ritorna il nuovo nome del file:
	 * <ID ELABORAZIONE EBAY>_<ID JOB EBAY>_<DATA INVIO>_<ORA INVIO>; 
	 */
	private static String getNewName(SnzhElaborazioniebay elaborazione) {
		StringBuilder builder = new StringBuilder();
		builder.append(elaborazione.getIdElaborazione());
		builder.append("_");
		builder.append(elaborazione.getJobId());
		builder.append("_");
		builder.append(DateUtility.formatDate(elaborazione.getDataInserimento(), "yyyyMMdd"));
		builder.append("_");
		builder.append(DateUtility.formatDate(elaborazione.getDataInserimento(), "HHmmss"));
		return builder.toString();
	}	

}
