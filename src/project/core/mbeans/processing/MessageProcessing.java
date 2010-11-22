package project.core.mbeans.processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;

import project.client.persistence.Message;
import project.core.mbeans.database.ConnectionManager;
import project.persistence.properties.MessageWithProperties;
import project.utils.statistics.impl.Stemmer;

@RemoteBinding(jndiBinding = "MessageProcessing")
@Stateful
@CacheConfig(removalTimeoutSeconds=18000L)
public class MessageProcessing extends ConnectionManager implements
		MessageProcessingMBean, Runnable {

	//
	// used by tokenizer
	//
	private static final String WORD_INPUT_DELIMITERS = "[ \t\r\n:*)(,%^&*$#/~!;.?`'\"-]";
	private static final String WORD_DB_DELIMITER = ";";

	//
	// used by frequency counter
	//
	private static final String WORD_FREQ_DELIMITER = ":";

	//
	// used by 'bad message' classifier
	//
	private static final int MIN_MESSAGE_LENGTH = 10;

	private static final int MAX_SUGGESTIONS = 5;

	//
	// used by dictionary
	//
	private static final String STR_DICTIONARY_LOCATION = "/home/alexd/workspace/proiect-diploma/input/dict";

	//
	// used by spell checking
	//
	private static final String STR_SPELLCHECKER_LOCATION = "/home/alexd/workspace/proiect-diploma/input/english.0/english.0";

	//
	// used by special word algorithm
	//
	private static final String STR_SPECIALWORD_LOCATION = "/home/alexd/workspace/proiect-diploma/config/special-words.in";

	private static IDictionary dictionary = null;
	static {

		try {

			// construct the URL to the Wordnet dictionary directory
			URL url = new URL("file", null, STR_DICTIONARY_LOCATION);
			dictionary = new Dictionary(url);
			dictionary.open();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Message> queue = null;
	private boolean bRunning = true;

	private List<String> stopWords = null;

	private List<String> specialWords = null;

	private SpellChecker spellChecker = null;

	private MessageDispatcherMBean dispatcher = null;

	public void addRawMessage(Message message) {

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

	@PostConstruct
	public void start() {

		// System.out.println ("MessageProcessing started");
		queue = new LinkedList<Message>();

		// setJndiName(this.getClass().getSimpleName());

		//
		// load the stopWords
		stopWords = new LinkedList<String>();

		//
		// load the specialWords
		specialWords = new LinkedList<String>();

		//
		// load the dispatcher
		this.initDispatcher();

		try {
			this.setConnectionParams("ebas", "gwtebas", "bachelor_project");
			Connection connection = this.getConnection();

			String query = "select content from Word where labels like 'stop'";
			Statement s0 = connection.createStatement();

			ResultSet set = s0.executeQuery(query);
			while (set.next()) {
				stopWords.add(set.getString("content"));
			}

			set.close();
			s0.close();

			//
			// init the spell checker
			//
			this.initSpellCheckDictionary();

			//
			//
			BufferedReader in = new BufferedReader(new FileReader(new File(
					STR_SPECIALWORD_LOCATION)));
			String specialWord = null;
			while ((specialWord = in.readLine()) != null) {
				specialWord = specialWord.trim();
				specialWords.add(specialWord);
			}
			in.close();

			//
			// start the thread
			//
			new Thread(this).start();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@PreDestroy
	public void stop() {

		bRunning = false;
		synchronized (this) {
			this.notify();
		}

		/*
		 * try { unbind (); } catch (Exception e) { e.printStackTrace(); }
		 */

		// System.out.println ("MessageProcessing stopped");
	}

	public void run() {

		while (bRunning) {

			Message msg = null;
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
					} catch (InterruptedException e) {
					}
				}
			}

			if (msg != null) {

				//
				// Process the message
				//

				System.out.println("New message for processing:");
				System.out.println("\t> url       : " + msg.getUrl());
				// System.out.println ("\t> content   : " + msg.getContent());
				MessageWithProperties processedMessageWithProperties = this
						.processMessage(msg);

				// System.out.println ("\t> formatted : " +
				// processedMessageWithProperties.getFormattedContent());

				//
				// advance this message to the dispatcher

				//
				// let the dispatcher handle the processed message
				if (dispatcher != null) {
					dispatcher.dispatchMessage(processedMessageWithProperties);
				} else {
					System.out.println("dispatcher is null !!!");
				}

			}
		}
	}

	/**
	 * Tokenize an input string according to the {@link #WORD_INPUT_DELIMITERS}
	 * 
	 * @param input
	 * @return
	 */
	private String[] tokenize(String input) {

		//
		// lowercase the input
		//
		input = input.toLowerCase();

		//
		// replace html markers
		//
		input = this.replaceHtmlMarkers(input);

		//
		// and return the tokenization
		//
		return input.split(WORD_INPUT_DELIMITERS);
	}

	/**
	 * Replace the Html markers from input string
	 * 
	 * @param input
	 * @return
	 */
	private String replaceHtmlMarkers(String input) {

		input = input.replaceAll("&lt;", "<");
		input = input.replaceAll("&gt;", ">");
		input = input.replaceAll("&quot;", "\"");
		input = input.replaceAll("&apos;", "'");
		input = input.replaceAll("&#xd;", " ");
		input = input.replaceAll("&nbsp;", " ");

		return input;
	}

	/**
	 * Stem the word (root part of it)
	 * 
	 * @param input
	 * @return
	 */
	private String stem(String input) {

		return Stemmer.stem(input);

	}

	/**
	 * Find out if a word is stop word or not
	 * 
	 * @param input
	 *            raw input (unstemmed)
	 * @return
	 */
	private boolean isStopWord(String input) {

		// 
		// first lowercase
		input = input.toLowerCase();

		// 
		// stem it
		// input = stem (input);

		//
		// check stop words list
		return stopWords.contains(input);
	}

	private boolean isSpecialWord(String input) throws IllegalArgumentException {

		//
		// first lowercase
		input = input.toLowerCase();

		if (!input.matches("[a-z]*"))
			throw new IllegalArgumentException("Invalid input string: " + input);

		Iterator<String> i = specialWords.iterator();
		while (i.hasNext()) {

			String specialWord = i.next();
			if (input.contains(specialWord)
					&& input.length() > (specialWord.length() + 2))
				return true;

		}

		return false;
	}

	/**
	 * Given a vector of String objects, match valid words and concatenate them
	 * in a single String.
	 * 
	 * @param tokens
	 * @return
	 */
	private String accumulateTokens(String[] words) {

		List<String> validWordsList = new LinkedList<String>();
		List<Integer> validWordsPositions = new LinkedList<Integer>();
		int index = 0;

		for (String w : words) {

			w = w.trim();
			if (w.matches("[a-z]*")) {

				validWordsList.add(w);
				validWordsPositions.add(index);

			}

			index++;
		}

		String[] wordsProcessed = validWordsList
				.toArray(new String[validWordsList.size()]);

		//
		// 3.2. and create the final formatted string
		//

		index = 0;
		String strFormattedMessage = "";

		for (String w : wordsProcessed) {

			if (w.length() > 0) {

				//
				//
				if (validWordsPositions.contains(new Integer(index))) {

					if (this.isStopWord(w))
						strFormattedMessage += w + "-stop" + WORD_DB_DELIMITER;
					else if (this.isSpecialWord(w))
						strFormattedMessage += w + "-special"
								+ WORD_DB_DELIMITER;
					else {
						String suggestions = this.spellCheck(w);

						if (suggestions.equals(w))
							strFormattedMessage += w + WORD_DB_DELIMITER;
						else {

							//
							// the word is misspelled
							//

							// TODO what's next?

						}
					}

				} else {
					strFormattedMessage += WORD_DB_DELIMITER;
				}
			} else {

				strFormattedMessage += WORD_DB_DELIMITER;
			}

			index++;
		}

		//
		// 3.3. clear used memory
		//
		wordsProcessed = null;
		validWordsList.clear();
		validWordsPositions.clear();

		return strFormattedMessage;
	}

	/**
	 * Returns the word frequency, given a formatted input (after it has been
	 * processed)
	 * 
	 * @param input
	 * @return
	 */
	private Map<String, Integer> getWordFrequency(String input) {

		Map<String, Integer> frequencies = new HashMap<String, Integer>();

		//
		//
		String[] tokens = input.split(WORD_DB_DELIMITER);
		for (String t : tokens) {

			if (t.length() > 0) {

				//
				// only count frequency for words that are not 'stop' or
				// 'special'
				//
				if (t.indexOf("-") == -1) {

					Integer f = frequencies.get(t);
					if (f == null) {
						f = new Integer(1);
					} else {
						f = new Integer(f + 1);
					}

					frequencies.put(t, f);
				}
			}

		}

		tokens = null;
		return frequencies;
	}

	/**
	 * Calculate the properties for message
	 * 
	 * @param message
	 * @return
	 */
	private MessageWithProperties calculateMessageProperties(
			MessageWithProperties message) {

		if (message.getFormattedContent().equals("")) {

			message.setProperty("type", "bad-message");

		} else {

			//
			// compose the frequency string
			//
			String strFrequencies = "";

			Map<String, Integer> frequencies = this.getWordFrequency(message
					.getFormattedContent());
			Iterator<String> i = frequencies.keySet().iterator();

			while (i.hasNext()) {

				String key = i.next();
				Integer f = frequencies.get(key);

				strFrequencies += key + WORD_FREQ_DELIMITER + f
						+ WORD_DB_DELIMITER;
			}
			message.setProperty("frequencies", strFrequencies);

			//
			// calculate number of valid words
			//
			message.setProperty("num-words", "" + frequencies.size());

			//
			// compose the stop words string
			//
			String strStopWords = "";
			String[] tokens = message.getFormattedContent().split(
					WORD_DB_DELIMITER);
			for (String t : tokens) {

				if (t.indexOf("-stop") != -1) {

					strStopWords += t.substring(0, t.indexOf('-'))
							+ WORD_DB_DELIMITER;

				}

			}
			if (!strStopWords.equals(""))
				message.setProperty("stop-words", strStopWords);

			//
			// compose the special words string
			//
			String strSpecialWords = "";
			for (String t : tokens) {

				if (t.indexOf("-special") != -1) {

					strSpecialWords += t.substring(0, t.indexOf('-'))
							+ WORD_DB_DELIMITER;

				}

			}
			if (!strSpecialWords.equals(""))
				message.setProperty("special-words", strSpecialWords);
		}

		return message;
	}

	public MessageWithProperties processMessage(Message message) {

		MessageWithProperties processedMessage = new MessageWithProperties(
				message);

		if (processedMessage.getContent() != null) {
			if (processedMessage.getContent().length() > MIN_MESSAGE_LENGTH) {

				//
				//
				String[] words = this.tokenize(processedMessage.getContent());

				//
				//
				processedMessage.setFormattedContent(this
						.accumulateTokens(words));

				//
				// now calculate properties
				//
				processedMessage = this
						.calculateMessageProperties(processedMessage);

			} else {

				// This is not a valid message
				//
				System.out
						.println("\t> invalid message (message's content is too short)");
				processedMessage.setContent("");
				processedMessage.setFormattedContent("");

			}
		} else {

			// This is not a valid message
			//
			System.out
					.println("\t> invalid message (message's content is null)");
			processedMessage.setContent("");
			processedMessage.setFormattedContent("");

		}

		//
		// return the message
		return processedMessage;
	}

	private void initDispatcher() {
		if (dispatcher == null) {

			try {
				InitialContext context = new InitialContext();
				dispatcher = (MessageDispatcherMBean) context
						.lookup("MessageDispatcher");

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void initSpellCheckDictionary() {
		File dict = new File(STR_SPELLCHECKER_LOCATION);
		try {
			spellChecker = new SpellChecker(new SpellDictionaryHashMap(dict));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String spellCheck(String input) throws IllegalArgumentException {

		if (!input.contains(" ")) {
			if (spellChecker.isCorrect(input))
				return input;
			else {
				List suggestions = spellChecker.getSuggestions(input, 10);
				Iterator i = suggestions.iterator();
				int idx = 1;
				String strSuggestions = "";
				while (i.hasNext()) {

					String sugg = i.next().toString();
					strSuggestions += sugg;
					if (idx < MAX_SUGGESTIONS)
						strSuggestions += ",";

					idx++;
				}

				return strSuggestions;
			}
		} else
			throw new IllegalArgumentException(
					"spellCheck can only verify single words");
	}
}