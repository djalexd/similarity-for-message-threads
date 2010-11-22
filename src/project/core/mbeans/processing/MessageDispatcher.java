package project.core.mbeans.processing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;

import project.persistence.properties.MessageWithProperties;

@RemoteBinding(jndiBinding="MessageDispatcher")
@Stateful
@CacheConfig(removalTimeoutSeconds=18000L)
public class MessageDispatcher implements MessageDispatcherMBean {
	
	private MessageLinkProcessorMBean linkProcessor = null;

	@PostConstruct
	public void start() {
		//System.out.println ("Dispatcher started");
		
		try {
			InitialContext context = new InitialContext ();
			linkProcessor = (MessageLinkProcessorMBean) context.lookup("MessageLinkProcessor");
		} catch (Exception e) {
			e.printStackTrace(); // TODO move to log
		}
	}

	@PreDestroy
	public void stop() {
		//System.out.println ("Dispatcher stopped");
	}	
	
	public void dispatchMessage(MessageWithProperties message) {
		
		if (message.hasProperty("type")) {
			if (message.getProperty("type").equals("bad-message")) {
				// print this
				System.out.println ("Invalid message :");
				System.out.println ("\t> url    : " + message.getUrl());
				System.out.println ("\t> user   : " + message.getUser());
				if (message.getMessageThread() != null)
					System.out.println ("\t> thread : " + message.getMessageThread().getName());
			}
		} else {

			// dispatch towards the persistent layer 
			//
			linkProcessor.addProcessedMessage(message);
		}
	}
}
