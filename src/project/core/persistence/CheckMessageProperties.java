package project.core.persistence;

import java.util.Iterator;
import java.util.Map;

import project.client.persistence.Message;
import project.core.mbeans.processing.MessageLinkProcessor;
import project.core.mbeans.processing.MessageLinkProcessorMBean;
import project.core.mbeans.processing.MessageProcessing;
import project.core.mbeans.processing.MessageProcessingMBean;
import project.persistence.properties.MessageWithProperties;

public class CheckMessageProperties {
	PersistenceLoaderMBean loader = new PersistenceLoader ();
	MessageProcessingMBean lvl_one = new MessageProcessing ();
	MessageLinkProcessorMBean lvl_two = new MessageLinkProcessor ();
	
	public CheckMessageProperties () {
		loader.start();
		lvl_one.start();
		lvl_two.start();
	}

	public void testProperties (int msgId) {
		try {
			Message msg = loader.loadMessage(msgId);
			
			if (msg != null) {
				MessageWithProperties processed = lvl_one.processMessage(msg);
				lvl_two.addProcessedMessage(processed);
				/*
				processed = lvl_two.processMessage(processed);
				
				System.out.println ("formatted : " + processed.getFormattedContent());
				
				Map<String,String> props = processed.getProperties();	
				Iterator<String> i = props.keySet().iterator();
				while (i.hasNext()) {
					String key = i.next();
					String val = props.get(key);
					
					System.out.println (key + " => " + val);
				}
				
				processed.saveProperties(loader.getConnection());
				*/
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		CheckMessageProperties p = new CheckMessageProperties ();
		p.testProperties(1);
	}
}
