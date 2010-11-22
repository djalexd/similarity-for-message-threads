package project.core.mbeans.crawlers;

import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface ThreadCrawlerMBean {

	/**
	 * @deprecated Why would you start / stop a crawler? 
	 */
	@Deprecated
	public void start ();
	
	/**
	 * @deprecated Why would you start / stop a crawler?
	 */
	@Deprecated
	public void stop ();
	
	/**
	 * Add a new crawler, dinamically loaded from classpath
	 * @param crawlerClassName
	 * @param threadCrawlerClassName
	 * @param minSleepPerPage
	 * @param maxSleepPerPage
	 * @param minSleepPerThread
	 * @param maxSleepPerThread
	 * @param currentPage
	 * @param maxPage
	 * @param boardName
	 */
	public void addCrawler (
			String crawlerName,
			String crawlerType,
			String url,
			int minSleepPerPage, int maxSleepPerPage,
			int minSleepPerThread, int maxSleepPerThread,
			int startPage,
			int maxPage,
			boolean roundRobin,
			boolean startNow);	
	
	/**
	 * Updates the page index in the settings 
	 * @param boardName
	 * @param page
	 */
	//public void updatePage (String boardName, int page);
	
	/**
	 * Returns the set of crawlers from database
	 * @return
	 * 
	 * @deprecated Don't return a weak-typed {@link Map}
	 */
	@Deprecated
	public Map<String, Map<String,String>> getCrawlers ();
	
	/**
	 * Returns a list of settings
	 * @param name The name of the crawler type, or null if all settings are requested
	 * @return
	 */
	public Map<String,Map<String,String>> getCrawlerSettings (String name);
}
