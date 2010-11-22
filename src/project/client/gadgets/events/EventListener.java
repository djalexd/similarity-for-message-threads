package project.client.gadgets.events;

import java.util.List;

/**
 * 
 * @author Alex Dobjanschi
 * @since 17.06.2009
 *
 */
public interface EventListener {

	/**
	 * Handle an event
	 * @param name
	 * @param params
	 */
	public void handleEvent (String name, List<String> params);
}
