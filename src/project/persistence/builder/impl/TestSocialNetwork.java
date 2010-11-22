package project.persistence.builder.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import project.client.utils.FloatSparseMatrix;
import project.client.utils.SparseMatrix;
import project.core.mbeans.database.ConnectionManagerMysqlImpl;
import sun.security.krb5.internal.LocalSeqNumber;

public class TestSocialNetwork {

	private ConnectionManagerMysqlImpl manager = null;

	private int size;
	private float density, deviation;
	private FloatSparseMatrix connectivity;
	private Map<Integer,Integer> inDegree, outDegree;
	private Map<Integer,Float> localDensity;
	private Map<Integer,Float> localDimension;
	private Map<Integer,Float> localMessageSize;

	public TestSocialNetwork () {
		manager = new ConnectionManagerMysqlImpl ();
		manager.setConnectionParams("ebas", "gwtebas", "bachelor_project");

		connectivity = new FloatSparseMatrix ();
		inDegree = new HashMap <Integer, Integer> ();
		outDegree = new HashMap <Integer, Integer> ();
		localDensity = new HashMap <Integer, Float> ();
		localDimension = new HashMap<Integer, Float> ();
		localMessageSize = new HashMap<Integer, Float> ();

	}

	private void calculateNetwork () 
	throws Exception {

		connectivity.clear();

		float coef0 = 1.0f, coef1 = 0.25f, coef2 = 0.25f;
		Connection c = manager.getConnection();

		String query0 = "select u.userID,v.userID from Message u, Message v where v.id=u.parentID order by 1";
		String query2 = "select propertyValue from settings where tableName like ? and tableID = ? and propertyKey like ?";


		Statement s0 = c.createStatement();
		ResultSet set0 = s0.executeQuery(query0);

		PreparedStatement s2 = c.prepareStatement(query2);
		s2.setString(1, "Message");
		s2.setString(3, "similarities");

		while (set0.next()) {

			int userID   = set0.getInt(1);
			int parentID = set0.getInt(2);
			
			if (userID == parentID)
				continue;
			
			connectivity.updateElement(userID, parentID, coef0 * 1.0f);
		}
		
		set0.close();
		s2.close();
		s0.close();
	}
	
	
	private void calculateNodeSize () 
	throws Exception {
	
		Connection c = manager.getConnection();
		Statement s0 = c.createStatement();
		
		ResultSet set0 = s0.executeQuery("select u.id,count(m.id) from User u, Message m where m.userID = u.id group by u.id");
		while (set0.next()) {
			localDimension.put(set0.getInt(1), new Float (set0.getInt(2)));
		}
		
		set0.close();
		s0.close();
		
	}
	
	
	private void calculateNodeMessageSize () 
	throws Exception {
		
		Connection c = manager.getConnection();
		Statement s0 = c.createStatement();
		
		ResultSet set0 = s0.executeQuery(" select u.id,count(m.id),sum(cast(s.propertyValue as unsigned)) from User u, Message m, settings s where m.userID = u.id and s.tableID = m.id and s.propertyKey like 'num-words' group by u.id");
		while (set0.next()) {
			localMessageSize.put(set0.getInt(1), new Float (set0.getFloat(3) / set0.getInt(2)));
		}
		
		set0.close();
		s0.close();		
	}
	
	

