package it.ebaypusher.dao;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the SNZH_ELABORAZIONIEBAY database table.
 * 
 */
@Entity
@Table(name = "SNZH_ELABORAZIONIEBAY")
@NamedQuery(name = "SnzhElaborazioniebay.findAll", query = "SELECT s FROM SnzhElaborazioniebay s")
public class SnzhElaborazioniebay implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID_ELABORAZIONE")
	private String idElaborazione;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATA_ELABORAZIONE")
	private Date dataElaborazione;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATA_INSERIMENTO")
	private Date dataInserimento;

	@Lob
	@Column(name = "ERRORE_JOB")
	private String erroreJob;

	@Column(name = "FASE_JOB")
	private String faseJob;

	@Column(name = "FILE_REFERENCE_ID")
	private String fileReferenceId;

	@Column(name = "FILENAME")
	private String filename;

	@Column(name = "JOB_ID")
	private String jobId;

	@Column(name = "JOB_PERC_COMPL")
	private int jobPercCompl;

	@Column(name = "JOB_STATUS")
	private String jobStatus;

	@Column(name = "JOB_TYPE")
	private String jobType;

	@Column(name = "NUM_TENTATIVI")
	private int numTentativi;

	@Lob
	@Column(name = "PATH_FILE_ESITO")
	private String pathFileEsito;

	@Lob
	@Column(name = "PATH_FILE_INPUT")
	private String pathFileInput;

	public SnzhElaborazioniebay() {
	}

	public String getIdElaborazione() {
		return this.idElaborazione;
	}

	public void setIdElaborazione(String idElaborazione) {
		this.idElaborazione = idElaborazione;
	}

	public Date getDataElaborazione() {
		return this.dataElaborazione;
	}

	public void setDataElaborazione(Date dataElaborazione) {
		this.dataElaborazione = dataElaborazione;
	}

	public Date getDataInserimento() {
		return this.dataInserimento;
	}

	public void setDataInserimento(Date dataInserimento) {
		this.dataInserimento = dataInserimento;
	}

	public String getErroreJob() {
		return this.erroreJob;
	}

	public void setErroreJob(String erroreJob) {
		this.erroreJob = erroreJob;
	}

	public String getFaseJob() {
		return this.faseJob;
	}

	public void setFaseJob(String faseJob) {
		this.faseJob = faseJob;
	}

	public String getFileReferenceId() {
		return this.fileReferenceId;
	}

	public void setFileReferenceId(String fileReferenceId) {
		this.fileReferenceId = fileReferenceId;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getJobId() {
		return this.jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public int getJobPercCompl() {
		return this.jobPercCompl;
	}

	public void setJobPercCompl(int jobPercCompl) {
		this.jobPercCompl = jobPercCompl;
	}

	public String getJobStatus() {
		return this.jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String getJobType() {
		return this.jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public int getNumTentativi() {
		return this.numTentativi;
	}

	public void setNumTentativi(int numTentativi) {
		this.numTentativi = numTentativi;
	}

	public String getPathFileEsito() {
		return this.pathFileEsito;
	}

	public void setPathFileEsito(String pathFileEsito) {
		this.pathFileEsito = pathFileEsito;
	}

	public String getPathFileInput() {
		return this.pathFileInput;
	}

	public void setPathFileInput(String pathFileInput) {
		this.pathFileInput = pathFileInput;
	}

}