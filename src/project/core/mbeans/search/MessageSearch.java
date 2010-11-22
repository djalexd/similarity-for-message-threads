package project.core.mbeans.search;

import java.io.IOError;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;

import project.client.persistence.Message;
import project.client.persistence.MessageThread;
import project.client.persistence.User;
import project.core.mbeans.analysis.MessageBaseProcessorMBean;
import project.core.persistence.PersistenceLoaderMBean;
import project.persistence.properties.MessageWithProperties;
import project.utils.statistics.MessageSimilarity;
import project.utils.statistics.impl.MessageSimilarity_Impl;

@Stateful
@RemoteBinding(jndiBinding="MessageSearch")
@CacheConfig(removalTimeoutSeconds=18000L)
public class MessageSearch implements MessageSeachMBean {
	
	private static int MIN_NUM_WORDS = 10;

	private boolean bReloadMessageEveryQuery;
	private List<MessageWithProperties> messages;
	
	private PersistenceLoaderMBean loader;
	private MessageBaseProcessorMBean base;
	
	
	@PostConstruct
	public void start() {

		try {
			
			InitialContext context = new InitialContext ();
			loader = (PersistenceLoaderMBean) context.lookup("PersistenceLoader");
			base   = (MessageBaseProcessorMBean) context.lookup("MessageBaseProcessor");
			
			bReloadMessageEveryQuery = false;
			messages = new LinkedList<MessageWithProperties> ();
		
			this.loadMessages();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	
	public List<Message> search(String[] keywords, int limit, boolean lookUser) 
	throws IllegalArgumentException {

		if (keywords == null)
			throw new IllegalArgumentException ("query is null!");

		if (keywords.length == 0)
			throw new IllegalArgumentException ("query is empty!");

		System.out.println ("Query: ");
		for (int i = 0; i < keywords.length; i++) {
			System.out.println ("  > " + keywords [i]);
		}

		if (bReloadMessageEveryQuery)
			this.loadMessages();


		Map<User, Float> userRelevance = new HashMap<User, Float> (); 

		
		SortedMap<Float, List<MessageWithProperties>> sortedMap = new TreeMap<Float, List<MessageWithProperties>> 
		(new Comparator<Float> () {
			public int compare (Float f1, Float f2) {
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

		List<String> synonims = new LinkedList<String> ();
		for (int i = 0; i < keywords.length; i++) {
			try {

				String[] foundSynonims = base.getSynonyms(keywords [i]);
				for (int j = 0; j < foundSynonims.length; j++) {

					if (!synonims.contains(foundSynonims [j])) {
						synonims.add(foundSynonims [j]);
					}

				}

			} catch (IOException e) {

			}
		}

		for (int i = 0; i < keywords.length; i++) {	
			synonims.remove(keywords [i]);
		}

		Message queryMsg   = new Message ();		
		Message synonimMsg = new Message ();

		String str = "";
		for (int i = 0; i < keywords.length; i++) {
			str += keywords [i] + ";";
		}
		queryMsg.setFormattedContent(str);


		str = "";
		System.out.println ("Synonim query: ");		
		for (int i = 0; i < synonims.size(); i++) {
			str += synonims.get(i) + ";";
			System.out.println ("  > " + synonims.get(i));			
		}
		synonimMsg.setFormattedContent(str);
		synonims.clear();

		MessageSimilarity calculator = new MessageSimilarity_Impl ();
		Iterator<MessageWithProperties> i = null;

		if (lookUser) {
			
			
			i = messages.iterator();
			while (i.hasNext()) {

				MessageWithProperties msg = i.next();
				float rel = relevance(msg, calculator, queryMsg, synonimMsg);
				msg.setRelevance(rel);

				User u = loader.getMessageUser(msg.getId());

				if (userRelevance.get(u) == null) {
					userRelevance.put(u, rel);
				} else {
					userRelevance.put(u, userRelevance.get(u) + rel);
				}				
			}
			
			Iterator<User> u_i = userRelevance.keySet().iterator();
			while (u_i.hasNext()) {
				User u = u_i.next();
				if (userRelevance.get(u) == 0.0f)
					u_i.remove();
			}

			i = messages.iterator();
			while (i.hasNext()) {
				MessageWithProperties msg = i.next();
				User u = loader.getMessageUser(msg.getId());
				
				float userRel = 0.0f;
				if (userRelevance.get(u) != null)
					userRel = userRelevance.get(u);
				
				float rel = 0.3f * relevance(msg, calculator, queryMsg, synonimMsg) + 
				0.7f * userRel;

				msg.setRelevance(rel);

				if (sortedMap.get(rel) == null) {
					List<MessageWithProperties> l = new LinkedList<MessageWithProperties> ();
					l.add(msg);
					sortedMap.put(rel, l);
				} else {
					sortedMap.get(rel).add(msg);
				}

			}
		} else {

			i = messages.iterator();
			while (i.hasNext()) {
				MessageWithProperties msg = i.next();
				float rel = relevance(msg, calculator, queryMsg, synonimMsg);

				msg.setRelevance(rel);

				if (sortedMap.get(rel) == null) {
					List<MessageWithProperties> l = new LinkedList<MessageWithProperties> ();
					l.add(msg);
					sortedMap.put(rel, l);
				} else {
					sortedMap.get(rel).add(msg);
				}

			}
		}

		//
		// remove irrelevant keys
		//
		sortedMap.remove(0.0f);

		List<Message> list = new LinkedList<Message> ();
		Iterator<Float> j = sortedMap.keySet().iterator();
		while (j.hasNext()) {
			if (limit != -1 && list.size() > limit)
				break;

			Float relevance = j.next();
			List<MessageWithProperties> l = sortedMap.get(relevance);

			Iterator<MessageWithProperties> k = l.iterator();
			while (k.hasNext()) {
				if (limit != -1 && list.size() > limit) {
					
					float minRelevance = 1.0f, maxRelevance = 0.0f;
					Iterator<Message> k1 = list.iterator();
					while (k1.hasNext()) {
						Message msg = k1.next();
						if (msg.getRelevance() < minRelevance)
							minRelevance = msg.getRelevance();
						
						if (msg.getRelevance() > maxRelevance)
							maxRelevance = msg.getRelevance();
					}
					
					System.out.println ("min relevance = " + minRelevance + ", max relevance = " + maxRelevance);
					if (maxRelevance != minRelevance) {
						float mult = 1.0f / (maxRelevance - minRelevance);

						k1 = list.iterator();
						while (k1.hasNext()) {
							Message msg = k1.next();
							msg.setRelevance((msg.getRelevance() - minRelevance) * mult);
							System.out.println ("update relevance to " + msg.getRelevance());
						}
					}
					
					return list;
				}
					

				MessageWithProperties msg = k.next();
				list.add(createMessageFromMessageWithProperties (msg, queryMsg.getFormattedContent().split(";"),
						synonimMsg.getFormattedContent().split(";")));

				System.out.println ("Message " + msg.getUrl() + " : " + relevance);

			}
		}

		float minRelevance = 1.0f, maxRelevance = 0.0f;
		Iterator<Message> k = list.iterator();
		while (k.hasNext()) {
			Message msg = k.next();
			if (msg.getRelevance() < minRelevance)
				minRelevance = msg.getRelevance();
			
			if (msg.getRelevance() > maxRelevance)
				maxRelevance = msg.getRelevance();
		}
		
		System.out.println ("min relevance = " + minRelevance + ", max relevance = " + maxRelevance);
		if (maxRelevance != minRelevance) {
			float mult = 1.0f / (maxRelevance - minRelevance);

			k = list.iterator();
			while (k.hasNext()) {
				Message msg = k.next();
				msg.setRelevance((msg.getRelevance() - minRelevance) * mult);
				System.out.println ("update relevance to " + msg.getRelevance());
			}
		}
		
		return list;
	}

	
	private float relevance (MessageWithProperties message, MessageSimilarity calculator, Message queryMessage, Message synonimMessage) {
		
		if (message.getNumWords() < MIN_NUM_WORDS)
			return 0.0f;
		
		return 0.7f * calculator.sim3(message, queryMessage) + 
			   0.3f * calculator.sim3(message, synonimMessage);

		// TODO improve this, by taking into account the strength of message's owner wrt the
		// network, etc
	}
	
	
	private synchronized void loadMessages () {
		messages.clear();
		
		try {
			Date d1 = new Date ();
			messages = loader.loadMessagesWithProperties (null);
			Date d2 = new Date ();
			System.out.println ("Finished loading the messages (time = " + (d2.getTime() - d1.getTime()) + " miliseconds, # messages = " + messages.size() + ")");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Message createMessageFromMessageWithProperties (MessageWithProperties message, String[] query, String[] synonims) {
		
		Message msg = new Message ();
		
		String str = message.getContent();
		for (int i = 0; i < query.length; i++) {
			Pattern p = Pattern.compile(query [i], Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(str);
			while (m.find()) {
				if (str.indexOf("<span style=\"background-color:yellow\">" + m.group() + "</span>") == -1)
					str = str.replaceAll(m.group(), "<span style=\"background-color:yellow\">" + m.group() + "</span>");
			}
		}
		
		for (int i = 0; i < synonims.length; i++) {
			Pattern p = Pattern.compile(synonims [i], Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(str);
			while (m.find()) {
				if (str.indexOf("<span style=\"background-color:lightblue\">" + m.group() + "</span>") == -1 &&
					str.indexOf("<span style=\"background-color:yellow\">" + m.group() + "</span>") == -1)
					str = str.replaceAll(m.group(), "<span style=\"background-color:lightblue\">" + m.group() + "</span>");
			}			
		}

		msg.setContent(str);
		msg.setFormattedContent(message.getFormattedContent());
		msg.setId(message.getId());
		msg.setMessageThread(message.getMessageThread());
		msg.setParent(message.getParent());
		msg.setPublishDate(message.getPublishDate());
		msg.setUrl(message.getUrl());
		msg.setUser(message.getUser());
		msg.setMessageThread(loader.getMessageThread(msg.getId()));
		msg.setUser(loader.getMessageUser(msg.getId()));
		msg.setRelevance(message.getRelevance());
		
		return msg;
	}
}
