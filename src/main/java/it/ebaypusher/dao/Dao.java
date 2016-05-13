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
	
	// private String jdbcURL;
	
	public Dao(EntityManager manager) throws ClassNotFoundException {
		
		this.manager = manager;
		this.tx = new ThreadLocal<EntityTransaction>();
		
//		// Register JDBC driver
//		Class.forName(Configurazione.getText(Configurazione.DB_DRIVER));
//
//		StringBuilder builder = new StringBuilder();
//		builder.append("jdbc:mysql://");
//		builder.append(Configurazione.getText(Configurazione.DB_HOST));
//		builder.append(":");
//		builder.append(Configurazione.getIntValue(Configurazione.DB_PORT, 3306));
//		builder.append("/");
//		builder.append(Configurazione.getText(Configurazione.DB_SCHEMA));
//		
//		jdbcURL = builder.toString();
		
	}
	
	public void begin() {
		tx.set(manager.getTransaction());
		tx.get().begin();
	}
	
	public void commit() {
		tx.get().commit();
	}
	
	public void rollback() {
		tx.get().rollback();
	}
	
	public void update(SnzhElaborazioniebay elaborazione) {
		manager.merge(elaborazione);
		
//		Connection conn = null;
//
//		PreparedStatement pstmt = null;
//		
//		try {
//
//			// Open a connection
//			conn = open();
//
//			StringBuilder updateQuery = new StringBuilder();
//			updateQuery.append("UPDATE " + getTableName() + " ");
//			updateQuery.append("SET ");
//			updateQuery.append("stato = ?, ");
//			updateQuery.append("file_name = ?, ");
//			updateQuery.append("id_job_ebay = ?, ");
//			updateQuery.append("id_file = ?, ");
//			updateQuery.append("avanzamento = ?, ");
//			updateQuery.append("data_ora_invio = ?, ");
//			updateQuery.append("descrizione_errore = ?, ");
//			updateQuery.append("data_ora_errore = ?, ");
//			updateQuery.append("tentativi_invio = ? ");
//			updateQuery.append("WHERE id = ?");
//
//			pstmt = conn.prepareStatement(updateQuery.toString());
//			pstmt.setString(1, elaborazione.getStato().toString());
//			pstmt.setString(2, elaborazione.getFileName());
//			pstmt.setString(3, elaborazione.getIdJobEbay());
//			pstmt.setString(4, elaborazione.getIdFile());
//			pstmt.setBigDecimal(5, elaborazione.getAvanzamento());
//			pstmt.setTimestamp(6, elaborazione.getDataOraInvio());
//			pstmt.setString(7, elaborazione.getDescrizioneErrore());
//			pstmt.setTimestamp(8, elaborazione.getDataOraErrore());
//			pstmt.setInt(9, elaborazione.getTentativiDiInvio());
//
//			pstmt.setBigDecimal(10, elaborazione.getId());
//
//			pstmt.addBatch();
//			return pstmt.execute();
//			
//		} catch (SQLException se) {
//			// Handle errors for JDBC
//			se.printStackTrace();
//		} catch (Exception e) {
//			// Handle errors for Class.forName
//			e.printStackTrace();
//		} finally {
//			// finally block used to close resources
//			try {
//				if (pstmt != null)
//					pstmt.close();
//			} catch (SQLException se2) {
//			}
//			try {
//				if (conn != null)
//					conn.close();
//			} catch (SQLException se) {
//				se.printStackTrace();
//			}
//		}
//
//		return false;
		
	}
	
	/**
	 * Crea una elaborazione per quel fileName.
	 * @param fileName
	 * @return
	 */
	public void insert(SnzhElaborazioniebay elaborazione) {
		
		manager.persist(elaborazione);

//		Connection conn = null;
//
//		PreparedStatement pstmt = null;
//		
//		ElaborazioniEbay elaborazione = new ElaborazioniEbay();
//		elaborazione.setFileName(fileName);
//		elaborazione.setStato();
//
//		try {
//
//			// Open a connection
//			conn = open();
//						
//			String insertSQL = "INSERT INTO " + getTableName() + " (stato, file_name) values (?, ?)";
//			pstmt = conn.prepareStatement(insertSQL); // , new String[]{"id"});
//			pstmt.setString(1, elaborazione.getStato().toString());
//			pstmt.setString(2, elaborazione.getFileName());
//			pstmt.addBatch();
//			pstmt.execute();
//			ResultSet rs = pstmt.getGeneratedKeys();
//			while ( rs.next() ) {
//				elaborazione.setId(rs.getBigDecimal("GENERATED_KEY"));
//				return elaborazione;
//			}
//			
//		} catch (SQLException se) {
//			// Handle errors for JDBC
//			se.printStackTrace();
//		} catch (Exception e) {
//			// Handle errors for Class.forName
//			e.printStackTrace();
//		} finally {
//			// finally block used to close resources
//			try {
//				if (pstmt != null)
//					pstmt.close();
//			} catch (SQLException se2) {
//			}
//			try {
//				if (conn != null)
//					conn.close();
//			} catch (SQLException se) {
//				se.printStackTrace();
//			}
//		}
//
//		return null;
		
	}
		
	public List<SnzhElaborazioniebay> findAll() {
		
		return manager.createQuery("Select a From SnzhElaborazioniebay a", SnzhElaborazioniebay.class).getResultList();

//		Connection conn = null;
//
//		Statement stmt = null;
//		
//		List<ElaborazioniEbay> result = new ArrayList<ElaborazioniEbay>();
//		
//		try {
//
//			// Open a connection
//			conn = open();
//			
//			// Execute a query
//			logger.trace("Creating statement...");
//			stmt = conn.createStatement();
//						
//			ResultSet rs = stmt.executeQuery("SELECT * FROM " + getTableName());
//
//			// Extract data from result set
//			while (rs.next()) {
//				
//				ElaborazioniEbay item = new ElaborazioniEbay();
//				
//				item.setId(rs.getBigDecimal("id"));
//				item.setStato(Stato.valueOf(rs.getString("stato")));
//				item.setFileName(rs.getString("file_name"));
//				item.setIdJobEbay(rs.getString("id_job_ebay"));
//				item.setIdFile(rs.getString("id_file"));
//				item.setAvanzamento(rs.getBigDecimal("avanzamento"));
//				item.setDataOraInvio(rs.getTimestamp("data_ora_invio"));
//				item.setDescrizioneErrore(rs.getString("descrizione_errore"));
//				item.setDataOraErrore(rs.getTimestamp("data_ora_errore"));
//				item.setTentativiDiInvio(rs.getInt("tentativi_invio"));
//				
//				result.add(item);
//
//			}
//
//			// STEP 6: Clean-up environment
//			rs.close();
//			stmt.close();
//			conn.close();
//
//		} catch (SQLException se) {
//			// Handle errors for JDBC
//			se.printStackTrace();
//		} catch (Exception e) {
//			// Handle errors for Class.forName
//			e.printStackTrace();
//		} finally {
//			// finally block used to close resources
//			try {
//				if (stmt != null)
//					stmt.close();
//			} catch (SQLException se2) {
//			}
//			try {
//				if (conn != null)
//					conn.close();
//			} catch (SQLException se) {
//				se.printStackTrace();
//			}
//		}
//		
//		return result;

	}
	
//	private Connection open() throws SQLException {
//		logger.trace("Connecting to database: " + jdbcURL);
//		String username = Configurazione.getText(Configurazione.DB_USERNAME);
//		String password = Configurazione.getText(Configurazione.DB_PASSWORD);
//		if ( username == null || password == null ) {
//			return DriverManager.getConnection(jdbcURL);
//		}
//		return DriverManager.getConnection(jdbcURL, username, password);
//	}
	
	private String getTableName() {
		return Configurazione.getText(Configurazione.DB_TABLE);
	}

}
