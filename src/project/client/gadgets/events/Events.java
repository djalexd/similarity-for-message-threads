package project.client.gadgets.events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Events {

	private static Map<String, List<EventListener>> registeredListeners = null;
	static {
		registeredListeners = new HashMap<String, List<EventListener>> ();
	}
	
	
	public static void addListener (String name, EventListener listener) {
		if (registeredListeners.get(name) != null) {
			registeredListeners.get(name).add(listener);
		} else {
			List<EventListener> list = new LinkedList<EventListener> ();
			list.add(listener);
			registeredListeners.put(name, list);
		}
	}
	
	public static void remove (String name, EventListener listener) {
	
		//TODO not yet implemented
		
		
	}
	
	
	public static void throwEvent (String name, List<String> params) {
		
		List<EventListener> listeners = registeredListeners.get(name);
		if (listeners != null) {
			Iterator<EventListener> i = listeners.iterator();
			while (i.hasNext()) {
				EventListener l = i.next();
				l.handleEvent(name, params);
			}
		}
		
	}
}
