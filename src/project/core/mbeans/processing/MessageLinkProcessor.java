package project.core.mbeans.processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;

import com.google.gwt.user.server.rpc.UnexpectedException;

import project.client.persistence.Message;
import project.core.mbeans.database.ConnectionManagerMysqlImpl;
import project.core.persistence.PersistenceLoaderMBean;
import project.persistence.properties.MessageWithProperties;
import project.utils.statistics.MessageSimilarity;
import project.utils.statistics.impl.MessageSimilarity_Impl;

@RemoteBinding(jndiBinding="MessageLinkProcessor")
@Stateful
@CacheConfig(removalTimeoutSeconds=18000L)
public class MessageLinkProcessor extends ConnectionManagerMysqlImpl implements MessageLinkProcessorMBean, Runnable {
	
	private static final String WORD_DB_DELIMITER = ";";
	private static final String WORD_STRENGTH_DELIMITER = ":";
	
	//
	// used by link calculus
	//
	private static final String STR_SPECIALWORD_LINK_LOCATION = "/home/alexd/workspace/proiect-diploma/config/special-words-links.in";
	
	private static final float MIN_SIMILARITY = 0.001f;
	
	private static final int MIN_NUM_WORDS = 10;
	
	/**
	 * This is the maximum count of similarities saved in database. The measure is taken into
	 * account only if the number of similarities found is greater than this number. <br/>
	 * 
	 * <br/>
	 * Because storing space has O(n^2) complexity (where n is the number of messages existing in
	 * database), storing a large number (n >> 1) of messages will bloat the space used, not to mention
	 * slowing down the application. Limiting the similarities saved to this value, a better, O(n)
	 * storing algorithm is achieved. 
	 * 
	 *  <br/>
	 *  <br/>
	 *  Decreasing this number to <b>1</b> will cause the mechanism to save the most relevant similarity
	 *  (best first strategy, since data arrived progressively) between the current message and 
	 *  all the other (existing already in database)
	 */
	private static final int MAX_SIMILARITY_COUNT_SAVE = 20;
	
	
	private List<MessageWithProperties> queue = null;
	private boolean bRunning = true;
	
	//
	// connection used to load existing messages (and update them)
	//
	private PersistenceLoaderMBean loader = null;
	
	//
	// When the messages saved in database reach a couple of hundreds, loading the same
	// list of previous message over and over (when processing a new message) will take a
	// lot of time and is extremely inneficient
	//
	private List<MessageWithProperties> messages = new LinkedList<MessageWithProperties> (); // TODO = null;	

	private Map<String,Float> specialLinks = null;

