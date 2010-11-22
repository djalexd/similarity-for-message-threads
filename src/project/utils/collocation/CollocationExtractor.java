package project.utils.collocation;

import project.client.persistence.Message;

public interface CollocationExtractor {

	/**
	 * Given a specific list of words, previously transformed to 
	 * lowercase (and probably filtered), returns the collocation
	 * matrix. The matrix is determined by the number of words,
	 * has 0 values below the main diagonal and all results are
	 * scaled between (min,max) values of the entire matrix to the
	 * interval (0,1).
	 * @param words
	 * 
	 * @return
	 */
	public Matrix<WordStatistics> calculateCollocationMatrix (Message sentence);
	
	
	public Matrix<WordStatistics> calculateCollocationMatrix (Message sentence, int maxDistance);
	
	
	/**
	 * With the matrix calculated using previous method, you can find collocations between
	 * different words. This is only a shorthand encapsulation.
	 * @param collocation
	 * @param words
	 * @param w1
	 * @param w2
	 * @return
	 * @throws NumberFormatException
	 */
	public float determineCollocation (Matrix<WordStatistics> collocation, 
									   Message sentence, project.client.persistence.Word w1, project.client.persistence.Word w2)
						throws NumberFormatException;
}
