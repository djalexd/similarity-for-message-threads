package project.persistence.builder;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import project.client.persistence.MessageBoard;
import project.client.persistence.MessageThread;

public interface MessageBoardCrawler extends Iterator<MessageBoardCrawler> {
	
	/**
	 * Init the crawler with the specified url. The init
	 * code is web app specific
	 * @param url
	 */
	public void initCrawler (Object[] params)
					throws MalformedURLException, IllegalArgumentException;

	
	public MessageBoard getMessageBoard ();
	
	
	/**
	 * Returns a list of threads found on current page.
	 * 
	 * @return
	 */
	public List<MessageThread> extractMessageThreads ();
	
	/**
	 * Find out the number of pages
	 * @return
	 * 	
	 */
	public int getNumPages ();
	
	/**
	 * Find an (almost) exact number of threads for this forum.
	 * 
	 * The number is aproximative because the last page may not
	 * be filled with threads.
	 * 
	 * @see {@link #getNumPages()}
	 * @return
	 */
	public int getNumThreads ();
	
	
	/**
	 * Set the 'current' page to the specified index.
	 * @param index
	 * @return
	 * 		True if index is valid (hence, it actually changed
	 * the page), false otherwise
	 * 
	 * @see {@link #getNumPages()}
	 */
	public boolean setPage (int index);
	
	/**
	 * Returns the 'current' page
	 * @return
	 * 		The current page index
	 * @see {@link #getPage()}, {@link #getNumPages()}
	 */
	public int getPage ();
}
