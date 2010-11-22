package project.core.mbeans.processing;

import javax.ejb.Remote;

import project.client.persistence.Message;
import project.persistence.properties.MessageWithProperties;

/**
 * Second layer in message processing. This layer contains:<br/>
 * <lu>
 * <li>string tokenization</li>
 * <li>dictionary</li>
 * <li>stop words</li>
 * <li>word stemming</li> 
 * <li>word frequency counter</li>
 * <li>word collocation algorithms</li>
 * <li>special words</li>
 * <li>word spelling</li> 
 * </lu>
 * <br/>
 * This layer will accumulate the raw messages in a queue waiting to be processed.
 * <br/>If no messages are available, pause this thread.
 * 
 * @author Alex Dobjanschi
 * @since 29.05.2009
 */
@Remote
public interface MessageProcessingMBean {
	
	/**
	 * 
	 * @throws Exception
	 */
	public void start ();
	
	/**
	 * 
	 * @throws Exception
	 */
	public void stop ();

	/**
	 * Add a raw message to this layer
	 * @param message
	 */
	public void addRawMessage (Message message);
	
	/**
	 * Find out the length of waiting queue
	 * @return
	 */
	public int getQueueLength ();
	
	public MessageWithProperties processMessage (Message message);	
}
