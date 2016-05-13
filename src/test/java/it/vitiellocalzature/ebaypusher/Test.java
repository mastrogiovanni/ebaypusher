package it.vitiellocalzature.ebaypusher;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.UUID;

import it.ebaypusher.dao.Dao;
import it.ebaypusher.dao.ElaborazioniEbay;

public class Test {

	public static void main(String[] args) throws ClassNotFoundException, MalformedURLException {
		
		Dao dao = new Dao();
		
		ElaborazioniEbay e1 = dao.create(UUID.randomUUID().toString() + ".txt");
				
		System.out.println("Creato: " + e1);
		
		e1.setAvanzamento(new BigDecimal("50.34"));
		
		dao.update(e1);

		for ( ElaborazioniEbay e : dao.findAll()) {
			
			System.out.println(e);
			
		}


	}

}
