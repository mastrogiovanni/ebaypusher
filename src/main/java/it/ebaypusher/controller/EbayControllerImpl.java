package it.ebaypusher.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.AbortJobResponse;
import com.ebay.marketplace.services.AckValue;
import com.ebay.marketplace.services.BaseServiceResponse;
import com.ebay.marketplace.services.CreateUploadJobResponse;
import com.ebay.marketplace.services.DownloadFileResponse;
import com.ebay.marketplace.services.ErrorData;
import com.ebay.marketplace.services.ErrorMessage;
import com.ebay.marketplace.services.FileAttachment;
import com.ebay.marketplace.services.GetJobStatusResponse;
import com.ebay.marketplace.services.JobProfile;
import com.ebay.marketplace.services.JobStatus;
import com.ebay.marketplace.services.StartUploadJobResponse;
import com.ebay.marketplace.services.UploadFileResponse;

import ebay.dts.client.BulkDataExchangeActions;
import ebay.dts.client.CreateLMSParser;
import ebay.dts.client.FileTransferActions;
import it.ebaypusher.constants.Stato;
import it.ebaypusher.dao.SnzhElaborazioniebay;
import it.ebaypusher.utility.Configurazione;
import it.ebaypusher.utility.Utility;

public class EbayControllerImpl implements EbayController {

	private Log logger = LogFactory.getLog(EbayController.class);

