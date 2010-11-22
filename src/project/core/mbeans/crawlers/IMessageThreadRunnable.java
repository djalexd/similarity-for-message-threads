package project.core.mbeans.crawlers;

public interface IMessageThreadRunnable extends Runnable {

	/**
	 * Causes the thread to 'pause' (sleep / wait) until
	 * a call to 'activate' is done
	 * @param force When true, forces processing to be canceled
	 * (hence, current message / list of messages that are about
	 * to be inserted in DB will not be - but still their status
	 * is saved)
	 */
	public void pause (boolean force);
	
	/**
	 * Activate the thread. A second call (or calls when thead is
	 * active) will have no effects.
	 * 
	 * @see {@link #pause(boolean)}
	 */
	public void activate ();
	
	
	/**
	 * Permanently terminates this thread (terminates its 'Run' method
	 * loop).
	 * @param force
	 */
	public void stop (boolean force);
}
