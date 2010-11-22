package project.client.rpc;

import java.util.List;
import java.util.Map;

import project.client.persistence.Message;
import project.client.persistence.MessageThread;
import project.client.persistence.User;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

public interface RpcInterface extends RemoteService {
	
	/*
	 * ----------- Crawler specific functionality --------------
	 */
	
	/**
	 * Insert new settings for crawling
	 * @param name
	 * @param crawlerClass
	 * @param threadClass
	 * @param baseUrl
	 * @return
	 */
	public String insertCrawlerSettings (String name, 
										 String crawlerClass, 
										 String threadClass, 
										 String baseUrl);
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public Map<String,Map<String,String>> getCrawlerSettings (String name);

	/**
	 * Insert a new crawler
	 * @param name
	 * @param url
	 * @param crawlerType
	 * @param startPage
	 * @param endPage
	 * @param sleepPerThread
	 * @param sleepPerPage
	 * @param roundRobin
	 * @param startNow
	 * @return
	 */
	public String insertCrawler (String name,
								 String url,
								 String crawlerType,
								 int startPage, int endPage,
								 String sleepPerThread, String sleepPerPage,
								 boolean roundRobin, boolean startNow);
	
	/**
	 * Update an existing crawler (uniquely identified by its name)
	 * @param name
	 * @param url
	 * @param crawlerType
	 * @param startPage
	 * @param endPage
	 * @param sleepPerThread
	 * @param sleepPerPage
	 * @return
	 */
	public String updateCrawler (String name,
								 String url,
								 String crawlerType,
								 int startPage,
								 int endPage,
								 String sleepPerThread, String sleepPerPage);	
	
	/**
	 * Get all crawlers and their settings, or just one crawler if
	 * {@link #name} is not null
	 * @param name The name of the crawler
	 * @return
	 */
	public Map<String,Map<String,String>> getCrawlers (String name);	
	
	/**
	 * Retrieve the list of crawlers for a message board
	 * @param url
	 * @return
	 */
	public Map<String,Map<String,String>> getCrawlersByUrl (String url);
	
	/**
	 * Retrieve the list of crawlers by type
	 * @param type
	 * @return
	 */
	public Map<String,Map<String,String>> getCrawlersByType (String type);
	
	
	/**
	 * Start / pause a crawler
	 * @param name The name of the crawler 
	 * @param start
	 */
	public String activateCrawler (String name, boolean start);
	
	
	/*
	 * ----------------- Statistics related functionality -----------------
	 */
	

	/**
	 * Retrieve statistics for forums
	 * @param boardName The name of the board, or null for general statistics
	 */
	public Map<String,String> getStatistics (String boardName);
	
	/**
	 * Calculate the social network and retrieve its statistics
	 * @param boardName
	 * @return
	 */
	public Map<String,String> getSocialStatistics (String boardName);
	

	/**
	 * Calculate statistics for a single user
	 * @param userName
	 * @return
	 */
	public Map<String,String> getUserSocialStatistics (String userName);
	
	
	/*
	 * ------------------- Search related functionality -------------------
	 */
	
	
	/**
	 * Search for messages
	 * @param keywords
	 * @param limit
	 * @return
	 */
	public List<Message> search (String[] keywords, int limit, boolean lookUser);
	
	public List<Message> searchForThread (String thread);	
	
	/**
	 * Retrieves the used of a message
	 * @param messageId
	 * @return
	 */
	public User getMessageUser (int messageId);
	
	/**
	 * Retrieve the thread of the message
	 * @param messageId
	 * @return
	 */
	public MessageThread getMessageThread (int messageId);
	
	/**
	 * Retrieve a list of similar messages
	 * @param messageId
	 * @param limit
	 * @return
	 */
	public List<Message> getSimilarMessages (int messageId, int limit);
	
	/**
	 * Retrieve the list of messages in a thread
	 * @param threadId
	 * @return
	 */
	public List<Message> getThreadMessages (int threadId);
}
