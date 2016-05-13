package thresh;

import java.util.Properties;

public class BnConfigurazione {

	private String logging;
	private String getJobStatusQueryInterval;
	private String bulkDataExchangeURL;
	private String fileTransferURL;
	private String userToken;

	public BnConfigurazione(Properties prop) {
		logging = prop.getProperty("logging");
		getJobStatusQueryInterval = prop.getProperty("getJobStatusQueryInterval");
		bulkDataExchangeURL = prop.getProperty("bulkDataExchangeURL");
		fileTransferURL = prop.getProperty("fileTransferURL");
		userToken = prop.getProperty("userToken");
	}

//	public String getLogging() {
//		return logging;
//	}
//
//	public void setLogging(String logging) {
//		this.logging = logging;
//	}
//
//	public String getGetJobStatusQueryInterval() {
//		return getJobStatusQueryInterval;
//	}
//
//	public void setGetJobStatusQueryInterval(String getJobStatusQueryInterval) {
//		this.getJobStatusQueryInterval = getJobStatusQueryInterval;
//	}
//
//	public String getBulkDataExchangeURL() {
//		return bulkDataExchangeURL;
//	}
//
//	public void setBulkDataExchangeURL(String bulkDataExchangeURL) {
//		this.bulkDataExchangeURL = bulkDataExchangeURL;
//	}
//
//	public String getFileTransferURL() {
//		return fileTransferURL;
//	}
//
//	public void setFileTransferURL(String fileTransferURL) {
//		this.fileTransferURL = fileTransferURL;
//	}
//
//	public String getUserToken() {
//		return userToken;
//	}
//
//	public void setUserToken(String userToken) {
//		this.userToken = userToken;
//	}

//	public String getPathFileToSend() {
//		return pathFileToSend;
//	}
//
//	public void setPathFileToSend(String pathFileToSend) {
//		this.pathFileToSend = pathFileToSend;
//	}

}
