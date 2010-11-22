package project.persistence.builder;

import java.util.Iterator;
import java.util.List;

import project.client.persistence.Message;


public interface MessageThreadCrawler extends Iterator<MessageThreadCrawler> {

	/**
	 * Init the crawler with specified url. The init code
	 * is web app dependent (specific)
	 * @param url
	 */
	public void initCrawler (Object[] params)
					throws IllegalArgumentException;
	
	/**
	 * Returns a list of messages found on current thread page.
	 * The thread may contain multiple pages, so unless a code like the following is used,
	 * not all messages would be read
	 * <br/>
	 * <br/>
	 * <code>
	 * <pre>
	 * while (crawler.hasNext()) {<br/>
	 *                crawler.next();<br/>
	 * 		List<Message> list_of_messages = crawler.extractMesssages ();<br/>
	 *                .... do something with messages from current thread page ...<br/>
	 * }<br/>
	 * </pre>
	 * </code>
	 * @return
	 */
	public List<Message> extractMessages ();

}
