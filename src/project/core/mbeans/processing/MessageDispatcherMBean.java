package project.core.mbeans.processing;

import javax.ejb.Remote;

import project.persistence.properties.MessageWithProperties;

/**
 * Simple layer that dispatches a message. This is the
 * next step after basic processing (done with {@link MessageProcessingMBean})
 * @author Alex Dobjanschi
 * @since 30.05.2008
 */
@Remote
public interface MessageDispatcherMBean {

	public void start ();
	
	public void stop ();
	
	/**
	 * 
	 * @param message
	 */
	public void dispatchMessage (MessageWithProperties message);
}
