package it.ebaypusher.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;

import com.ebay.marketplace.services.JobProfile;
import com.ebay.marketplace.services.JobStatus;

import it.ebaypusher.constants.Stato;
import it.ebaypusher.dao.SnzhElaborazioniebay;

public class Utility {

	public static void updateWith(SnzhElaborazioniebay elaborazione, JobProfile jobProfile) {
		elaborazione.setJobStatus(getStatus(jobProfile));
		elaborazione.setJobPercCompl(getPercent(jobProfile));
	}
	
	/**
	 * Verifica se lo stato del job su EBay è cambiato
	 * 
	 * @param jobProfile
	 * @param elaborazione
	 * @return
	 */
	public static boolean statusChanged(JobProfile jobProfile, SnzhElaborazioniebay elaborazione) {
		
		// Vecchio stato del job
		JobStatus oldStatus = JobStatus.valueOf(elaborazione.getJobStatus());
		
		// Cambiato lo stato
		if (!oldStatus.equals(jobProfile.getJobStatus())) {
			return true;
		}
		
		int percentComplete = getPercent(jobProfile);

		// Cambiata la percentuale di completamento
		return (elaborazione.getJobPercCompl() != percentComplete);
		
	}

	/**
	 * Ritorna percentuale di completamento
	 * 
	 * @param jobProfile
	 * @return
	 */
	public static int getPercent(JobProfile jobProfile) {
		int percentComplete = 0;
		if ( jobProfile.getPercentComplete() != null ) {
			percentComplete = (int) Math.round(jobProfile.getPercentComplete());
		}
		return percentComplete;
	}
	
	public static String getStatus(JobProfile jobProfile) {
		if ( jobProfile.getJobStatus() != null ) {
			return jobProfile.getJobStatus().toString();
		}
		return null;
	}

	/**
	 * Una elaborazione è terminata se ha raggiunto il massimo numero di tentativi
	 * di terminazione, oppure se è terminata con successo e i files di risposta sono
	 * impostati, verifica che sono presenti.
	 * 
	 * @param elaborazione
	 * @return
	 */
	public static boolean isProcessoTerminated(SnzhElaborazioniebay elaborazione) {
		
		if ( Stato.SUPERATO_NUMERO_MASSIMO_INVII.toString().equals(elaborazione.getFaseJob())) {
			return esitoIsOk(elaborazione);
		}
		
		if ( Stato.TERMINATO_CON_SUCCESSO.toString().equals(elaborazione.getFaseJob())) {
			return isJobCompleted(elaborazione);
		}
		
		return false;
		
	}
	
	public static boolean isJobCompledGood(SnzhElaborazioniebay elaborazione) {
		
		if (!JobStatus.COMPLETED.toString().equals(elaborazione.getJobStatus())) {
			return false;
		}
				
		return (elaborazione.getJobPercCompl() == 100.0);
	}

	public static boolean isJobCompledBad(SnzhElaborazioniebay elaborazione) {
		
		if (JobStatus.FAILED.toString().equals(elaborazione.getJobStatus())) {
			return true;
		}

		if (JobStatus.ABORTED.toString().equals(elaborazione.getJobStatus())) {
			return true;
		}
		
		return false;

	}

	/**
	 * Verifica che sia terminato un job ebay
	 * 
	 * @param elaborazione
	 * @return
	 */
	public static boolean isJobCompleted(SnzhElaborazioniebay elaborazione) {
		JobStatus status = JobStatus.valueOf(elaborazione.getJobStatus());
		if ( status == JobStatus.ABORTED ) {
			return true;
		}
		if ( status == JobStatus.FAILED ) {
			return true;
		}
		if ( status == JobStatus.COMPLETED && elaborazione.getJobPercCompl() == 100.0 ) {
			return true;
		}
		return false;
	}
	
	/**
	 * Verifica che una elaborazione, che specifica il file di esito, sia realmente 
	 * presente. Nel caso una elaborazione non lo valorizzi, allora va bene.
	 * 
	 * @param elaborazione
	 * @return
	 */
	public static boolean esitoIsOk(SnzhElaborazioniebay elaborazione) {

		if ( elaborazione.getPathFileEsito() == null ) {
			return true;
		}

		return new File(elaborazione.getPathFileEsito()).exists();
		
	}
	
	/**
	 * @param fileName Path assoluto di un file in una delle cartelle di sistema
	 * @return Nme sintetico del file
	 */
	public static String getFileLabel(String fileName) {
		return getFileLabel(new File(fileName));
	}

	/**
	 * @param file file in una delle cartelle di sistema
	 * @return Nme sintetico del file
	 */
	public static String getFileLabel(File file) {
		File output = new File(Configurazione.getText(Configurazione.OUTGOING_DIR));
		File sent = new File(Configurazione.getText(Configurazione.SENT_DIR));
		File error = new File(Configurazione.getText(Configurazione.ERROR_DIR));
		File report = new File(Configurazione.getText(Configurazione.REPORT_DIR));
		if ( file.getParentFile().equals(output)) {
			return "(OUTPUT) " + file.getName();
		}
		if ( file.getParentFile().equals(sent)) {
			return "(SENT) " + file.getName();
		}
		if ( file.getParentFile().equals(error)) {
			return "(ERROR) " + file.getName();
		}
		if ( file.getParentFile().equals(report)) {
			return "(REPORT) " + file.getName();
		}
		return file.getAbsolutePath();
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
		String fileName = getNewName(elaborazione) + ".xml";
		return new File(root, fileName);
	}

	/**
	 * Copy a stream into another
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer, 0, buffer.length)) > 0) {
			out.write(buffer, 0, len);
		}
		out.flush();
	}

	/**
	 * @param elaborazione
	 * @return Ritorna il nuovo nome del file:
	 * <ID ELABORAZIONE EBAY>_<FILE_NAME>_<ID JOB EBAY>_<DATA INVIO>_<ORA INVIO>; 
	 */
	private static String getNewName(SnzhElaborazioniebay elaborazione) {
		StringBuilder builder = new StringBuilder();
		builder.append(elaborazione.getIdElaborazione());
		builder.append("_");
		builder.append(elaborazione.getFilename());
		builder.append("_");
		builder.append(elaborazione.getJobId());
		builder.append("_");
		builder.append(DateUtility.formatDate(elaborazione.getDataInserimento(), "yyyyMMdd"));
		builder.append("_");
		builder.append(DateUtility.formatDate(elaborazione.getDataInserimento(), "HHmmss"));
		return builder.toString();
	}	

}
