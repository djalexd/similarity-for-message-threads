package project.core.mbeans.processing;

import javax.ejb.Remote;

import project.persistence.properties.MessageWithProperties;

@Remote
public interface MessageLinkProcessorMBean {

	public void start ();
	
	public void stop ();
	
	/**
	 * 
	 * @param message
	 */
	public void addProcessedMessage (MessageWithProperties message);
	
	/**
	 * 
	 * @return
	 */
	public int getQueueLength ();
	
	public MessageWithProperties processMessage (MessageWithProperties message);
}
