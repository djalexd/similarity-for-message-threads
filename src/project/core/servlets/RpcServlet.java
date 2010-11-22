package project.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import project.client.persistence.Message;
import project.client.persistence.MessageThread;
import project.client.persistence.User;
import project.client.rpc.RpcInterface;
import project.core.mbeans.analysis.MessageBaseProcessorMBean;
import project.core.mbeans.crawlers.ThreadCrawlerMBean;
import project.core.mbeans.database.ConnectionManager;
import project.core.mbeans.database.ConnectionManagerMBean;
import project.core.mbeans.search.MessageSeachMBean;
import project.core.persistence.PersistenceLoaderMBean;
import project.persistence.properties.MessageWithProperties;
import project.utils.statistics.ObjectLooseProperties;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

public class RpcServlet extends RemoteServiceServlet implements RpcInterface {

	/**
	 * Generated
	 */
	private static final long serialVersionUID = 7745608033035200272L;
	
	private Connection connection = null;
	
	private ThreadCrawlerMBean crawlerMBean = null;
	private MessageBaseProcessorMBean analysisMBean = null;
	private PersistenceLoaderMBean loader = null;
	private MessageSeachMBean searcher = null;
	
	
	public void init(ServletConfig config) {
		try {
			super.init(config);

			System.out.println("Servlet init");			
			InitialContext context = new InitialContext();
			
			ConnectionManagerMBean man = new ConnectionManager ();
			man.setConnectionParams("ebas", "gwtebas", "bachelor_project");
			connection = man.getConnection();
			
			crawlerMBean = (ThreadCrawlerMBean) context.lookup("ThreadCrawler");		
			analysisMBean = (MessageBaseProcessorMBean) context.lookup("MessageBaseProcessor");
			loader = (PersistenceLoaderMBean) context.lookup("PersistenceLoader");
			searcher = (MessageSeachMBean) context.lookup("MessageSearch");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected SerializationPolicy doGetSerializationPolicy(
			HttpServletRequest request, String moduleBaseURL, String strongName) {
		
		try {
			
			URL url = new URL (moduleBaseURL + strongName + ".gwt.rpc"); 
			SerializationPolicy policy = 
						SerializationPolicyLoader.loadFromStream(url.openStream(), null);
			
			return policy;
		
		} catch (Exception e) {
			System.out.println("No serialization policy found. Considering default behavior");
			return super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
		}
	}

	
	public Map<String, Map<String, String>> getCrawlers() {		
		return crawlerMBean.getCrawlers();
	}

	
	public List<MessageThread> getThreadMessages(int startThreadId, int endThreadId) {
		
		List<MessageThread> list = new LinkedList<MessageThread> ();
		try {
			//list = analysisMBean.loadMessageBoardThreads(startThreadId, endThreadId);
			list = null;
		} catch (Exception e) {
			
			e.printStackTrace();
		}

		System.out.println ("found " + list.size() + " threads");
		return list;
	}


	public String[] getSynonims(String word) {
		
		try {
			return analysisMBean.getSynonyms(word);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		PrintWriter writer = resp.getWriter();
		Map<String, Map<String,String>> crawlers = crawlerMBean.getCrawlers();
		Iterator<String> i = crawlers.keySet().iterator();
		
		while (i.hasNext()) {
			String name = i.next();
			writer.append(name + "\n");			
			Map<String,String> props = crawlers.get(name);
			
			Iterator<String> j = props.keySet().iterator();
			while (j.hasNext()) {
				String key = j.next();
				String val = props.get(key);
				
				writer.append("  > " + key + " : " + val + "\n");
			}
			
			writer.append("\n");
		}
		
	}

	public String insertCrawlerSettings(String name, String crawlerClass,
			String threadClass, String baseUrl) {
		
		//
		// try to find something similar
		//

		if (name == null || name.length() == 0 ||
			crawlerClass == null || crawlerClass.length() == 0 ||
			threadClass == null || threadClass.length() == 0 ||
			baseUrl == null || baseUrl.length() == 0)
			
			return "Invalid parameters (one is null or empty)!";
		
		String maxQuery = "select count(*) from settings where tableName like ? and propertyKey like ?";
		String query = "insert into settings(tableName,tableID,propertyKey,propertyValue) values(?,?,?,?)";
		try {
			PreparedStatement statementMaxQuery = connection.prepareStatement(maxQuery);
			statementMaxQuery.setString(1, "Crawlers-settings");
			statementMaxQuery.setString(2, "forum-type");
			ResultSet set = statementMaxQuery.executeQuery();
			int lastId = 0;
			if (set.next()) {
				lastId = set.getInt(1);
			}
			
			set.close();
			statementMaxQuery.close();
			
			lastId ++;
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, "Crawlers-settings");
			statement.setInt(2, lastId);
			
			statement.setString(3, "forum-type");
			statement.setString(4, name);
			statement.execute();
			
			statement.setString(3, "base-url");
			statement.setString(4, baseUrl);
			statement.execute();
			
			statement.setString(3, "crawler-class");
			statement.setString(4, crawlerClass);
			statement.execute();
			
			statement.setString(3, "thread-class");
			statement.setString(4, threadClass);
			statement.execute();
			
			statement.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "operation succeeded";
	}

	
	public List<Map<String, String>> getCrawlerSettings() {
		
		List<Map<String, String>> list = new LinkedList<Map<String,String>> ();
		
		String maxQuery = "select count(*) from settings where tableName like ? and propertyKey like ?";
		String query = "select * from settings where tableName like ? and tableID like ?";
		try {
			PreparedStatement statementMaxQuery = connection.prepareStatement(maxQuery);
			statementMaxQuery.setString(1, "Crawlers-settings");
			statementMaxQuery.setString(2, "forum-type");
			ResultSet set = statementMaxQuery.executeQuery();
			int lastId = 0;
			if (set.next()) {
				lastId = set.getInt(1);
			}
			
			set.close();
			statementMaxQuery.close();
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1,"Crawlers-settings");
			
			for (int i = 1; i < lastId + 1; i++) {
				statement.setInt(2, i);
				ResultSet s = statement.executeQuery();
				
				Map<String,String> map = new HashMap<String, String> ();
				while (s.next()) {
					map.put(s.getString("propertyKey"), s.getString("propertyValue"));
				}
				
				//
				// add the map
				list.add(map);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	
	public List<Message> search(String[] keywords, int limit, boolean lookUser) {
		
		try {

			return searcher.search(keywords, limit, lookUser);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} 
	}
	

	@Override
	public String activateCrawler(String name, boolean start) {		
		return "not yet implemented";
	}

	@Override
	public synchronized Map<String, Map<String, String>> getCrawlers(String name) {
		
		Map<String,Map<String,String>> map = crawlerMBean.getCrawlers();
		if (name == null)
			return map;
		else {
			synchronized (map) {
				Iterator<String> i = map.keySet().iterator();
				while (i.hasNext()) {
					String key = i.next();
					if (!key.equals(name))
						i.remove();
				}
				
				return map;
			}
		}		
	}

	@Override
	public Map<String, Map<String, String>> getCrawlersByType(String type) {
		
		Map<String,Map<String,String>> map = crawlerMBean.getCrawlers();
		if (type == null)
			return map;
		else {
			synchronized (map) {
				Iterator<String> i = map.keySet().iterator();
				while (i.hasNext()) {
					String key = i.next();
					Map<String,String> crawler = map.get(key);
					if (crawler.containsKey("type")) {
						if (!crawler.get("type").equals(type)) {
							i.remove();
						}
					} else {
						i.remove();
					}
				}
				
				return map;				
			}
		}
		
	}

	@Override
	public Map<String, Map<String, String>> getCrawlersByUrl(String url) {
		
		Map<String,Map<String,String>> map = crawlerMBean.getCrawlers();
		if (url == null)
			return map;
		else {
			synchronized (map) {
				Iterator<String> i = map.keySet().iterator();
				while (i.hasNext()) {
					String key = i.next();
					Map<String,String> crawler = map.get(key);
					if (crawler.containsKey("url")) {
						if (!crawler.get("url").equals(url)) {
							i.remove();
						}
					} else {
						i.remove();
					}
				}
				
				return map;				
			}
		}		
	}

	public MessageThread getMessageThread(int messageId) {
		return loader.getMessageThread(messageId);
	}

	
	public User getMessageUser (int messageId) {
		return loader.getMessageUser(messageId);
	}

	
	@Override
	public List<Message> getSimilarMessages(int messageId, int limit) {
		
		Message msg = loader.loadMessage(messageId);
		
		MessageWithProperties msgWProps = new MessageWithProperties (msg);
		msgWProps.loadProperties(connection);

		if (!msgWProps.hasProperty("similarities")) {
			return new LinkedList<Message> ();
		}
		
		String[] similarityTokens = msgWProps.getProperty("similarities").split(";");
		int numSimilarities = similarityTokens.length / 2;
		List<Message> messages = new LinkedList<Message> ();
		if (numSimilarities != 0) {
			for (String similarity : similarityTokens) {

				String url = similarity.substring(0, similarity.indexOf(':'));
				String strength = similarity.substring(similarity.indexOf(':') + 1);

				if (Float.parseFloat(strength) > 0.0f) {
					Message similarMsg = loader.loadMessage(url);
					similarMsg.setUser(loader.getMessageUser(similarMsg.getId()));
					similarMsg.setMessageThread(loader.getMessageThread(similarMsg.getId()));

					messages.add(similarMsg);
				}
			}

		}
		
		return messages;
	}

	
	@Override
	public Map<String, String> getSocialStatistics(String boardName) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public Map<String, String> getStatistics(String boardName) {
		
		Map<String,String> map = new HashMap<String, String> ();
		
		map.put("num-words",    "" + loader.getNumWords(boardName, false));
		map.put("num-users",    "" + loader.getNumUsers(boardName));
		map.put("num-messages", "" + loader.getNumMessages(boardName));
		map.put("num-invalid-messages", "" + loader.getNumInvalidMessages(boardName));
		map.put("num-threads",  "" + loader.getNumThreads(boardName));
		
		if (boardName == null) {
			map.put("num-distinct-words", "" + loader.getNumWords(null, true));
			map.put("num-boards", "" + loader.getNumBoards());
		}

		return map;
	}

	
	public List<Message> getThreadMessages(int threadId) {
		
		return loader.loadThreadMessages(threadId);
		
	}

	@Override
	public Map<String, String> getUserSocialStatistics(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String insertCrawler(String name, String url, String crawlerType,
			int startPage, int endPage, String sleepPerThread,
			String sleepPerPage, boolean roundRobin, boolean startNow) {
		
		String sl11 =   sleepPerPage.substring(0, sleepPerPage.indexOf('-'));
		String sl12 =   sleepPerPage.substring(   sleepPerPage.indexOf('-') + 1);
		String sl21 = sleepPerThread.substring(0, sleepPerThread.indexOf('-'));
		String sl22 = sleepPerThread.substring(   sleepPerThread.indexOf('-') + 1);
		
		int x1 = Integer.parseInt(sl11);
		int x2 = Integer.parseInt(sl12);
		int x3 = Integer.parseInt(sl21);
		int x4 = Integer.parseInt(sl22);		
		
		crawlerMBean.addCrawler(name, crawlerType, url, x1, x2, x3, x4, startPage, endPage, roundRobin, startNow);
		return "insertCrawler : ok";		
	}

	
	@Override
	public String updateCrawler(String name, String url, String crawlerType,
			int startPage, int endPage, String sleepPerThread,
			String sleepPerPage) {
		
		// TODO Auto-generated method stub
		return null;
	}

	
	public Map<String, Map<String, String>> getCrawlerSettings(String name) {
		return crawlerMBean.getCrawlerSettings(name);
	}


	public List<Message> searchForThread(String threadName) {
		
		MessageThread thread = loader.loadMessageThreadByName(threadName);
		
		if (thread == null)
			return new LinkedList<Message> ();
		List<Message> messages = loader.loadThreadMessages(thread.getId());
		Iterator<Message> i = messages.iterator();
		while (i.hasNext()) {
			
			Message msg = i.next();
			msg.setMessageThread(thread);
			msg.setUser(loader.getMessageUser(msg.getId()));
			
		}
		
		return messages;
	}
	
}