	public void displayNetworkStatistics () 
	throws Exception {
		
		// first calculate network statistics
		System.out.println ("Calculating network ...");
		this.calculateNetwork();
		System.out.println ("done");
		System.out.println ("Calculating node size ...");
		this.calculateNodeSize();
		System.out.println ("done");
		System.out.println ("Calculate node message size ...");
		this.calculateNodeMessageSize();
		System.out.println ("done");

		Connection c = manager.getConnection();
		Statement s0 = c.createStatement();

		ResultSet set0 = s0.executeQuery("select count(*) from User");
		if (set0.next()) {
			this.size = set0.getInt(1);
		}

		set0.close();

		this.density = (float) connectivity.size() / (this.size * (this.size - 1));
		this.deviation = 0.0f;
		
		System.out.println ("Fetching user id list ...");
		List<Integer> listUserIds = new LinkedList<Integer> ();
		set0 = s0.executeQuery("select id from User");
		while (set0.next()) {
			listUserIds.add(new Integer (set0.getInt(1)));
		}
		System.out.println ("done");

		System.out.println ("Calculating used degrees ...");
		Iterator<Integer> i_1 = listUserIds.iterator();
		while (i_1.hasNext()) {
			int u = i_1.next();
			
			Iterator<Integer> i_2 = listUserIds.iterator();
			while (i_2.hasNext()) {
				
				int v = i_2.next();
				
				if ((u != v) && (connectivity.getElement(u, v) != null)) {
					
					if (outDegree.get(u) == null) {
						outDegree.put(u, 1);
					} else {
						//System.out.println ("Increased out-degree for " + u);
						outDegree.put(u, outDegree.get(u) + 1);
					}

					
					if (inDegree.get(v) == null) {
						inDegree.put(v, 1);
					} else {
						//System.out.println ("Increased in-degree for " + v);
						inDegree.put(v, inDegree.get(v) + 1);
					}
					
				}				
			}
			
			if (outDegree.get(u) != null)
				localDensity.put(u, new Float (outDegree.get(u)) / (this.size * (this.size - 1)));
			
		}
		
		System.out.println ("done");

		
		Iterator<Integer> i = localDensity.keySet().iterator();
		while (i.hasNext()) {
			int user = i.next();
			if (localDensity.containsKey(user)) {
				float lDens = localDensity.get(user);
				this.deviation += (lDens - this.density) * (lDens - this.density);
			}
		}

		this.deviation = (float) Math.sqrt(this.deviation / this.size);

		System.out.println ("Network size       : " + this.size);
		System.out.println ("Unique connections : " + connectivity.size());
		System.out.println ("Network density    : " + String.format("%10.6f", this.density));
		System.out.println ("Network deviation  : " + String.format("%10.6f", this.deviation));

		/*
		i = inDegree.keySet().iterator();
		while (i.hasNext()) {
			Integer user = i.next();
			if (inDegree.get(user) != null) {
				int degree = inDegree.get(user);
				if (degree > 10) {
					System.out.println ("User " + user + " has a in-degree " + degree);
				}
			}
		}
		*/
		
		SortedMap<Integer, List<Integer>> sortedUsers = new TreeMap<Integer, List<Integer>> ();
		
		i = outDegree.keySet().iterator();
		while (i.hasNext()) {
			Integer user = i.next();
			
			if (outDegree.get(user) != null) {
				int degree = outDegree.get(user);
				if (sortedUsers.get(degree) == null) {
					List<Integer> l = new LinkedList<Integer> ();
					l.add(user);
					sortedUsers.put(degree, l);
				} else {
					List<Integer> l = sortedUsers.get (degree);
					l.add (user);
					sortedUsers.put(degree, l);
				}
			}
		}
		
		
		
		i = sortedUsers.keySet().iterator();
		while (i.hasNext()) {
			Integer degree = i.next();
			float percent = sortedUsers.get(degree).size() * 100.0f / (float) this.size;
			Iterator<Integer> u = sortedUsers.get(degree).iterator();
			float messageSize = 0.0f;
			while (u.hasNext()) {
				Integer user = u.next();
				if (localMessageSize.get(user) != null)
					messageSize += localMessageSize.get(user);
			}
			messageSize /= sortedUsers.get(degree).size();
			System.out.println ("Degree : " + degree + ", users : " + sortedUsers.get(degree).size() + " (" + String.format("%.5f", percent) + " %), average message size : " + String.format("%.2f", messageSize));
		}
		
		
		i = sortedUsers.keySet().iterator();
		while (i.hasNext()) {
		
			Integer degree = i.next();
			if (degree > 50) {
				Iterator<Integer> u = sortedUsers.get(degree).iterator();
				while (u.hasNext()) {
					Integer user = u.next();
					System.out.println ("User : " + user);
					System.out.println ("  > centrality : " + degree);
					System.out.println ("  > power : " + localDimension.get(user));
					System.out.println ("  > in-degree : " + inDegree.get(user));
					System.out.println ("  > message size : " + localMessageSize.get(user));
				}
			}
		}
		
		
		/*
		i = localDimension.keySet().iterator();
		while (i.hasNext()) {
			Integer user = i.next();
			Float size = localDimension.get(user);
			if (size > 20) {
				System.out.println ("User " + user + " has a size of " + size);
			}
		}
		*/
	}

	public static void main (String[] args) {

		TestSocialNetwork network = new TestSocialNetwork ();
		try {

			network.displayNetworkStatistics();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
