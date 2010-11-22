package project.persistence.builder.impl;

import java.util.Iterator;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Parser;

import project.client.persistence.Message;
import project.client.persistence.MessageThread;
import project.persistence.builder.MessageThreadCrawler;


public class GoogleGroupsThreadCrawler extends BaseHttpClient implements MessageThreadCrawler {

	private MessageThread messageThread;
	private boolean read;

	public GoogleGroupsThreadCrawler () {
		//
		// do nothing, used by
		
		this.read = false;
    	System.out.println ("Warning! The default (no parameter) constructor of " + 
    			this.getClass().getName() + " is meant to be used only by the MBean crawler. Do not use it directly, as it doesn't init anything!!");
		
	}
	
	public GoogleGroupsThreadCrawler (Object[] params) {

		this.initCrawler(params);
		
	}
	
	public List<Message> extractMessages() {
		
		List<Message> msgs = null;
		
		try {
			HttpHost target = new HttpHost(this.messageThread.getMessageBoard().getUrl(), 80, "http");
			HttpClient client = createHttpClient();
			HttpRequest req = createRequest("/group/" + this.messageThread.getMessageBoard().getName() + 
											"/browse_thread/thread/" + this.messageThread.getUrl());

			//System.out.println("executing request to " + target + ": " + req.getRequestLine().getUri());
			HttpEntity entity = null;
			try {
				HttpResponse rsp = client.execute(target, req);
				entity = rsp.getEntity();

				if (entity != null) {
					String content = EntityUtils.toString(entity);		
					
					content = content.replaceAll("<br>", "\n");
					content = content.replaceAll("&lt;", "<");
					content = content.replaceAll("&gt;", ">");
					content = content.replaceAll("&nbsp;", " ");
					content = content.replaceAll("<p>", "\n\n");
					content = content.replaceAll("&quot;", "\"");
					
			        Parser parser = new Parser (content);
			        MyVisitor v = new MyVisitor ();
			        parser.visitAllNodesWith(v);
			        
			        msgs = v.getMessages();
			        
			        /*
			        Iterator<Message> i = msgs.iterator();
			        while (i.hasNext()) {
			        	Message msg = i.next();
			        	System.out.println ("--------------------------------------------------");
			        	System.out.println ("Author: " + msg.getUser().getName());
			        	System.out.println ("Content: " + msg.getContent());
			        }
			        */
										
					content = null;
					
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
		
		
		return msgs;
	}

	public void initCrawler(Object[] params) throws IllegalArgumentException {
		
		if (params == null)
			throw new IllegalArgumentException ("init params are null");
		
		if (params.length != 1)
			throw new IllegalArgumentException ("invalid number of params (expected 1, found " + params.length + ")");

		
		Class<?>[] classes = new Class<?>[] {
				MessageThread.class
		};
		
		for (int i = 0; i < params.length; i++) {
			if (!params [i].getClass().equals(classes [i])) {
				throw new IllegalArgumentException ("Invalid argument (expected class " + 
													classes [i].getSimpleName() + ", found " + 
													params [i].getClass().getSimpleName() + ")");
			}
		}		
		
		this.messageThread = (MessageThread) params [0];
		read = false;
	}

	public boolean hasNext() {
		return (!read);
	}

	public MessageThreadCrawler next() {
		this.read = true;
		return this;
	}

	public void remove() {
		/* Not implemented */
	}
}
