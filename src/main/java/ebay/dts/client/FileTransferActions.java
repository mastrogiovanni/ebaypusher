/**
 * © 2010-2013 eBay Inc., All Rights Reserved
 * Licensed under CDDL 1.0 -  http://opensource.org/licenses/cddl1.php
 */

package ebay.dts.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.marketplace.services.AckValue;
import com.ebay.marketplace.services.DownloadFileResponse;
import com.ebay.marketplace.services.FileAttachment;
import com.ebay.marketplace.services.FileTransferServicePort;
import com.ebay.marketplace.services.UploadFileRequest;
import com.ebay.marketplace.services.UploadFileResponse;

import it.ebaypusher.controller.EbayConnectorException;
import it.ebaypusher.utility.Utility;

/**
 *
 * @author zhuyang
 */
public class FileTransferActions {

	FileTransferCall call;

    private static Log logger = LogFactory.getLog(FileTransferActions.class);

	public FileTransferActions(Properties prop) {
		call = new FileTransferCall(prop.getProperty("fileTransferURL"), prop.getProperty("userToken"));
	}

	public void uploadFile(String xmlFile, String jobId, String fileReferenceId) throws EbayConnectorException {
		
		String callName = "uploadFile";
		
		try {

			String compressedFileName = compressFileToGzip(xmlFile);
			
			if (compressedFileName == null) {
				logger.error("Failed to compress your XML file into gzip file. Aborted");
				throw new EbayConnectorException("Failed to compress your XML file into gzip file. Aborted");
			}
			
			FileTransferServicePort port = call.setFTSMessageContext(callName);
			UploadFileRequest request = new UploadFileRequest();
			FileAttachment attachment = new FileAttachment();
			File fileToUpload = new File(compressedFileName);
			DataHandler dh = new DataHandler(new FileDataSource(fileToUpload));
			attachment.setData(dh);
			attachment.setSize(fileToUpload.length());
			String fileFormat = "gzip";
			request.setFileFormat(fileFormat);
			
			/*
			 * For instance, the Bulk Data Exchange Service uses a job ID as a
			 * primary identifier, so, if you're using the Bulk Data Exchange
			 * Service, enter the job ID as the taskReferenceId.
			 */

			request.setTaskReferenceId(jobId);
			request.setFileReferenceId(fileReferenceId);
			request.setFileAttachment(attachment);
			// request.
			if (port != null && request != null) {
				UploadFileResponse response = port.uploadFile(request);
				if (response.getAck().equals(AckValue.SUCCESS)) {
					return;
				} else {
					logger.error(response.getErrorMessage().getError().get(0).getMessage());
					throw new EbayConnectorException(response.getErrorMessage().getError().get(0).getMessage());
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new EbayConnectorException(e.getMessage());
		}
				
	}

	public UploadFileResponse uploadFile2(String xmlFile, String jobId, String fileReferenceId) throws EbayConnectorException {
		
		String callName = "uploadFile";
		UploadFileResponse response = null;
		File fileToUpload = null;
		
		try {

			String compressedFileName = compressFileToGzip(xmlFile);
			if (compressedFileName == null) {
				logger.error("Failed to compress your XML file into gzip file. Aborted.");
				return null;
			}
			FileTransferServicePort port = call.setFTSMessageContext(callName);
			UploadFileRequest request = new UploadFileRequest();
			FileAttachment attachment = new FileAttachment();
			fileToUpload = new File(compressedFileName);
			DataHandler dh = new DataHandler(new FileDataSource(fileToUpload));
			attachment.setData(dh);
			attachment.setSize(fileToUpload.length());
			String fileFormat = "gzip";
			request.setFileFormat(fileFormat);
			/*
			 * For instance, the Bulk Data Exchange Service uses a job ID as a
			 * primary identifier, so, if you're using the Bulk Data Exchange
			 * Service, enter the job ID as the taskReferenceId.
			 */

			request.setTaskReferenceId(jobId);
			request.setFileReferenceId(fileReferenceId);
			request.setFileAttachment(attachment);
			// request.
			if (port != null && request != null) {
				response = port.uploadFile(request);
			}

			return response;

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new EbayConnectorException(e.getMessage(), e);
		}
		finally {
			if ( fileToUpload != null && fileToUpload.exists()) {
				fileToUpload.delete();
			}
		}
		
	}

	public void downloadFile2(String fileName, String jobId, String fileReferenceId) throws EbayConnectorException {

		String callName = "downloadFile";

		try {
			
			FileTransferServicePort port = call.setFTSMessageContext(callName);
			com.ebay.marketplace.services.DownloadFileRequest request = new com.ebay.marketplace.services.DownloadFileRequest();
			request.setFileReferenceId(fileReferenceId);
			request.setTaskReferenceId(jobId);
			DownloadFileResponse response = port.downloadFile(request);
			if (response.getAck().equals(AckValue.SUCCESS)) {
				logger.debug(AckValue.SUCCESS.toString());
			} else {
				logger.error(response.getErrorMessage().getError().get(0).getMessage());
				throw new EbayConnectorException(response.getErrorMessage().getError().get(0).getMessage());
			}
			
			FileAttachment attachment = response.getFileAttachment();
			DataHandler dh = attachment.getData();
			
			try {
				InputStream in = dh.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(new GZIPInputStream(in));
				FileOutputStream fo = new FileOutputStream(new File(fileName)); // "C:/myDownLoadFile.gz"
				BufferedOutputStream bos = new BufferedOutputStream(fo);
				int bytes_read = 0;
				byte[] dataBuf = new byte[4096];
				while ((bytes_read = bis.read(dataBuf)) != -1) {
					bos.write(dataBuf, 0, bytes_read);
				}
				bis.close();
				bos.flush();
				bos.close();
				logger.info("File attachment has been saved successfully to " + fileName);

			} catch (IOException e) {
				logger.error("Exception caught while trying to save the attachement");
				throw new EbayConnectorException("Exception caught while trying to save the attachement", e);
			}
			
		} catch (Exception e) {
			throw new EbayConnectorException("Exception caught while trying to save the attachement", e);
		}
		
	}

	public DownloadFileResponse downloadFile(String jobId, String fileReferenceId) throws EbayConnectorException {
		String callName = "downloadFile";
		FileTransferServicePort port = call.setFTSMessageContext(callName);
		com.ebay.marketplace.services.DownloadFileRequest request = new com.ebay.marketplace.services.DownloadFileRequest();
		request.setFileReferenceId(fileReferenceId);
		request.setTaskReferenceId(jobId);
		return port.downloadFile(request);
	}

	private static String compressFileToGzip(String inFilename) throws EbayConnectorException {
		
		// compress the xml file into gz file in the save folder
		String outFilename = null;
		String usingPath = inFilename.substring(0, inFilename.lastIndexOf(File.separator) + 1);
		String fileName = inFilename.substring(inFilename.lastIndexOf(File.separator) + 1);
		outFilename = usingPath + fileName + ".gz";

		try {
			
			BufferedReader in = new BufferedReader(new FileReader(inFilename));
			BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outFilename)));
			logger.trace("Writing gz file to: " + Utility.getFileLabel(outFilename));
			
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			in.close();
			out.close();
			
		} catch (FileNotFoundException e) {
			logger.error("Cannot gzip file: " + inFilename);
			throw new EbayConnectorException("Cannot gzip file: " + inFilename, e);
		} catch (IOException e) {
			logger.error("Cannot gzip file: " + inFilename);
			throw new EbayConnectorException("Cannot gzip file: " + inFilename, e);
		}
		
		logger.info("The compressed file has been saved to " + outFilename);
		return outFilename;
	}

}
