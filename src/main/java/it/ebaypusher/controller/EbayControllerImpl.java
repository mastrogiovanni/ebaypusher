package it.ebaypusher.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.AbortJobResponse;
import com.ebay.marketplace.services.AckValue;
import com.ebay.marketplace.services.BaseServiceResponse;
import com.ebay.marketplace.services.BulkDataExchangeService;
import com.ebay.marketplace.services.CreateUploadJobResponse;
import com.ebay.marketplace.services.DownloadFileResponse;
import com.ebay.marketplace.services.ErrorData;
import com.ebay.marketplace.services.ErrorMessage;
import com.ebay.marketplace.services.FileAttachment;
import com.ebay.marketplace.services.GetJobStatusResponse;
import com.ebay.marketplace.services.GetJobsResponse;
import com.ebay.marketplace.services.JobProfile;
import com.ebay.marketplace.services.JobStatus;
import com.ebay.marketplace.services.StartDownloadJobResponse;
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
	public boolean test() {
		try {
			new BulkDataExchangeService();
			logger.info("Connection to ebay setup");
			return true;
		}
		catch (Throwable t) {
			logger.error("Ebay is not accessible");
		}
		return false;
	}

	@Override
	public void create(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {

		// Cattura il tipo di job
		String jobType = getJobTypeFromXML(new File(elaborazione.getPathFileInput()));

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

		logger.info("Inizio upload file: " + Utility.getFileLabel(elaborazione.getPathFileInput()));

		FileTransferActions ftActions = new FileTransferActions(Configurazione.getConfiguration());

		UploadFileResponse uploadFileResp = ftActions.uploadFile2(
				elaborazione.getPathFileInput(), 
				elaborazione.getJobId(), 
				elaborazione.getFileReferenceId());

		if (uploadFileResp == null) {
			throw new EbayConnectorException("Impossibile effettuare l'upload del job: risposta vuota");
		}

		check(uploadFileResp);

		logger.info("File trasferito con successo: " + Utility.getFileLabel(elaborazione.getPathFileInput()));

	}
	
	@Override
	public void startActiveInventoryReport(SnzhElaborazioniebay elaborazione) throws Exception {
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
		StartDownloadJobResponse response = bdeActions.startDownloadJob("ActiveInventoryReport", null);
		check(response);
		elaborazione.setJobType("ActiveInventoryReport");
		elaborazione.setJobId(response.getJobId());
		logger.info("Inventory Report richiesto con successo: " + elaborazione.getJobId());
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
	public JobProfile getJobProfile(String jobId) throws EbayConnectorException {
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
		GetJobStatusResponse getJobStatusResp = bdeActions.getJobStatus(jobId);
		check(getJobStatusResp);
		JobProfile jobProfile = retrieveOneJobStatus(getJobStatusResp);
		if ( jobProfile == null ) {
			throw new EbayConnectorException("Job profile not found: " + jobId);
		}
		return jobProfile;
	}

	@Override
	public void killAll() throws EbayConnectorException {
		try {
			BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
			GetJobsResponse response = bdeActions.getJobs(null);
			for (JobProfile profile : response.getJobProfile()) {
				if ( JobStatus.CREATED.equals(profile.getJobStatus())) {
					abort(profile.getJobId());
					logger.info("Killed job: " + profile.getJobId());
				}
			}
		}
		catch (Throwable e) {
			throw new EbayConnectorException("Errore nel killAll: " + e.getMessage());
		}
	}
	
	@Override
	public boolean downloadResponse(String jobId, File output) throws EbayConnectorException {
		
		BulkDataExchangeActions bdeActions = new BulkDataExchangeActions(Configurazione.getConfiguration());
        GetJobStatusResponse getJobStatusResp = bdeActions.getJobStatus(jobId);
        check(getJobStatusResp);
        JobProfile job = retrieveOneJobStatus(getJobStatusResp);
		
		FileTransferActions ftActions = new FileTransferActions(Configurazione.getConfiguration());
		
		DownloadFileResponse downloadFileResp = ftActions.downloadFile(
				jobId, 
				job.getFileReferenceId());

		if (downloadFileResp == null) {
			return false;
		}
		
		// Verifica la risposta
		check(downloadFileResp);

		FileAttachment attachment = downloadFileResp.getFileAttachment();
		
		DataHandler dh = attachment.getData();

		InputStream in = null;

		try {
			
			in = dh.getInputStream();
			
			// Wrap zip file
			ZipInputStream zipFile = new ZipInputStream(in);
			
			ZipEntry entry = null;
			
			int count = 0;

			// Tenta di aprire il primo file nello zip
	        while ((entry = zipFile.getNextEntry()) != null) {
	        	
	        	count ++;
	        	
				FileOutputStream fo = new FileOutputStream(output);
				BufferedOutputStream bos = new BufferedOutputStream(fo);
				int bytes_read = 0;
				byte[] dataBuf = new byte[4096];
				while ((bytes_read = zipFile.read(dataBuf)) != -1) {
					bos.write(dataBuf, 0, bytes_read);
				}
				
				bos.flush();
				bos.close();
				
	        }

	        // Tenta di leggere direttamente il file
	        if ( count == 0 ) {
	        	
	        	FileOutputStream fo = new FileOutputStream(output);
				BufferedOutputStream bos = new BufferedOutputStream(fo);
				int bytes_read = 0;
				byte[] dataBuf = new byte[4096];
				while ((bytes_read = in.read(dataBuf)) != -1) {
					bos.write(dataBuf, 0, bytes_read);
				}
				
				bos.flush();
				bos.close();
				
	        }

			logger.info("File di esito salvato correttamente: " + Utility.getFileLabel(output.getAbsolutePath()));

	        return true;

		} catch (IOException e) {
			logger.error("Errore mentre si cercava di salvare il file di esito");
		}
		finally {
			if ( in != null ) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		
		return false;
		
	}

	@Override
	public boolean saveResponseFile(SnzhElaborazioniebay elaborazione) throws EbayConnectorException {

		File fileToSave = null;

		if ( Stato.TERMINATO_CON_SUCCESSO.toString().equals(elaborazione.getFaseJob()) ) {
			fileToSave = Utility.getReportFile(elaborazione);
		}
		else {
			fileToSave = Utility.getErrorFile(elaborazione);
		}

		elaborazione.setPathFileEsito(fileToSave.getAbsolutePath());

		// Elimina il file se esiste
		if ( fileToSave.exists() ) {
			logger.trace("Rimuove file vecchio: " + fileToSave);
			fileToSave.delete();
		}

		return downloadResponse(
				elaborazione.getJobId(),
				new File(fileToSave.getAbsolutePath()));

	}

	@Override
	public String getJobTypeFromXML(File file) throws EbayConnectorException {
		
		CreateLMSParser parser = new CreateLMSParser();
		
		try {
			
			parser.parse(file);

		} catch (Throwable t) {
			logger.error("Impossibile effettuare il parsing del file: " + file.getName());
			throw new EbayConnectorException("Impossibile effettuare il parsing del file: " + file.getName(), t);
		}

		String jobType = parser.getJobType();
		if (jobType == null) {
			logger.error("Il tipo di job del file non è valido");
			throw new EbayConnectorException("Il tipo di job del file non è valido");
		}
		
		return jobType;

	}

	private void check(BaseServiceResponse response) throws EbayConnectorException {
		ErrorMessage errorMsg = null;
		if (!response.getAck().equals(AckValue.SUCCESS)) {
			errorMsg = new ErrorMessage();
			if (errorMsg != null) {
				ErrorData error = response.getErrorMessage().getError().get(0);
				if (response.getAck().equals(AckValue.WARNING)) {
					throw new EbayConnectorException(error);
				} else if (response.getAck().equals(AckValue.FAILURE)
						|| response.getAck().equals(AckValue.PARTIAL_FAILURE)) {
					throw new EbayConnectorException(error);
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
