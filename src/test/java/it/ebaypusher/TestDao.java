package it.ebaypusher;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.SnzhElaborazioniebay;
import it.ebaypusher.dao.SnzhEsitiebay;
import it.ebaypusher.utility.Configurazione;

public class TestDao {

	public static void main(String[] args) throws ClassNotFoundException {

		Properties props = Configurazione.getConfiguration();
		props.setProperty("eclipselink.ddl-generation", "create-tables");
		props.setProperty("eclipselink.ddl-generation.output-mode", "database");
		props.setProperty("eclipselink.logging.level", "SEVERE");

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("persistenceUnit", props);
		EntityManager manager = factory.createEntityManager();
		Dao dao = new Dao(manager);
		
		for ( SnzhElaborazioniebay e : dao.findAll()) {
			e.setEsitoParsed(false);
			dao.update(e);
			// System.out.println(e);
		}
		
		// dao.insert(elaborazione);

	}

}
