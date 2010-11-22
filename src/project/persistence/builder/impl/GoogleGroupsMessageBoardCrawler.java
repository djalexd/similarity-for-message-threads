package project.persistence.builder.impl;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

import project.client.persistence.MessageBoard;
import project.client.persistence.MessageThread;
import project.core.mbeans.crawlers.CrawlerData;
import project.persistence.builder.MessageBoardCrawler;


public class GoogleGroupsMessageBoardCrawler extends BaseHttpClient implements MessageBoardCrawler {

	private static final String STR_DATABASE = "bachelor_project";
	private static final String STR_USERNAME = "ebas";
	private static final String STR_PASSWORD = "gwtebas";

	private Connection connection = null;

	static {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	

	private MessageBoard board = null;

	private int pageIdx            = -1;
	private int maxPageIdx         = -1;
	private int numMessagesPerPage = -1;
	
	private CrawlerData data       = null;

	private static final String THREAD_REGEX_0 = "<a href=\"/group/";
	private static final String THREAD_REGEX_1 = "/browse_thread/thread/([a-z0-9]*)#\"><font size=\"..\">(.*)</font></a>";
	private static final String PAGE_IDX_REGEX = "<b>([0-9]*)</b> of <b>([0-9]*)</b>";


	public GoogleGroupsMessageBoardCrawler () {

		this.numMessagesPerPage = 10;
		System.out.println ("Warning! The default (no parameter) constructor of " + 
				this.getClass().getName() + " is meant to be used only by the MBean crawler. Do not use it directly, as it doesn't init anything!!");

	}

	public GoogleGroupsMessageBoardCrawler (Object[] params) 
	throws MalformedURLException {

		this.numMessagesPerPage = 10;
		this.initCrawler(params);
	}


	public List<MessageThread> extractMessageThreads() {

		List<MessageThread> threads = new LinkedList<MessageThread> ();
		//session.beginTransaction();

		try {

			HttpHost target = new HttpHost(this.board.getUrl(), 80, "http");
			HttpClient client = createHttpClient();
			HttpRequest req = createRequest("/group/" + this.board.getName() + 
					"/topics?start=" + (this.numMessagesPerPage * this.pageIdx) + "&sa=N");

			//System.out.println("executing request to " + target + ": " + req.getRequestLine().getUri());
			HttpEntity entity = null;
			try {

				HttpResponse rsp = client.execute(target, req);
				entity = rsp.getEntity();

				if (entity != null) {
					String content = EntityUtils.toString(entity);
					if (content.indexOf(" To protect our users, we can't process your request") != -1) {

						System.out.println ("Your crawler was banned!!!");

					} else {

						Pattern pattern = Pattern.compile(THREAD_REGEX_0 + data.getUrl() + THREAD_REGEX_1);
						Matcher matcher = pattern.matcher(content);
						while (matcher.find()) {

							MessageThread msgThread = new MessageThread ();
							msgThread.setMessageBoard(board);
							msgThread.setName(matcher.group(2));
							msgThread.setUrl(matcher.group(1));
							threads.add(msgThread);

						}

						//System.out.println ("Found " + threads.size() + " threads");

						pattern = Pattern.compile(PAGE_IDX_REGEX);
						matcher = pattern.matcher(content);
						while (matcher.find()) {
							if (matcher.groupCount() == 2) {
								this.maxPageIdx = Integer.parseInt(matcher.group(2)) / this.numMessagesPerPage; 
							} else {
								//System.out.println ("Invalid match on max page index : " + matcher.group()); TODO log
							}
						}

						System.out.println ("Indexed page = " + this.pageIdx + 
											"(start = " + data.getStartPage() + 
											",end = " + data.getEndPage() +
											",max found = " + this.maxPageIdx + ")");
					}
				}

			} finally {
				// If we could be sure that the stream of the entity has been
				// closed, we wouldn't need this code to release the connection.
				// However, EntityUtils.toString(...) can throw an exception.

				// if there is no entity, the connection is already released
				if (entity != null)
					entity.consumeContent(); // release connection gracefully
			}		

		} catch (Exception e) {
			e.printStackTrace();
		}

		//session.getTransaction().commit();
		return threads;		
	}


	public int getNumPages() {
		if (this.maxPageIdx == -1) {
			// 
			// it hasn't been initialized, so update it now
			// (just make a request for any page)

			this.maxPageIdx = 0;
			this.pageIdx = 0;
			this.extractMessageThreads();
		}

		return this.maxPageIdx;
	}


	public int getNumThreads() {
		return this.getNumPages() * this.numMessagesPerPage;
	}


	public int getPage() {		
		return this.pageIdx;
	}


	public void initCrawler(Object[] params) throws IllegalArgumentException {		

		try {

			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + STR_DATABASE, 
					STR_USERNAME, STR_PASSWORD);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (params.length < 1 || params.length > 2) {
			throw new IllegalArgumentException ("Invalid number of params (expected 1, found " + params.length + ")");
		}

		Class<?>[] classes = new Class<?>[] {
				CrawlerData.class
		};

		for (int i = 0; i < params.length; i++) {
			if (!params [i].getClass().equals(classes [i])) {
				throw new IllegalArgumentException ("Invalid argument (expected class " + 
						classes [i].getSimpleName() + ", found " + 
						params [i].getClass().getSimpleName() + ")");
			}
		}

		try {
			
			this.data = (CrawlerData) params [0];
			String query = "select * from MessageBoard where name like ?;";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, data.getUrl());

			ResultSet set = statement.executeQuery(); 
			if (!set.next()) {
				System.out.println ("Insert a new messsageBoard: " + data.getUrl());
				query = "insert into MessageBoard(name,url) values(?,?)";
				
				set.close();
				statement.close();
				
				statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
				statement.setString(1, data.getUrl());
				statement.setString(2, data.getSettings().getUrl());

				statement.execute();
				set = statement.getGeneratedKeys();
				set.next();

				this.board = new MessageBoard ();
				board.setId(set.getInt(1));
				board.setName(data.getUrl());
				board.setDescription(null);
				board.setUrl(data.getSettings().getUrl());
				
				set.close();
				statement.close();
				
			} else {

				this.board = new MessageBoard ();
				board.setId(set.getInt(1));
				board.setName(set.getString(2));
				board.setDescription(set.getString(3));
				board.setUrl(set.getString(4));
				
				set.close();
				statement.close();
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		this.setPage(data.getCurrentPage());
	}


	public boolean setPage(int index) {
		if (this.getNumPages() != -1) {
			if (index < 0 || index > (this.getNumPages() - 1))
				return false;
		} else {
			pageIdx = maxPageIdx = 0;
			extractMessageThreads ();
		}

		this.pageIdx = index;
		return true;
	}


	public boolean hasNext() {
		return this.pageIdx < data.getEndPage();
	}


	public MessageBoardCrawler next() {

		if (!this.setPage(this.getPage() + 1))
			return null;

		return this;
	}


	public void remove() {
		/* Not implemented, it does nothing (inherited from Iterator<E>, it should safely remove the current object pointed by iterator) */
	}


	public MessageBoard getMessageBoard() {
		return this.board;
	}
}
