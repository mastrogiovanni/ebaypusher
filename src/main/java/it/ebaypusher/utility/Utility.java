package it.ebaypusher.utility;

import java.io.File;

import it.ebaypusher.dao.ElaborazioniEbay;

public class Utility {
	
	public static File getInputFile(ElaborazioniEbay elaborazione) {
		File root = new File(Configurazione.getText(Configurazione.OUTGOING_DIR));
		return new File(root, elaborazione.getFileName());
	}
	
	public static File getSentFile(ElaborazioniEbay elaborazione) {
		File root = new File(Configurazione.getText(Configurazione.SENT_DIR));
		String fileName = getNewName(elaborazione) + ".xml";
		return new File(root, fileName);
	}

	public static File getErrorFile(ElaborazioniEbay elaborazione) {
		File root = new File(Configurazione.getText(Configurazione.ERROR_DIR));
		String fileName = getNewName(elaborazione) + ".xml";
		return new File(root, fileName);
	}

	public static File getReportFile(ElaborazioniEbay elaborazione) {
		File root = new File(Configurazione.getText(Configurazione.REPORT_DIR));
		String fileName = getNewName(elaborazione) + ".xml.gz";
		return new File(root, fileName);
	}

	/**
	 * @param elaborazione
	 * @return Ritorna il nuovo nome del file:
	 * <ID ELABORAZIONE EBAY>_<ID JOB EBAY>_<DATA INVIO>_<ORA INVIO>; 
	 */
	private static String getNewName(ElaborazioniEbay elaborazione) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(elaborazione.getId());
		builder.append("_");
		builder.append(elaborazione.getIdJobEbay());
		builder.append("_");
		builder.append(DateUtility.formatDate(elaborazione.getDataOraInvio(), "yyyyMMdd"));
		builder.append("_");
		builder.append(DateUtility.formatDate(elaborazione.getDataOraInvio(), "HHmmss"));
		return builder.toString();
		
	}	

}
