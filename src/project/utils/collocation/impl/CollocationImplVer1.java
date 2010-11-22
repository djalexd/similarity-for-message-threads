package project.utils.collocation.impl;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import project.client.persistence.Message;
import project.client.persistence.Word;
import project.utils.collocation.CollocationExtractor;
import project.utils.collocation.Matrix;
import project.utils.collocation.WordStatistics;

public class CollocationImplVer1 implements CollocationExtractor {
	
	private static final int MAX_DISTANCE = 10;
	private int distance = MAX_DISTANCE;
	
	private static Map<String,Integer> wordIndices = new HashMap<String, Integer> ();
	private static int crtIdx = -1;
	
	
	public CollocationImplVer1 () {}
	public CollocationImplVer1 (int distance) {
		this.distance = distance;
	}

	
	public Matrix<WordStatistics> calculateCollocationMatrix(Message message, int maxDistance) {
		
		this.distance = maxDistance;
		return calculateCollocationMatrix(message);
		
	}

	public Matrix<WordStatistics> calculateCollocationMatrix(Message message) {
		
		/**
		 * Algorithm is as follows
		 * 
		 *  1. Create the matrix. This can become quite memory consuming,
		 *  since it's stored as N(words) ^ 2 * 4 bytes (float)
		 *  
		 *  2. Loop through all words, from 1 to N.
		 *           - for each one, loop from distance/2 (behing) to distance/2 (ahead)
		 *           - for the given pair, calculate mean and variance :) - 2 matrices
		 *           
		 *  3. Return the matrix
		 */
		
		//System.out.println ("Analyzing " + sentence.getWords().size() + " words");
		//System.out.println (words);

		String[] words = message.getFormattedContent().split(";");
		//Iterator<Word> i = sentence.getWords().iterator();
		//while (i.hasNext()) {
		for (String w : words) {
			//Word w = i.next();
			if (!wordIndices.containsKey(w)) {
				crtIdx ++;
				Integer wIdx = new Integer (crtIdx);
				
				wordIndices.put(w, wIdx);
			}
		}
		
		//System.out.println ("Matrix has " + counts.size() + " x " + counts.size() + " elements");
		
		// Allocate the matrix
		Matrix<WordStatistics> m = new Matrix<WordStatistics> ();
		
		//Word[] vecWords = sentence.getWords().toArray(new Word [sentence.getWords().size()]);
		
		//Loop through all words
		for (int j = 0; j < words.length; j++) {
			
			String w1 = words [j];
			Integer p1 = wordIndices.get(w1);
			
			for (int k = j + 1; k < (j + 1 + distance) ; k++) {
				if (k >= words.length)
					break;
				
				String w2 = words [k];
				Integer p2 = wordIndices.get(w2);
				float dist = k - j;				

				WordStatistics stats = m.getValue(p1, p2);
				if (stats == null) {
					stats = new WordStatistics ();

					stats.setW1(w1);
					stats.setW2(w2);
				}
				
				stats.addOffset((float) dist);
				stats.updateStats();
				m.setValue(p1, p2, stats);
			}
		}
		
		/*
		Iterator<Point> j = m.getValues().keySet().iterator();
		while (j.hasNext()) {
			Point p = j.next();
			WordStatistics stats = m.getValues().get(p);
			
			stats.updateStats();
		}
		*/
		
		//System.out.println ("Matrix has " + m.getSize() + " non-null elements (out of " + (counts.size() * counts.size()) + ")");		
		return m;
	}

	
	public float determineCollocation(Matrix<WordStatistics> collocation,
			Message message, Word w1, Word w2) throws NumberFormatException {
		return 0;
	}

}
