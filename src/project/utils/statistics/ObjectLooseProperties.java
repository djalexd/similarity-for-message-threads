package project.utils.statistics;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ObjectLooseProperties implements Serializable {
	
	private String table;
	private int id;
	
	private Map<String, String> properties;
	
	public ObjectLooseProperties () {
		properties = new HashMap<String, String> ();
	}
	
	public ObjectLooseProperties (String table, int id) {
		this ();
		this.setTable(table);
		this.setId(id);
	}
	
	public ObjectLooseProperties (String table) {
		this ();
		this.setTable(table);
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public void loadFromDatabase (Connection conn) {

		try {
			String query = "select * from settings where tableName = ? and tableID = ?";
			PreparedStatement statement = conn.prepareStatement(query);

			statement.setString(1, this.getTable());
			statement.setInt(2, this.getId());

			properties.clear();		
			ResultSet set = statement.executeQuery();
			while (set.next()) {

				properties.put(set.getString("propertyKey"), set.getString("propertyValue"));

			}

			set.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveToDatabase (Connection conn) 
						throws SQLException {
		
		String query = "select * from settings where tableName = ? and tableID = ? and propertyKey = ?";
		PreparedStatement statement = conn.prepareStatement(query);
		
		statement.setString(1, this.getTable());
		statement.setInt(2, this.getId());

		Iterator<String> i = properties.keySet().iterator();
		while (i.hasNext()) {
			
			String key = i.next();
			statement.setString(3, key);
			
			ResultSet set = statement.executeQuery();
			if (!set.next()) {
				
				//
				// the property was not yet saved in database, perform an insert
				String insertQuery = "insert into settings(tableName,tableID,propertyKey,propertyValue) " +
				                     "values(?,?,?,?)";
				
				PreparedStatement s2 = conn.prepareStatement(insertQuery);
				s2.setString(1, getTable());
				s2.setInt(2, getId());
				s2.setString(3, key);
				s2.setString(4, properties.get(key));
				
				s2.execute();
				s2.close();
				
			} else {
				
				//
				// the property was saved in database, perform an update

				String updateQuery = "update settings set propertyValue = ?" +
									 "where id = ?";
				
				PreparedStatement s2 = conn.prepareStatement(updateQuery);
				s2.setString (1, properties.get(key));
				s2.setInt(2, set.getInt("id"));
				
				s2.execute();
				s2.close();				
			}

			set.close();
		}
		
		statement.close();
	}
	
	
	public void clearFromDatabase (Connection connection) 
						throws SQLException {
		
		String query = "delete from settings where tableName = ? and tableID = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		
		statement.setString(1, getTable());
		statement.setInt(2, getId());
		
		statement.execute();
		statement.close();
	}

	
	public String toString() {
		String str = this.getClass().getName() + "[\n";

		str += "table = " + this.getTable() + ", id = " + this.getId() + "\n,";
		Iterator<String> i = this.getProperties().keySet().iterator();
		while (i.hasNext()) {
			String nextKey = i.next();
			str += nextKey + " : " + this.getProperties().get(nextKey);
			if (i.hasNext())
				str += ",\n";
		}
		str += "]";
		return str;
	}
	
	public int getNumEntries (Connection connection, int propertyCount) 
					throws IllegalStateException, SQLException {
		
		if (this.getTable() == null)
			throw new IllegalStateException ("Table is not specified");
		
		Statement s0 = connection.createStatement();
		ResultSet set0 = s0.executeQuery("select count(*) from settings where tableName like '" + this.getTable() + "'");
		
		if (!set0.next()) {
			set0.close();
			s0.close();
			
			return 0;
		}
		
		int count = set0.getInt(1);
		
		set0.close();
		s0.close();
		
		return count / propertyCount;
	}
}
