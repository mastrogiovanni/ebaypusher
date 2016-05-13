package it.ebaypusher;

import java.sql.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import it.ebaypusher.dao.SnzhElaborazioniebay;

public class JPATest {

	private EntityManager manager;

	public JPATest(EntityManager manager) {
		this.manager = manager;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("persistenceUnit");
		EntityManager manager = factory.createEntityManager();
		JPATest test = new JPATest(manager);

		EntityTransaction tx = manager.getTransaction();
		tx.begin();
		try {
			test.createEmployees();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tx.commit();

		test.listEmployees();

		System.out.println(".. done");
	}

	private void createEmployees() {
		int numOfEmployees = manager.createQuery("Select a From SnzhElaborazioniebay a", SnzhElaborazioniebay.class).getResultList().size();
		if (numOfEmployees == 0) {
			SnzhElaborazioniebay elaborazione = new SnzhElaborazioniebay();
			elaborazione.setSnzhJobid("jobid");
			elaborazione.setSnzhDatainserimento(new Date(System.currentTimeMillis()));
			elaborazione.setSnzhJobtype("type");
			elaborazione.setSnzhFilereferenceid("reference");
			elaborazione.setSnzhFasejob("fase");
			elaborazione.setSnzhFilename("filename");
			elaborazione.setSnzhJobstatus("stato");
			elaborazione.setSnzhJobperccompl(1);
			elaborazione.setSnzhPathfileinput("input");
			elaborazione.setSnzhStatojob("statojob");
			manager.persist(elaborazione);
		}
	}

	private void listEmployees() {
		List<SnzhElaborazioniebay> resultList = manager.createQuery("Select a From SnzhElaborazioniebay a", SnzhElaborazioniebay.class).getResultList();
		System.out.println("num of employess:" + resultList.size());
		for (SnzhElaborazioniebay next : resultList) {
			System.out.println("next employee: " + next);
		}
	}

}
