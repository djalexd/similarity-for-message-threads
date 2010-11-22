package project.utils.statistics.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import project.client.persistence.Message;
import project.utils.statistics.MessageSimilarity;

public class MessageSimilarity_Impl implements MessageSimilarity {
	
	
	private Map<String, Integer> getMessageFrequency (Message m) {
		if (m == null || m.getFormattedContent() == null || m.getFormattedContent().length() == 0) {
			return new HashMap<String, Integer> ();
		}
		
		String str = m.getFormattedContent().trim();
		
		Map<String,Integer> map = new HashMap<String, Integer> ();
		String[] tokens = str.split(";");
		if (tokens.length == 0) {
			return new HashMap<String, Integer> ();
		}
		
		for (String t : tokens) {
			
			if (t.length() > 0) {

				if (map.containsKey(t)) {

					Integer count = map.get(t);
					map.put(t, new Integer (count + 1));

				} else {

					map.put(t, new Integer (1));

				}

			} 

		}
		
		tokens = null;
		
		return map;
	}
	
	private float length (Map<String,Integer> v) {

		float total = 0.0f;
		Iterator<String> i = v.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			int value = v.get(key);
			
			total += value * value;
		}
		
		return total;
	}
	
	
	private float distance (Map<String,Integer> v1, Map<String,Integer> v2) {
		
		float dist = 0.0f;
		
		Iterator<String> i = v1.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			if (v2.containsKey(key)) {
				int c1 = v1.get(key);
				int c2 = v2.get(key);
				
				dist += 0.5f * (c1 - c2) * (c1 - c2);
			} else {
				
				int c1 = v1.get(key);
				
				dist += c1 * c1;
			}
		}
		
		i = v2.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			if (v1.containsKey(key)) {
				int c1 = v1.get(key);
				int c2 = v2.get(key);
				
				dist += 0.5f * (c1 - c2) * (c1 - c2);
			} else {
				
				int c2 = v2.get(key);
				
				dist += c2 * c2;
			}
		}
		
		return dist;
	}
	
	private float dot (Map<String, Integer> v1, Map<String, Integer> v2) {
		
		if (v1.keySet().size() < v2.keySet().size())
			return dot (v2, v1);
				
		float total = 0.0f;
		Iterator<String> i = v2.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			if (v1.containsKey(key)) {
				total += v1.get(key) * v2.get(key);
			}
		}
		
		if (length(v1) == 0 || length(v2) == 0)
			return 0.0f;
		
		return total / (length (v1) * length (v2));
	}
	
	public float sim1(Message m1, Message m2) {
		
		if (m1 == null || m2 == null)
			return 0.0f;
		
		if (m1.getFormattedContent() == null || m2.getFormattedContent() == null)
			return 0.0f;
		
		if (m1.getFormattedContent().equals(m2.getFormattedContent()))
			return 1.0f;
		
		float dist = this.distance(this.getMessageFrequency(m1), this.getMessageFrequency(m2));
		return (float) Math.exp(-dist * dist);
	}

	
	public float sim2(Message m1, Message m2) {
		
		if (m1 == null || m2 == null)
			return 0.0f;
		
		if (m1.getFormattedContent() == null || m2.getFormattedContent() == null)
			return 0.0f;
		
		if (m1.getFormattedContent().equals(m2.getFormattedContent()))
			return 1.0f;		
		
		float dist = this.distance(this.getMessageFrequency(m1), this.getMessageFrequency(m2));
		return 1.0f / (1.0f + dist);
	}

	
	public float sim3(Message m1, Message m2) {
		
		if (m1 == null || m2 == null)
			return 0.0f;
		
		if (m1.getFormattedContent() == null || m2.getFormattedContent() == null ||
			m1.getFormattedContent().length() == 0 || m2.getFormattedContent().length() == 0)
			return 0.0f;
		
		if (m1.getFormattedContent().equals(m2.getFormattedContent()))
			return 1.0f;		
				
		return this.dot(this.getMessageFrequency(m1), this.getMessageFrequency(m2));
	}

	
	public float sim4(Message m1, Message m2) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public float sim5(Message m1, Message m2) {
		// TODO Auto-generated method stub
		return 0;
	}
}
