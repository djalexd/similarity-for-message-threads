package project.client.rpc;

import java.util.List;
import java.util.Map;

import project.client.persistence.Message;
import project.client.persistence.MessageThread;
import project.client.persistence.User;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RpcInterfaceAsync {	
	
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
	public void insertCrawlerSettings (String name, 
										 String crawlerClass, 
										 String threadClass, 
										 String baseUrl,
										 AsyncCallback<String> callback);
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public void getCrawlerSettings (String name, AsyncCallback<Map<String,Map<String,String>>> callback);

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
	public void insertCrawler (String name,
								 String url,
								 String crawlerType,
								 int startPage, int endPage,
								 String sleepPerThread, String sleepPerPage,
								 boolean roundRobin, boolean startNow,
								 AsyncCallback<String> callback);
	
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
	public void updateCrawler (String name,
								 String url,
								 String crawlerType,
								 int startPage,
								 int endPage,
								 String sleepPerThread, String sleepPerPage,
								 AsyncCallback<String> callback);	
	
	/**
	 * Get all crawlers and their settings, or just one crawler if
	 * {@link #name} is not null
	 * @param name The name of the crawler
	 * @return
	 */
	public void getCrawlers (String name, AsyncCallback<Map<String,Map<String,String>>> callback);	
	
	/**
	 * Retrieve the list of crawlers for a message board
	 * @param url
	 * @return
	 */
	public void getCrawlersByUrl (String url,AsyncCallback<Map<String,Map<String,String>>> callback);
	
	/**
	 * Retrieve the list of crawlers by type
	 * @param type
	 * @return
	 */
	public void getCrawlersByType (String type,AsyncCallback<Map<String,Map<String,String>>> callback);
	
	
	/**
	 * Start / pause a crawler
	 * @param name The name of the crawler 
	 * @param start
	 */
	public void activateCrawler (String name, boolean start,AsyncCallback<String> callback);
	
	
	/*
	 * ----------------- Statistics related functionality -----------------
	 */
	

	/**
	 * Retrieve statistics for forums
	 * @param boardName The name of the board, or null for general statistics
	 */
	public void getStatistics (String boardName,AsyncCallback<Map<String,String>> callback);
	
	/**
	 * Calculate the social network and retrieve its statistics
	 * @param boardName
	 * @return
	 */
	public void getSocialStatistics (String boardName,AsyncCallback<Map<String,String>> callback);
	

	/**
	 * Calculate statistics for a single user
	 * @param userName
	 * @return
	 */
	public void getUserSocialStatistics (String userName, AsyncCallback<Map<String,String>> callback);
	
	
	/*
	 * ------------------- Search related functionality -------------------
	 */
	
	
	/**
	 * Search for messages
	 * @param keywords
	 * @param limit
	 * @return
	 */
	public void search (String[] keywords, int limit,boolean lookUsers, AsyncCallback<List<Message>> callback);
	
	public void searchForThread (String thread, AsyncCallback<List<Message>> callback);
	
	/**
	 * Retrieves the used of a message
	 * @param messageId
	 * @return
	 */
	public void getMessageUser (int messageId,AsyncCallback<User> callback);
	
	/**
	 * Retrieve the thread of the message
	 * @param messageId
	 * @return
	 */
	public void getMessageThread (int messageId,AsyncCallback<MessageThread> callback);
	
	/**
	 * Retrieve a list of similar messages
	 * @param messageId
	 * @param limit
	 * @return
	 */
	public void getSimilarMessages (int messageId, int limit,AsyncCallback<List<Message>> callback);
	
	/**
	 * Retrieve the list of messages in a thread
	 * @param threadId
	 * @return
	 */
	public void getThreadMessages (int threadId,AsyncCallback<List<Message>> callback);	
	
}
