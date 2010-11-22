package project.persistence.builder.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import project.client.persistence.Message;
import project.client.persistence.MessageBoard;
import project.client.persistence.MessageThread;
import project.core.persistence.PersistenceLoader;
import project.persistence.builder.MessageThreadCrawler;
import project.utils.statistics.MessageSimilarity;
import project.utils.statistics.impl.MessageSimilarity_Impl;

public class Test {
	
	private static final float MIN_SHOW_COEF = 0.025f;
	
	private class MessageSimilarityToken {
		
		private static final float MIN_COEF = 0.001f;
		
		private Message m1, m2;
		private int idxM1, idxM2;
		private float coefficient;
		public Message getM1() {
			return m1;
		}
		public void setM1(Message m1) {
			this.m1 = m1;
		}
		public Message getM2() {
			return m2;
		}
		public void setM2(Message m2) {
			this.m2 = m2;
		}
		public float getCoefficient() {
			return coefficient;
		}
		public void setCoefficient(float coefficient) {
			this.coefficient = coefficient;
			if (this.coefficient < MIN_COEF) {
				this.coefficient = 0.0f;
			}
		}
		public int getIdxM1() {
			return idxM1;
		}
		public void setIdxM1(int idxM1) {
			this.idxM1 = idxM1;
		}
		public int getIdxM2() {
			return idxM2;
		}
		public void setIdxM2(int idxM2) {
			this.idxM2 = idxM2;
		}
	}
	
	private Connection connection = null;
	
	public List<Message> loadMessages(int minId, int maxId) throws SQLException {

		List<Message> messages = new LinkedList<Message> ();

		// prepare the statement
		// 
		String query = "select * from Message where id >= ? and id <= ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, minId);
		statement.setInt(2, maxId);
		
		ResultSet set = statement.executeQuery();
		
		while (set.next()) {
			
			Message msg = new Message ();
			msg.setId(set.getInt("id"));
			msg.setContent(set.getString("content"));
			msg.setFormattedContent(set.getString("formatted_content"));
			msg.setPublishDate(set.getString("publishDate"));
			msg.setUrl(set.getString("url"));
			
			messages.add(msg);			
		}
		
		set.close();
		statement.close();
		
		return messages;		
	}	
	
	public Test () {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + "bachelor_project", 
					 "ebas", "gwtebas");			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<Message> messages = null;
		try {
			messages = this.loadMessages(0, 1000);
		} catch (Exception e) {}

		Iterator<Message> i = messages.iterator();
		while (i.hasNext()) {
			Message msg = i.next();
			if (msg.getFormattedContent().length() < 10) {
				i.remove();
			}
		}

		float minCoef =  10000000.0f;
		float maxCoef = -1000000.0f;
		List<MessageSimilarityToken> tokens = new LinkedList<MessageSimilarityToken> ();
		MessageSimilarity similarity = new MessageSimilarity_Impl ();
		
		for (int j = 0; j < messages.size() - 1; j++) {
			for (int k = j + 1; k < messages.size(); k++) {
				MessageSimilarityToken token = new MessageSimilarityToken ();
				token.setM1(messages.get(j));
				token.setM2(messages.get(k));
				token.setIdxM1(j);
				token.setIdxM2(k);
				token.setCoefficient(similarity.sim3(token.getM1(), token.getM2()));
				if (token.getCoefficient() < minCoef) {
					minCoef = token.getCoefficient();
				}
				if (token.getCoefficient() > maxCoef) {
					maxCoef = token.getCoefficient();
				}
			
				tokens.add(token);
			}
		}
		
		Collections.sort(tokens, new Comparator<MessageSimilarityToken> () {
						public int compare (MessageSimilarityToken t1, MessageSimilarityToken t2) {
							
							if (t1.getCoefficient() == t2.getCoefficient())
								return 0;
							
							if (t1.getCoefficient() < t2.getCoefficient())
								return 1;
							
							return -1;
						}
		});
		
		
		Iterator<MessageSimilarityToken> z = tokens.iterator();
		while (z.hasNext()) {
			MessageSimilarityToken to = z.next();
			String[] tokens1 = to.getM1().getFormattedContent().split(";");
			String[] tokens2 = to.getM2().getFormattedContent().split(";");
			float coef = (to.getCoefficient() - minCoef) / (maxCoef - minCoef);
			//float coef = to.getCoefficient();
			if (coef > MIN_SHOW_COEF) {
			System.out.println (to.getIdxM1() + " (" + tokens1.length + ")" + 
					            "<-> " + 
					            to.getIdxM2() + " (" + tokens2.length + ") " + 
					            "sim " + coef);
			
			
			for (int z1 = 0; z1 < tokens1.length; z1 ++) {
				System.out.print (tokens1 [z1] + ";");
			}
			System.out.println ();
			for (int z1 = 0; z1 < tokens2.length; z1 ++) {
				System.out.print (tokens2 [z1] + ";");
			}
			System.out.println ();
			}	
		}
		
	}
	
	public static void main (String[] args) {
		//Test t = new Test ();
		
		String str = "external:2;change:1;creates:1;manage:1;help:1;time:1;says:2;host:1;regards:1;create:1;";
		String[] tokens = str.split("[;:]");
		System.out.println ("# tokens = " + tokens.length);
	}
}
