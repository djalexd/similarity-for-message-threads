package project.core.mbeans.crawlers;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.naming.InitialContext;

import project.persistence.builder.MessageBoardCrawler;
import project.persistence.builder.MessageThreadCrawler;
import project.utils.statistics.ObjectLooseProperties;
import project.client.persistence.Message;
import project.client.persistence.MessageThread;
import project.client.persistence.User;
import project.core.mbeans.database.ConnectionManagerMysqlImpl;
import project.core.mbeans.processing.MessageProcessingMBean;
import project.core.persistence.PersistenceLoaderMBean;

public class CMessageThreadRunnable extends ConnectionManagerMysqlImpl implements IMessageThreadRunnable {

	private transient PersistenceLoaderMBean loader = null;
	private transient MessageProcessingMBean processor = null;
	
	private CrawlerData data;
	private int databaseIndex;

	private boolean bRunning = true;
	
	private MessageBoardCrawler crawler = null;
	private MessageThreadCrawler crawlerThread = null;
	
	
	
	public CMessageThreadRunnable (CrawlerData data, int index) {
		this.data = data;
		this.databaseIndex = index;
		
		// init database connection
		//
		this.setConnectionParams("ebas", "gwtebas", "bachelor_project");		
		
		try {			
			InitialContext context = new InitialContext ();
			loader = (PersistenceLoaderMBean) context.lookup("PersistenceLoader");
			processor = (MessageProcessingMBean) context.lookup("MessageProcessing");

			Class<?> crawlerClass       = Class.forName(this.data.getSettings().getCrawlerClass());
			Class<?> crawlerThreadClass = Class.forName(this.data.getSettings().getCrawlerThreadClass());
			
			crawler       = (MessageBoardCrawler) crawlerClass.newInstance();
			crawlerThread = (MessageThreadCrawler) crawlerThreadClass.newInstance();

			crawler.initCrawler(new Object[] { this.data });
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
	public void run() {

		while (bRunning && this.data.isActive()) {

			// does the crawler have more pages?
			//
			if (crawler.hasNext()) {

				// advance
				//
				crawler.next();

				//
				// Extract the threads
				//

				List<MessageThread> threads = crawler.extractMessageThreads();
				Iterator<MessageThread> i = threads.iterator();
				while (i.hasNext()) {
					MessageThread t = i.next();
					t.getMessages().clear();

					//
					// Extract messages for one thread
					//
					crawlerThread.initCrawler(new Object [] { t });

					//
					//
					while (crawlerThread.hasNext()) {
						crawlerThread.next();
						t.getMessages().addAll(crawlerThread.extractMessages());
					}

					//
					// update the messageBoard
					//
					t.setMessageBoard(crawler.getMessageBoard());
					try {
						loader.insertMessageThread(t.getUrl(), t.getName(), crawler.getMessageBoard());
						Iterator<Message> j = t.getMessages().iterator();
						while (j.hasNext()) {

							Message msg = j.next();
							msg.setMessageThread(t);

							User u = loader.loadUser(msg.getUser().getName(), crawler.getMessageBoard().getId());
							if (u == null) {
								loader.insertUser(msg.getUser().getName(), crawler.getMessageBoard().getId());
								u = loader.loadUser(msg.getUser().getName(), crawler.getMessageBoard().getId());
							}

							MessageThread thread = loader.loadMessageThread(t.getUrl());
							if (thread == null) {
								loader.insertMessageThread(t.getUrl(), t.getName(), crawler.getMessageBoard());
								thread = loader.loadMessageThread(t.getUrl());
							}

							msg.setUser(u);
							msg.setMessageThread(thread);

							//
							// let the processor handle the raw message
							//
							if (loader.loadMessage(msg.getUrl()) == null) {
								processor.addRawMessage(msg);
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}


					//
					// First sleep, this is per thread
					//
					int sleep1 = data.getMinSleepSecsThread () + 
					(int) ((data.getMaxSleepSecsThread () - data.getMinSleepSecsThread()) * Math.random());

					//System.out.println ("Sleeping for " + sleep1 + " seconds");
					this.pause(sleep1 * 1000);
				}

				//
				// Final sleep, this is per page
				//
				int sleep2 = data.getMinSleepSecsPage() + 
				(int) ((data.getMaxSleepSecsPage() - data.getMinSleepSecsPage()) * Math.random());

				//System.out.println ("Sleeping for " + sleep2 + " seconds");
				this.pause(sleep2 * 1000);

				//
				// update page index
				//
				//mbean.updatePage(crawler.getMessageBoard().getName(), crawler.getPage());
				this.updatePage (crawler.getPage());
				this.activate();
				
				
			} else if (data.isRoundRobin()) {
				
				this.updatePage(data.getStartPage());
			}			
			
		}

	}

	public void activate () {

		if (!data.isActive() && bRunning) {
			data.setActive(true);
			this.notify(); //TODO if sleep doesn't work, comment this
		}
	}

	/**
	 * @param force Not used
	 */
	public synchronized void pause (boolean force) {

		if (data.isActive() && bRunning) {
			try {
				data.setActive(false);
				this.wait();
			} catch (InterruptedException e) { e.printStackTrace(); }
		}	
	}

	private synchronized void pause (int timeout) {

		if (data.isActive() && bRunning) {

			try {
				data.setActive(false);
				
				System.out.println("Sleeping for " + timeout + " miliseconds");
				this.wait(timeout);
				
				data.setActive(true);
			} catch (InterruptedException e) { e.printStackTrace(); }

		}

	}

	/**
	 * @param force Not used
	 */
	public synchronized void stop (boolean force) {

		bRunning = false;
		data.setActive(true);
		this.notify(); //TODO if sleep doesn't work, comment this
	}
	
	private synchronized void updatePage(int page) {
		
		data.setCurrentPage(page);
		
		ObjectLooseProperties props = new ObjectLooseProperties ("Crawlers", this.databaseIndex);
		props.getProperties().putAll(data.getData());
		
		try {
			props.saveToDatabase(this.getConnection());
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}	
}
