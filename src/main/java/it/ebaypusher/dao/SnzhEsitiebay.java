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
 * The persistent class for the SNZH_ESITIEBAY database table.
 * 
 */
@Entity
@Table(name = "SNZH_ESITIEBAY")
@NamedQuery(name = "SnzhEsitiebay.findAll", query = "SELECT s FROM SnzhEsitiebay s")
public class SnzhEsitiebay implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_ESITO")
	private String idEsito;
	
	@Column(name = "FK_ID_ELABORAZIONE")
	private String idElaborazione;
		
	@Column(name = "RESPONSE_TYPE")
	private String responseType;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATA_ESITO")
	private Date dataEsito;
	
	@Column(name = "ACK")
	private String ack;
	
	@Column(name = "SKU")
	private String sku;
	
	@Column(name = "ITEM_ID")
	private String itemId;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_TIME")
	private Date startTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_TIME")
	private Date endTime;

	@Lob
	@Column(name = "SEGNALAZIONI")
	private String segnalazioni;

	@Column(name = "CORRELATION_ID")
	private String correlationId;

	public SnzhEsitiebay() {
	}

	public String getIdEsito() {
		return idEsito;
	}

	public void setIdEsito(String idEsito) {
		this.idEsito = idEsito;
	}

	public String getIdElaborazione() {
		return idElaborazione;
	}

	public void setIdElaborazione(String idElaborazione) {
		this.idElaborazione = idElaborazione;
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public Date getDataEsito() {
		return dataEsito;
	}

	public void setDataEsito(Date dataEsito) {
		this.dataEsito = dataEsito;
	}

	public String getAck() {
		return ack;
	}

	public void setAck(String ack) {
		this.ack = ack;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getSegnalazioni() {
		return segnalazioni;
	}

	public void setSegnalazioni(String segnalazioni) {
		this.segnalazioni = segnalazioni;
	}

	@Override
	public String toString() {
		return "SnzhEsitiebay [idEsito=" + idEsito + ", idElaborazione=" + idElaborazione + ", responseType=" + responseType + ", dataEsito=" + dataEsito + ", ack=" + ack + ", sku=" + sku
				+ ", itemId=" + itemId + ", startTime=" + startTime + ", endTime=" + endTime + ", segnalazioni=" + segnalazioni + "]";
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}


}