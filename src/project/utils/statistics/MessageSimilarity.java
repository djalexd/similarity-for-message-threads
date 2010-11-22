package project.utils.statistics;

import project.client.persistence.Message;

public interface MessageSimilarity {

	public float sim1 (Message m1, Message m2);
	
	public float sim2 (Message m1, Message m2);

	public float sim3 (Message m1, Message m2);
	
	public float sim4 (Message m1, Message m2);
	
	public float sim5 (Message m1, Message m2);		
}
