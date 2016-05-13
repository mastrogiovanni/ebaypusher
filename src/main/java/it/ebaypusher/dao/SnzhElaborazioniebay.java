package it.ebaypusher.dao;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the SNZH_ELABORAZIONIEBAY database table.
 */
@Entity
@Table(name="SNZH_ELABORAZIONIEBAY")
@NamedQuery(name="SnzhElaborazioniebay.findAll", query="SELECT s FROM SnzhElaborazioniebay s")
public class SnzhElaborazioniebay implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="SNZH_IDELABORAZIONE", unique=true, nullable=false)
	private String snzhIdelaborazione;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="SNZH_DATAELABORAZIONE")
	private Date snzhDataelaborazione;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="SNZH_DATAINSERIMENTO", nullable=false)
	private Date snzhDatainserimento;

	@Lob
	@Column(name="SNZH_ERROREJOB")
	private String snzhErrorejob;

	@Column(name="SNZH_FASEJOB", nullable=false, length=30)
	private String snzhFasejob;

	@Column(name="SNZH_FILENAME", nullable=false, length=80)
	private String snzhFilename;

	@Column(name="SNZH_FILEREFERENCEID", nullable=false, length=30)
	private String snzhFilereferenceid;

	@Column(name="SNZH_JOBID", nullable=false, length=30)
	private String snzhJobid;

	@Column(name="SNZH_JOBPERCCOMPL", nullable=false)
	private int snzhJobperccompl;

	@Column(name="SNZH_JOBSTATUS", nullable=false, length=15)
	private String snzhJobstatus;

	@Column(name="SNZH_JOBTYPE", nullable=false, length=30)
	private String snzhJobtype;

	@Lob
	@Column(name="SNZH_PATHFILEESITO")
	private String snzhPathfileesito;

	@Lob
	@Column(name="SNZH_PATHFILEINPUT", nullable=false)
	private String snzhPathfileinput;

	@Column(name="SNZH_STATOJOB", nullable=false, length=30)
	private String snzhStatojob;

	public SnzhElaborazioniebay() {
	}

	public String getSnzhIdelaborazione() {
		return this.snzhIdelaborazione;
	}

	public void setSnzhIdelaborazione(String snzhIdelaborazione) {
		this.snzhIdelaborazione = snzhIdelaborazione;
	}

	public Date getSnzhDataelaborazione() {
		return this.snzhDataelaborazione;
	}

	public void setSnzhDataelaborazione(Date snzhDataelaborazione) {
		this.snzhDataelaborazione = snzhDataelaborazione;
	}

	public Date getSnzhDatainserimento() {
		return this.snzhDatainserimento;
	}

	public void setSnzhDatainserimento(Date snzhDatainserimento) {
		this.snzhDatainserimento = snzhDatainserimento;
	}

	public String getSnzhErrorejob() {
		return this.snzhErrorejob;
	}

	public void setSnzhErrorejob(String snzhErrorejob) {
		this.snzhErrorejob = snzhErrorejob;
	}

	public String getSnzhFasejob() {
		return this.snzhFasejob;
	}

	public void setSnzhFasejob(String snzhFasejob) {
		this.snzhFasejob = snzhFasejob;
	}

	public String getSnzhFilename() {
		return this.snzhFilename;
	}

	public void setSnzhFilename(String snzhFilename) {
		this.snzhFilename = snzhFilename;
	}

	public String getSnzhFilereferenceid() {
		return this.snzhFilereferenceid;
	}

	public void setSnzhFilereferenceid(String snzhFilereferenceid) {
		this.snzhFilereferenceid = snzhFilereferenceid;
	}

	public String getSnzhJobid() {
		return this.snzhJobid;
	}

	public void setSnzhJobid(String snzhJobid) {
		this.snzhJobid = snzhJobid;
	}

	public int getSnzhJobperccompl() {
		return this.snzhJobperccompl;
	}

	public void setSnzhJobperccompl(int snzhJobperccompl) {
		this.snzhJobperccompl = snzhJobperccompl;
	}

	public String getSnzhJobstatus() {
		return this.snzhJobstatus;
	}

	public void setSnzhJobstatus(String snzhJobstatus) {
		this.snzhJobstatus = snzhJobstatus;
	}

	public String getSnzhJobtype() {
		return this.snzhJobtype;
	}

	public void setSnzhJobtype(String snzhJobtype) {
		this.snzhJobtype = snzhJobtype;
	}

	public String getSnzhPathfileesito() {
		return this.snzhPathfileesito;
	}

	public void setSnzhPathfileesito(String snzhPathfileesito) {
		this.snzhPathfileesito = snzhPathfileesito;
	}

	public String getSnzhPathfileinput() {
		return this.snzhPathfileinput;
	}

	public void setSnzhPathfileinput(String snzhPathfileinput) {
		this.snzhPathfileinput = snzhPathfileinput;
	}

	public String getSnzhStatojob() {
		return this.snzhStatojob;
	}

	public void setSnzhStatojob(String snzhStatojob) {
		this.snzhStatojob = snzhStatojob;
	}

}