	@PostConstruct
	public void start() {
		//System.out.println ("MessageLinkProcessor service started");
		//JndiBinder.bind("MessageLinkProcessor", this);
		
		try {
			InitialContext context = new InitialContext (); 
			loader = (PersistenceLoaderMBean) context.lookup("PersistenceLoader");
			
			//
			// load existing messages (along with their properties)
			//
			messages = loader.loadMessagesWithProperties (null);
			
			//
			//
			queue = new LinkedList<MessageWithProperties> ();
			
			//
			//
			this.loadSpecialWordLinks();
			
			//
			// setup the connection
			this.setConnectionParams("ebas", "gwtebas", "bachelor_project");
			
			//
			// start the thread
			//
			new Thread (this).start();			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PreDestroy
	public void stop() {
		
		//
		//
		messages.clear();

		//JndiBinder.unbind("MessageLinkProcessor");
		//System.out.println ("MessageLinkProcessor service stopped");
	}

	
	public void addProcessedMessage(MessageWithProperties message) {
		
		queue.add(message);
		synchronized (this) {
			this.notify();
		}
	}

	public int getQueueLength() {
		
		synchronized (queue) {
			return queue.size();
		}
	}

	
	public void run() {
		
		while (bRunning) {
			
			MessageWithProperties msg = null;
			if (this.getQueueLength() != 0) {
				
				synchronized (queue) {

					//
					// get the first message in queue
					//
					msg = queue.get(0);
					queue.remove(0);

				}
			} else {
				//
				// wait for a message, sleep until then
				//
				
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {}					
				}
			}			

			if (msg != null) {


				System.out.println ("New message for link processing : ");
				System.out.println ("\t> url : " + msg.getUrl());
				
				//
				// reject bad messages (they should have been dispatched and handled somewhere
				// else, so only by mistake the arrived here)
				//

				if (msg.hasProperty("type") && msg.getProperty("type").equals("bad-message")) {
					System.out.println ("Bad message!");
					continue;
				}

				//
				// process the message (attach more properties) 
				//

				msg = this.processMessage(msg);

				//
				// finally, persist the object
				//

				try {

					loader.insertMessage(msg);
					
					//
					// TODO workaround to get inserted message's row id. Upgrade this later
					//
					Message msgTmp = loader.loadMessage(msg.getUrl());
					msg.setId(msgTmp.getId());
					msgTmp = null;
					msg.saveProperties(this.getConnection());

					//
					// well, we don't want to load the messages everytime a new one arrives, so just
					// use this list as storage
					//
					messages.add(msg);

				} catch (SQLException e) {
					e.printStackTrace(); // TODO log this
				}

			}
		}
	}
	
	
	
	public MessageWithProperties processMessage (MessageWithProperties message) {
		
		Map<String, Float> links = this.calculateSpecialWordLinks(message);
		String strLink = "";
		
		//
		// Iterate through all links found and compose the string
		//
		Iterator<String> i = links.keySet().iterator();		
		while (i.hasNext()) {
			
			String url = i.next();
			Float strength = links.get(url);
			
			strLink += url + WORD_STRENGTH_DELIMITER + strength.floatValue() + WORD_DB_DELIMITER;
		}
		message.setProperty("special-link", strLink);
		
		
		Map<String,Float> similarities = this.calculateTextSimilarity(message);
		String strSimilarity = "";
		
		if (similarities.size() < MAX_SIMILARITY_COUNT_SAVE) {
			//
			// Iterate through all similarities found and compose the string
			//
			i = similarities.keySet().iterator();
			while (i.hasNext()) {

				String url = i.next();
				Float strength = similarities.get(url);

				strSimilarity += url + WORD_STRENGTH_DELIMITER + strength.floatValue() + WORD_DB_DELIMITER;
				this.addCrossLinkSimilarity(message,url, strength);
			}
			
			message.setProperty("num-similarities", "" + similarities.size());
		} else {
			//
			// First, sort out the best similarities
			//
			Set<Entry<String,Float>> set = similarities.entrySet();
			List<Entry<String,Float>> list = new LinkedList<Entry<String,Float>> ();
			
			Iterator<Entry<String,Float>> j = set.iterator();
			while (j.hasNext()) {
				list.add(j.next());
			}
			
			//
			// Sort the entries
			//
			Collections.sort(list, new Comparator<Entry<String,Float>> () {
						public int compare (Entry<String,Float> e1, Entry<String,Float> e2) {
							
							if (e1.getValue().equals(e2.getValue()))
								return 0;
							
							if (e1.getValue() < e2.getValue())
								return 1;
							
							return -1;
						}
			});
			
			//
			// and get the most relevant ones
			//
			list = list.subList(0, MAX_SIMILARITY_COUNT_SAVE);
			j = list.iterator();
			while (j.hasNext()) {

				Entry<String,Float> entry = j.next();
				String url = entry.getKey();
				Float strength = entry.getValue();

				strSimilarity += url + WORD_STRENGTH_DELIMITER + strength.floatValue() + WORD_DB_DELIMITER;
				this.addCrossLinkSimilarity(message,url, strength);
			}
			
			//
			// clear memory used
			//
			list.clear();
			// set.clear () (set will be cleared below - #257 - since all elements are actually references)
			
			message.setProperty("num-similarities", "" + list.size());
		}
		message.setProperty("similarities", strSimilarity);
		
		similarities.clear();
		links.clear();
		
		//
		// print message properties
		// 
		//System.out.println (message.getProperties());
		
		return message;
	}
	
	
	
	private Map<String,Float> calculateSpecialWordLinks (MessageWithProperties message) {
		
		Map<String, Float> links = new HashMap<String, Float> ();
		String strSpecialWords = message.getProperty("special-words");
		
		if (strSpecialWords == null) {
			
			// TODO log this
			//
			//System.out.println ("The 'special-words' property not found!");
			return links;
		}
		
		String[] tokenWords = strSpecialWords.split(WORD_DB_DELIMITER);		
		Iterator<MessageWithProperties> i = messages.iterator();
		while (i.hasNext()) {
			
			MessageWithProperties other = i.next();
			if (other.hasProperty("special-words")) {
				
				String strOtherSpecialWords = other.getProperty("special-words");
				String[] tokenOthers = strOtherSpecialWords.split(WORD_DB_DELIMITER);
				
				float strength = 0.0f;
				for (int k1 = 0; k1 < tokenWords.length; k1 ++) {
					
					for (int k2 = 0; k2 < tokenOthers.length; k2 ++) {
						
						String strComposed = tokenWords [k1] + ":" + tokenOthers [k2];
						if (specialLinks.containsKey(strComposed)) {
							
							strength += specialLinks.get(strComposed);
							
						}
					}
					
				}
				
				links.put(other.getUrl(), new Float (strength));
			}
			
		}
	
		return links;
	}
	

	private Map<String,Float> calculateTextSimilarity (MessageWithProperties message) {
		
		Map<String,Float> similarities = new HashMap<String, Float> ();
		MessageSimilarity simCalculator = new MessageSimilarity_Impl ();
		Iterator<MessageWithProperties> i = messages.iterator();
		
		//
		// see if this message has enough words
		//
		if (message.hasProperty("num-words")) {
			int numWords = Integer.parseInt(message.getProperty("num-words"));
			if (numWords < MIN_NUM_WORDS) {
				
				//
				// not enough words, do not calculate this message's similarities
				//
				return similarities;
			}
		} else {
			//
			// bad message, too short. return no similarities
			//
			return similarities;
		}
		
		
		while (i.hasNext()) {
			
			MessageWithProperties other = i.next();
			if (other.hasProperty("num-words")) {
				
				int otherWords = Integer.parseInt(other.getProperty("num-words"));
				if (otherWords < MIN_NUM_WORDS) {
					
					//
					// ignore this message
					//
					continue;
				}			
			} else {
				//
				// number of words is unknown. 
				// 
				//System.out.println ("Unknown number of words. Ignoring this message");
				continue;
			}
			
			float sim = simCalculator.sim3(message, other);
			if (sim > MIN_SIMILARITY) {
				similarities.put(other.getUrl(), sim);
			}
		}
		
		return similarities;
	}
		
	/**
	 * TODO code this method
	 * @param message
	 * @return
	 */
	private Map<String,Float> calculateSynonimSimilarity (MessageWithProperties message) {
		
		return null;
	}
	
	
	private void loadSpecialWordLinks () {
		
		try {
			this.specialLinks = new HashMap<String, Float> ();
			String line = null;
			
			
			BufferedReader in = new BufferedReader (new FileReader (new File (STR_SPECIALWORD_LINK_LOCATION)));
			while ( (line = in.readLine()) != null) {
				
				String[] tokens = line.split(" ");
				for (int i = 0; i < tokens.length; i++) {
					tokens [i] = tokens [i].trim ();
				}
				
				if (tokens.length != 3) {
					
					System.out.println ("Invalid line : " + line);
					continue;
					
				}
				
				for (String t : tokens) {
					t = t.trim();
				}
				
				String strCompLink = tokens [0] + ":" + tokens [1];
				Float strength = Float.parseFloat(tokens [2]);
				specialLinks.put(strCompLink, strength);
				
				strCompLink = tokens [1] + ":" + tokens [0];
				specialLinks.put(strCompLink, strength);
			}
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace(); //TODO move to log
		}
	}
	
	
	/**
	 * Used internally to create cross-links, using similarities, between messages. This method is called
	 * while processing a new message.
	 * @param message
	 * @param otherUrl
	 * @param strength
	 * @throws IllegalStateException
	 */
	private void addCrossLinkSimilarity (MessageWithProperties message, String otherUrl, Float strength) 
						throws IllegalStateException {
		
		MessageWithProperties other = this.findMessageByUrl(otherUrl);
		if (other == null) {
			//System.out.println ("Unable to find message with url : " + otherUrl); TODO move to log
			return;
		}
		
		if (other.hasProperty("similarities")) {
			if (other.hasProperty("num-similarities")) {
				
				int numSims = Integer.parseInt(other.getProperty("num-similarities"));
				if (numSims < MAX_SIMILARITY_COUNT_SAVE) {
					
					// the simple case, where other message's similarity counts is lower than maximum allowed.
					// in this case, simply set the new property concatenating message's url and strength
					//
					String strProp = other.getProperty("similarities");
					strProp += message.getUrl() + WORD_STRENGTH_DELIMITER + strength + WORD_DB_DELIMITER;
					other.setProperty("similarities", strProp);
					
					// also increase the similarity count
					other.setProperty("num-similarities", "" + (numSims + 1));
					
				} else {
					
					// the second case, a bit more complex : the similarity count reached maximum allowed value
					// and a position must be removed first TODO
				}
				
			} else {
				
				// no similarity count. the message was not processed with this code, just throw an exception
				//
				
				throw new IllegalStateException ("Cannot find 'num-similarities' property for message : " + message.getUrl());
				
			}
		} else {
		
			// no similarities, another simple case because this would be the first similarity inserted
			//
			
			String strProp = message.getUrl() + WORD_STRENGTH_DELIMITER + strength + WORD_DB_DELIMITER;
			other.setProperty("similarities", strProp);
			
			// set the similarity count to 1
			other.setProperty("num-similarities", "1");
		}
		
		// at this point, one of the branches above has changed properties for the other message.
		// persist the object in database (in list the operation is not needed, since java works with handles)
		
		try {
			other.saveProperties(this.getConnection());
		} catch (SQLException e) {
			e.printStackTrace(); // TODO move to log
		}
	}
	
	/**
	 * Used by {@link #addCrossLinkSimilarity(MessageWithProperties, String, Float)}
	 * @param url
	 * @return
	 */
	private MessageWithProperties findMessageByUrl (String url) {
		
		Iterator<MessageWithProperties> i = messages.iterator();
		while (i.hasNext()) {
			MessageWithProperties msg = i.next();
			if (msg.getUrl().equals(url))
				return msg;
		}
		
		return null;
	}
}
