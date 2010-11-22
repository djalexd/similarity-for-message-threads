package project.core.mbeans.database;

import java.sql.Connection;

public interface ConnectionManager {
		
	/**
	 * Sets new values (and inits a new connection)
	 * @param user
	 * @param password
	 * @param database
	 */
	public void setConnectionParams (String user, String password, String database);

	/**
	 * Returns a the opened SQL connection
	 * @return
	 */
	public Connection getConnection ();
}
