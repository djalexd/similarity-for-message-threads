package project.core.mbeans.crawlers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Stateful;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.SerializedConcurrentAccess;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;


import project.core.mbeans.database.ConnectionManagerMysqlImpl;
import project.utils.statistics.ObjectLooseProperties;

@Stateful
@RemoteBinding(jndiBinding="ThreadCrawler")
@SerializedConcurrentAccess
@CacheConfig(removalTimeoutSeconds=18000L)
public class ThreadCrawler extends ConnectionManagerMysqlImpl implements ThreadCrawlerMBean {

	private transient Connection connection = null;

	private Map<String,CrawlerSettingsData> settings;
	private Map<String,CrawlerData> crawlers;
	
	private transient List<IMessageThreadRunnable> runnables = new LinkedList<IMessageThreadRunnable> ();

	@PostConstruct
	public void start() {
		System.out.println ("ThreadCrawler started");

		try {			
			this.setConnectionParams("ebas", "gwtebas", "bachelor_project");
			connection = this.getConnection();

			settings = new HashMap<String, CrawlerSettingsData> ();
			crawlers = new HashMap<String, CrawlerData> ();
			this.setup();

			//setJndiName(this.getClass().getSimpleName());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@PrePassivate
	public void passivate () {
		System.out.println ("ThreadCrawler prepassivate");
	}

	@PostActivate
	public void activate () {
		System.out.println ("ThreadCrawler activate");
	}


	@PreDestroy
	public void stop() {

		//saveSettings();
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		/*
		//
		// terminate all runnables
		Iterator<IMessageThreadRunnable> i = runnables.iterator();
		while (i.hasNext()) {

			IMessageThreadRunnable runnable = i.next();
			runnable.stop(true);

		}
		runnables.clear();
		 */

		System.out.println ("ThreadCrawler stopped");	
	}


	public void addCrawler (
			String crawlerName,
			String crawlerType,
			String url,
			int minSleepPerPage, int maxSleepPerPage,
			int minSleepPerThread, int maxSleepPerThread,
			int startPage,
			int maxPage,
			boolean roundRobin,
			boolean startNow) {
		
		
		CrawlerData data = new CrawlerData ();
		data.setName(crawlerName);
		data.setType(crawlerType);
		data.setUrl(url);
		data.setMinSleepSecsPage(minSleepPerPage);
		data.setMaxSleepSecsPage(maxSleepPerPage);
		data.setMinSleepSecsThread(minSleepPerThread);
		data.setMaxSleepSecsThread(maxSleepPerThread);
		
		data.setStartPage(startPage);
		data.setCurrentPage(startPage);
		data.setEndPage(maxPage);
		
		data.setRoundRobin(roundRobin);
		data.setActive(startNow);
		
		//
		// Find out the settings
		//
		Iterator<String> i = settings.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			CrawlerSettingsData set = settings.get(key);
			
			if (data.getType().equals(set.getType())) {
				data.setSettings(set);
				break;
			}
		}
		
		crawlers.put("" + (crawlers.size() + 1), data);
		this.saveSettings();
		try {
			this.setup(crawlers.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	private void setup () 
	throws SQLException {

		this.loadSettings();

		
		//
		// Load the crawlers
		//
		for (int i = 1; i <= crawlers.size(); i++) {
			
			//this.setup (i);
			
		}
	}

	private void setup (int index) 
	throws SQLException {

		this.loadSettings();

		//
		// Load the crawlers
		//
		int numCrawlers = crawlers.size();
		if (index <= 0 || index > numCrawlers)
			return;

		//
		// find out the thread and message board classes
		//
		
		CrawlerData c = this.crawlers.get("" + index);
		IMessageThreadRunnable runnable = new CMessageThreadRunnable (c, index);
		
		runnables.add(runnable);
		new Thread (runnable).start();
	}

	private synchronized void loadSettings () {
		//
		// load the settings from database
		//
		try {
			
			
			this.settings.clear();
			
			ObjectLooseProperties props = new ObjectLooseProperties ("Crawlers-settings");
			int count = props.getNumEntries (connection, CrawlerSettingsData.getPropertyCount());

			for (int i = 1; i <= count; i++) {
				//
				// load each property
				//
				props.getProperties().clear();
				props.setId(i);
				
				props.loadFromDatabase(connection);
				
				CrawlerSettingsData data = new CrawlerSettingsData ();
				data.setData(props.getProperties());
				
				this.settings.put("" + i, data);
			}
						
			
			this.crawlers.clear();
			
			props = new ObjectLooseProperties ("Crawlers");
			count = props.getNumEntries (connection, CrawlerData.getPropertyCount());

			for (int i = 1; i <= count; i++) {
				//
				// load each property
				//
				props.getProperties().clear();
				props.setId(i);
				
				props.loadFromDatabase(connection);
				
				CrawlerData data = new CrawlerData ();
				data.setData(props.getProperties());
				
				Iterator<String> j = settings.keySet().iterator();
				while (j.hasNext()) {
					String key = j.next();
					CrawlerSettingsData set = settings.get(key);
					
					if (set.getType().equals(data.getType())) {
						data.setSettings(set);
						break;
					}
				}
				
				this.crawlers.put("" + i, data);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private synchronized void saveSettings () {
		
		//
		// save the settings to database
		//
		try {
						
			ObjectLooseProperties props = new ObjectLooseProperties ();

			props.setTable("Crawlers-settings");
			for (int i = 1; i <= settings.size(); i++) {
				//
				// save each property
				//
				props.getProperties().clear();
				props.setId(i);
				props.setProperties(settings.get("" + i).getData());
				
				props.saveToDatabase(connection);				
			}
						
			
			props.setTable("Crawlers");
			for (int i = 1; i <= crawlers.size (); i++) {
				//
				// load each property
				//
				props.getProperties().clear();
				props.setId(i);
				props.setProperties(crawlers.get("" + i).getData());
				
				props.saveToDatabase(connection);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public Map<String, Map<String, String>> getCrawlers() {

		this.loadSettings();

		Map<String,Map<String,String>> map = new HashMap<String, Map<String,String>> ();
		Iterator<String> i = crawlers.keySet().iterator();
		while (i.hasNext()) {
			String name = i.next();
			map.put(name, crawlers.get(name).getData());
		}
		
		return map;
	}


	public Map<String, Map<String, String>> getCrawlerSettings (String name) {
		
		this.loadSettings();
		Map<String,Map<String,String>> map = new HashMap<String, Map<String,String>> ();
		Iterator<String> i = settings.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			map.put(key, settings.get(key).getData());
		}
		
		if (name != null) {
			i = map.keySet().iterator();
			while (i.hasNext()) {
				String key = i.next();
				if (!key.equals(name))
					i.remove();
			}
		}
		
		return map;
	}
}
