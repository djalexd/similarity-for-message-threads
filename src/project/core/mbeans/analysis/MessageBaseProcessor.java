package project.core.mbeans.analysis;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

import project.client.persistence.Message;
import project.core.mbeans.database.ConnectionManagerMysqlImpl;
import project.core.mbeans.processing.MessageProcessingMBean;
import project.core.persistence.PersistenceLoaderMBean;
import project.persistence.properties.MessageWithProperties;
import project.utils.statistics.impl.Stemmer;

@Stateful
@RemoteBinding(jndiBinding="MessageBaseProcessor")
@CacheConfig(removalTimeoutSeconds=18000L)
public class MessageBaseProcessor extends ConnectionManagerMysqlImpl implements MessageBaseProcessorMBean {

	private static final String WORD_INPUT_DELIMITERS = "[ \t\r\n:*)(,%^&*$#/~!;.?`'\"-]";	
	private static final String STR_DICTIONARY_LOCATION = "/home/alexd/workspace/proiect-diploma/input/dict";

	private Connection connection = null;
	private static IDictionary dictionary = null;
	
	private PersistenceLoaderMBean loader = null;
	private MessageProcessingMBean processor = null;

	static {

		try {

			// construct the URL to the Wordnet dictionary directory
			URL url = new URL ("file", null, STR_DICTIONARY_LOCATION);
			dictionary = new Dictionary (url);
			dictionary.open();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PostConstruct
	public void start() {
		System.out.println ("MessageBaseProcessor started");
		
		try {
			
			this.setConnectionParams("ebas", "gwtebas", "bachelor_project");
			connection = this.getConnection();
			
			InitialContext context = new InitialContext ();
			loader = (PersistenceLoaderMBean) context.lookup("PersistenceLoader");
			
			//
			//
			//
			int n1 = 75, n2 = 120;
			
			System.out.println ("n1 = " + n1 + ", n2 = " + n2);
			//getWordStatistics(n1,n2);
			
			n1 = 1234; n2 = 2854;
			System.out.println ("n1 = " + n1 + ", n2 = " + n2);
			//getWordStatistics(n1, n2);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}


	@PreDestroy
	public void stop() {
		
		dictionary.close();
		try {		
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace(); // TODO move to log
		}
		
		System.out.println ("MessageBaseProcessor stopped");		
	}

	

	public String[] getSynonyms(String w) throws IOException {

		List<String> result = new LinkedList<String> ();

		IIndexWord idxWord = dictionary.getIndexWord(w, POS.NOUN);
		if (idxWord == null)
			return result.toArray(new String [result.size()]);

		List<IWordID> list = idxWord.getWordIDs();

		Iterator<IWordID> i = list.iterator();
		while (i.hasNext()) {
			IWordID wordID = i.next();
			IWord word = dictionary.getWord(wordID);

			ISynset synset = word.getSynset();
			for (IWord w1 : synset.getWords ()) {
				if (!w1.getLemma().toLowerCase().equals(w.toLowerCase()))
					result.add(w1.getLemma().toLowerCase());
			}
		}

		return result.toArray(new String [result.size()]);
	}


	
	public Map<String, String> getWordStatistics (int minMsgId, int maxMsgId) {
		
		int numInvalidWords = 0;
		int numStopWords = 0;
		int numWords = 0;
		
		List<String> words = new LinkedList<String> ();
		Map<String,Integer> invalid = new HashMap<String, Integer> ();
		SortedMap<Integer,List<String>> invalidSorted = new TreeMap<Integer, List<String>> (
				new Comparator<Integer> () {
					public int compare (Integer f1, Integer f2) {
						if (f1 == null && f2 == null)
							return 0;
						else if (f1 == null && f2 != null)
							return 1;
						else if (f1 != null && f2 == null)
							return -1;
						
						if (f1 < f2)
							return 1;
						else if (f1 > f2)
							return -1;
						else
							return 0;
					}
				});
		
		try {
			List<Message> messages = loader.loadMessages(minMsgId, maxMsgId);
			Iterator<Message> i = messages.iterator();
			while (i.hasNext()) {
				
				Message msg = i.next();
				String[] tokens = msg.getContent().split(WORD_INPUT_DELIMITERS);
				
				numWords += tokens.length;
				for (String t : tokens) {
					
					t = t.toLowerCase();
					if (!t.matches("[a-z]*")) {
						numInvalidWords ++;
						if (invalid.get(t) == null) {
							invalid.put(t, new Integer (1));
						} else {
							invalid.put(t, invalid.get(t) + 1);
						}
						continue;
					}
					
					if (loader.isStopWord(t)) {
						numStopWords ++;
						continue;
					}
					
					String t1 = Stemmer.stem(t);
					if (!words.contains(t1)) {
						words.add(t1);
					}
				}
			}
			
			System.out.println("# words = " + numWords);
			System.out.println("# invalid = " + numInvalidWords);
			System.out.println("# stop = " + numStopWords);
			System.out.println ("# after stemming = " + words.size());
			
			float minThreshold = 0.0001f;
			Iterator<String> j = invalid.keySet().iterator();
			while (j.hasNext()) {
				
				String inv = j.next();
				int count = invalid.get(inv);
				
				if (invalidSorted.get(invalid.get(inv)) == null) {
					List<String> l = new LinkedList<String> ();
					l.add(inv);
					invalidSorted.put(count, l);
				} else {
					List<String> l = invalidSorted.get(count);
					l.add(inv);
					invalidSorted.put(count, l);
					
				}				
			}
			
			Iterator<Integer> k = invalidSorted.keySet().iterator();
			while (k.hasNext()) {
				Integer c = k.next();
				System.out.println ("For count " + c + " :");
				System.out.println (invalidSorted.get(c).toString());
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
