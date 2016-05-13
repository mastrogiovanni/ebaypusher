package it.ebaypusher.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;

import it.ebaypusher.constants.Stato;

/**
 * File da elaborare per ebay
 * 
 * @author Michele Mastrogiovanni
 */
public class ElaborazioniEbay {

	private BigDecimal id;

	private Stato stato;

	private String fileName;

	// id job ebay
	private String idJobEbay;

	// File reference if
	private String idFile;

	private BigDecimal avanzamento;

	private Timestamp dataOraInvio;

	private String descrizioneErrore;

	private Timestamp dataOraErrore;

	private int tentativiDiInvio;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getIdJobEbay() {
		return idJobEbay;
	}

	public void setIdJobEbay(String idJobEbay) {
		this.idJobEbay = idJobEbay;
	}

	public String getIdFile() {
		return idFile;
	}

	public void setIdFile(String idFile) {
		this.idFile = idFile;
	}

	public Timestamp getDataOraInvio() {
		return dataOraInvio;
	}

	public void setDataOraInvio(Timestamp dataOraInvio) {
		this.dataOraInvio = dataOraInvio;
	}

	public BigDecimal getId() {
		return id;
	}

	public void setId(BigDecimal id) {
		this.id = id;
	}

	public void setAvanzamento(BigDecimal avanzamento) {
		this.avanzamento = avanzamento;
	}

	public BigDecimal getAvanzamento() {
		return avanzamento;
	}

	public String getDescrizioneErrore() {
		return descrizioneErrore;
	}

	public void setDescrizioneErrore(String descrizioneErrore) {
		this.descrizioneErrore = descrizioneErrore;
	}

	public Timestamp getDataOraErrore() {
		return dataOraErrore;
	}

	public void setDataOraErrore(Timestamp dataOraErrore) {
		this.dataOraErrore = dataOraErrore;
	}

	public int getTentativiDiInvio() {
		return tentativiDiInvio;
	}

	public void setTentativiDiInvio(int tentativiDiInvio) {
		this.tentativiDiInvio = tentativiDiInvio;
	}

	public Stato getStato() {
		return stato;
	}

	public void setStato(Stato stato) {
		this.stato = stato;
	}

	@Override
	public String toString() {
		return "ElaborazioniEbay [id=" + id + ", stato=" + stato + ", fileName=" + fileName + ", idJobEbay=" + idJobEbay + ", idFile=" + idFile + ", avanzamento=" + avanzamento + ", dataOraInvio="
				+ dataOraInvio + ", descrizioneErrore=" + descrizioneErrore + ", dataOraErrore=" + dataOraErrore + ", tentativiDiInvio=" + tentativiDiInvio + "]";
	}

}
