package it.ebaypusher.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.ebaypusher.constants.Stato;
import it.ebaypusher.utility.Configurazione;

public class Dao {
	
	private Log logger = LogFactory.getLog(Dao.class);
	
	private EntityManager manager;
	
	private ThreadLocal<EntityTransaction> tx;
	
	public Dao(EntityManager manager) throws ClassNotFoundException {
		this.manager = manager;
		this.tx = new ThreadLocal<EntityTransaction>();
	}
	
	private void begin() {
		tx.set(manager.getTransaction());
		tx.get().begin();
	}
	
	private void commit() {
		tx.get().commit();
	}
	
//	private void rollback() {
//		tx.get().rollback();
//	}
	
	public void update(SnzhElaborazioniebay elaborazione) {
		begin();
		manager.merge(elaborazione);
		commit();
	}
	
	/**
	 * Crea una elaborazione per quel fileName.
	 * @param fileName
	 * @return
	 */
	public void insert(SnzhElaborazioniebay elaborazione) {
		begin();
		manager.persist(elaborazione);
		commit();
	}
		
	public List<SnzhElaborazioniebay> findAll() {
		return manager.createQuery("Select a From SnzhElaborazioniebay a", SnzhElaborazioniebay.class).getResultList();
	}
	
}
