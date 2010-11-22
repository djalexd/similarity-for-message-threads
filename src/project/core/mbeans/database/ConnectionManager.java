package project.core.mbeans.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.RemoteBinding;

public class ConnectionManager implements ConnectionManagerMBean {
	
	static {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String username = null;
	private String password = null;
	private String database = null;
	
	private transient Connection conn = null;
	
	public Connection getConnection() {
		
		return conn;
		
	}

	
	public void setConnectionParams(String user, String password,
			String database) {
		
		this.username = user;
		this.password = password;
		this.database = database;
		
		try {
			
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + this.database, 
													 this.username, this.password);
			
		} catch (Exception e) {
			e.printStackTrace(); // TODO log this
		}
	}
		
}
