package project.utils.collocation;

import java.util.List;


public interface CollocationCumulator {

	/**
	 * Add a new collocation result (previously calculated)
	 * @param collocation
	 */
	public void addCollocation (Matrix<WordStatistics> collocation);
	
	
	/**
	 * Clear all collocations 
	 */
	public void clearCollocations ();
	
	/**
	 * Returns the collocation matrix, calculated so far
	 * @return
	 */
	public Matrix<WordStatistics> getCollocationMatrix ();
	
	/**
	 * Shorthand method to {@link #getCollocations(int)} with param '-1' (no limit)
	 * @see {@link #getCollocations(int)}
	 * @return
	 */
	public List<WordStatistics> getCollocations ();
	
	/**
	 * 
	 * @param limit An optional limit (upper bound) of the size of returned list. -1 if no limit is set
	 * @return
	 */
	public List<WordStatistics> getCollocations (int limit);
}
