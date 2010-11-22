package project.persistence.properties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import project.client.persistence.Message;
import project.utils.statistics.ObjectLooseProperties;

public class MessageWithProperties extends Message {
	
	private ObjectLooseProperties properties;
	
	public MessageWithProperties () {
		// default constructor
		
		properties = new ObjectLooseProperties ();
		properties.setTable("Message");		
	}
	
	public MessageWithProperties (Message message) {
		
		//
		// default constructor call
		this ();
		
		//
		// perform a deep copy
		//
		if (message != null) {
			this.setContent(message.getContent());
			this.setFormattedContent(message.getFormattedContent());
			this.setId(message.getId());
			this.setMessageThread(message.getMessageThread());
			this.setParent(message.getParent());
			this.setPublishDate(message.getPublishDate());
			this.setUrl(message.getUrl());
			this.setUser(message.getUser());
		}
	}
	
	public String getProperty (String name) {
		return properties.getProperties().get(name);
	}
	
	public void setProperty (String name, String value) {
		properties.getProperties().put(name, value);
	}
	
	public ObjectLooseProperties getProperties () {
		return properties;
	}
	
	public boolean hasProperty (String name) {
		return properties.getProperties().containsKey(name);
	}
	
	public void saveProperties (Connection connection) 
						throws IllegalStateException, SQLException {
		
		if (this.getId() == null || this.getId() <= 0)
			throw new IllegalStateException ("Unable to save properties (unknown object with id <= 0)");
			
		if (properties == null) {
			properties = new ObjectLooseProperties ();
			properties.setTable("Message");		
		}
		
		properties.setId(this.getId());
		properties.saveToDatabase(connection);
	}

	
	public void loadProperties (Connection connection)
						throws IllegalStateException {

		if (this.getId() <= 0)
			throw new IllegalStateException ("Unable to save properties (unknown object with id <= 0)");
			
		if (properties == null) {
			properties = new ObjectLooseProperties ();
			properties.setTable("Message");		
		}
		
		properties.setId(this.getId());
		properties.loadFromDatabase(connection);
	}
	
	
	public void clearProperties (boolean autoCommit, Connection connection)
						throws IllegalStateException, SQLException {

		if (this.getId() <= 0)
			throw new IllegalStateException ("Unable to save properties (unknown object with id <= 0)");
			
		if (properties == null) {
			properties = new ObjectLooseProperties ();
			properties.setTable("Message");		
		}
		
		properties.setId(this.getId());		
		properties.getProperties().clear();
		
		if (autoCommit) {
			properties.clearFromDatabase(connection);
		}
	}
	
	
	public Map<String,Integer> getWordFrequency () {
		if (properties.getProperties().get("frequencies") == null) {
			//System.out.println ("No frequencies found for message : " + this.getUrl());
			return new HashMap<String, Integer> ();
		}
		
		Map<String,Integer> map = new HashMap<String, Integer> ();
		String frequencies = properties.getProperties().get("frequencies");
		if (frequencies.length() == 0)
			return new HashMap<String, Integer> ();
		
		String[] tokens = frequencies.split("[;:]");
		for (int i = 0; i < tokens.length; i += 2) {
			map.put(tokens [i + 0], new Integer (tokens [i + 1]));
		}
		
		return map;
	}

	
	
	public String toString() {
		return properties.toString();
	}
	
	public int getNumWords () {
		if (properties.getProperties().get("num-words") == null) {
			return 0;
		}
		
		return Integer.parseInt(properties.getProperties().get("num-words"));
	}
}