	@Override
	public void create(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {

		File file = Utility.getInputFile(elaborazione);

		// Cattura il tipo di job
		String jobType = getJobTypeFromXML(file);

		// Effettua l'upload del job
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
		CreateUploadJobResponse createUploadJobresponse = bdeActions.createUploadJob(jobType);
		if (createUploadJobresponse == null) {
			throw new EbayConnectorException("Impossibile creare il job di upload: risposta vuota");
		}

		// Verifica la risposta del WS
		check(createUploadJobresponse);

		// Assign data to elaborazione
		elaborazione.setJobType(jobType);
		elaborazione.setJobPercCompl(0);
		elaborazione.setFileReferenceId(createUploadJobresponse.getFileReferenceId());
		elaborazione.setJobId(createUploadJobresponse.getJobId());
			
	}
	
	@Override
	public void abort(String jobId) throws EbayConnectorException {

		// Effettua l'upload del job
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
		
		AbortJobResponse response = bdeActions.abortJobs(jobId);

		// Verifica la risposta del WS
		check(response);

	}
	
	@Override
	public void upload(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {
		
		File file = Utility.getInputFile(elaborazione);

		logger.info("Inizio upload file: " + file);

		FileTransferActions ftActions = new FileTransferActions(Configurazione.getConfiguration());

		UploadFileResponse uploadFileResp = ftActions.uploadFile2(
				file.getAbsolutePath(), 
				elaborazione.getJobId(), 
				elaborazione.getFileReferenceId());
		
		if (uploadFileResp == null) {
			throw new EbayConnectorException("Impossibile effettuare l'upload del job: risposta vuota");
		}

		check(uploadFileResp);

		logger.info("File trasferito con successo: " + file);
		
	}

	@Override
	public void start(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {
		
		logger.info("Richiedo avvio batch elaborazione ebay: " + elaborazione.getJobId());

		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());

		StartUploadJobResponse startUploadJobResp = bdeActions.startUploadJob(elaborazione.getJobId());
		
		check(startUploadJobResp);

		logger.info("Batch elaborazione ebay avviato con successo: " + elaborazione.getJobId());

	}
	
	@Override
	public void updateProgressAndStatus(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {
		
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
		
		GetJobStatusResponse getJobStatusResp = bdeActions.getJobStatus(elaborazione.getJobId());
		
		check(getJobStatusResp);
		
		JobProfile job = retrieveOneJobStatus(getJobStatusResp);
		
		// Aggiorna l'avanzamento
		if ( job.getPercentComplete() != null ) {
			elaborazione.setJobPercCompl((int) Math.round(job.getPercentComplete()));
		}
		
		if (job.getJobStatus() != null) {
			elaborazione.setJobStatus(job.getJobStatus().toString());
		}

		if (job.getJobStatus().equals(JobStatus.COMPLETED) && job.getPercentComplete() == 100.0) {
			logger.info("jobId=" + job.getJobId() + "; " + "jobFileReferenceId=" + job.getFileReferenceId() + " : " + job.getJobType() + " : " + job.getJobStatus());
			elaborazione.setFaseJob(Stato.TERMINATO_CON_SUCCESSO.toString());
			elaborazione.setDataElaborazione(new Timestamp(System.currentTimeMillis()));
			return;
		}
		
		if (job.getJobStatus().equals(JobStatus.FAILED) || job.getJobStatus().equals(JobStatus.ABORTED)) {
			logger.error("JobId=" + job.getJobId() + ": " + "Job Type " + job.getJobType() + " : JobStatus= " + job.getJobStatus());
			elaborazione.setFaseJob(Stato.TERMINATO_CON_ERRORE.toString());
			elaborazione.setDataElaborazione(new Timestamp(System.currentTimeMillis()));
			return;
		}
		
		logger.info("JobId=" + job.getJobId() + ": " + "Job Type " + job.getJobType() + " : JobStatus= " + job.getJobStatus());
		
	}
	
	@Override
	public void saveResponseFile(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {
		
		File fileToSave = null;
		
		if ( Stato.TERMINATO_CON_SUCCESSO.toString().equals(elaborazione.getFaseJob()) ) {
			fileToSave = Utility.getReportFile(elaborazione);
		}
		else {
			fileToSave = Utility.getErrorFile(elaborazione);
		}

		elaborazione.setPathFileEsito(fileToSave.getAbsolutePath());

//		// Elimina il file se esiste
//		if ( fileToSave.exists() ) {
//			logger.trace("File già salvato: " + fileToSave);
//			return;
//		}
				
		FileTransferActions ftActions = new FileTransferActions(Configurazione.getConfiguration());
		DownloadFileResponse downloadFileResp = ftActions.downloadFile(
				elaborazione.getFilename(), 
				elaborazione.getJobId(), 
				elaborazione.getFileReferenceId());
		
		if (downloadFileResp == null) {
			throw new EbayConnectorException("Errore nel download della risposta");
		}
		
		// Verifica la risposta
		check(downloadFileResp);
		
		FileAttachment attachment = downloadFileResp.getFileAttachment();
		DataHandler dh = attachment.getData();
		
		try {
			
			InputStream in = dh.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(in);

			FileOutputStream fo = new FileOutputStream(fileToSave);
			BufferedOutputStream bos = new BufferedOutputStream(fo);
			int bytes_read = 0;
			byte[] dataBuf = new byte[4096];
			while ((bytes_read = bis.read(dataBuf)) != -1) {
				bos.write(dataBuf, 0, bytes_read);
			}
			bis.close();
			bos.flush();
			bos.close();

		} catch (IOException e) {
			logger.error("Errore nel salvataggio del report", e);
		}
		
		logger.info("File di risposta salvato correttamente in: " + fileToSave);

	}
		
	private String getJobTypeFromXML(File file) throws EbayConnectorException {
		CreateLMSParser parser = new CreateLMSParser();
		boolean parseOk = parser.parse(file);
		if (!parseOk) {
			logger.error("Impossibile effettuare il parsing del file: " + file.getName());
			throw new EbayConnectorException("Impossibile effettuare il parsing del file: " + file.getName());
		}
		// extract the JObType String successfully
		String jobType = parser.getJobType();
		if (jobType == null) {
			logger.error("Il tipo di job del file non è valido");
			throw new EbayConnectorException("Il tipo di job del file non è valido");
		}
		
		logger.info("Il tipo di job del file '" + file.getName() + "' è: " + jobType);
		return jobType;
	}

	private void check(BaseServiceResponse response) throws EbayConnectorException {
		ErrorMessage errorMsg = null;
		if (!response.getAck().equals(AckValue.SUCCESS)) {
			errorMsg = new ErrorMessage();
			if (errorMsg != null) {
				ErrorData error = response.getErrorMessage().getError().get(0);
				if (response.getAck().equals(AckValue.WARNING)) {
					throw new EbayConnectorException(error.getSeverity() + " --> ErrorID=" + error.getErrorId() + " ; ErrorMessage=\"" + error.getMessage() + "\"");
				} else if (response.getAck().equals(AckValue.FAILURE)
						|| response.getAck().equals(AckValue.PARTIAL_FAILURE)) {
					throw new EbayConnectorException(error.getSeverity() + " --> ErrorID=" + error.getErrorId() + " ; ErrorMessage=\"" + error.getMessage() + "\"");
				}
			}
		}
	}

	private JobProfile retrieveOneJobStatus(GetJobStatusResponse jobStatusResp) {
		JobProfile job = null;
		if (jobStatusResp != null) {
			List<JobProfile> listOfJobs = jobStatusResp.getJobProfile();
			if (listOfJobs.size() == 1) {
				Iterator<JobProfile> itr = listOfJobs.iterator();
				job = (JobProfile) itr.next();
			}
		}
		return job;
	}
	
}
