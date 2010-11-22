package project.utils.collocation.impl;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import project.utils.collocation.CollocationCumulator;
import project.utils.collocation.Matrix;
import project.utils.collocation.WordStatistics;

public class CollocationCumulator_Impl implements CollocationCumulator {
	
	private Matrix<WordStatistics> matrix;
	
	public CollocationCumulator_Impl () {
		matrix = new Matrix<WordStatistics> ();
	}

	
	public void addCollocation(Matrix<WordStatistics> collocation) {
		
		Iterator<Point> i = collocation.getValues().keySet().iterator();
		
		while (i.hasNext()) {
			Point pt = i.next();
			
			WordStatistics toInsert = collocation.getValue (pt.x, pt.y);
			WordStatistics currentStats  = matrix.getValue (pt.x, pt.y);
			
			if (currentStats != null) {
				
				//
				// The value already exists there, so update ;) 
				//
				
				for (int j = 0; j < toInsert.getNumOccurences (); j++) {
					
					currentStats.addOffset(toInsert.getOffset());
				}
				
				currentStats.updateStats();
				
				matrix.setValue(pt.x, pt.y, currentStats);
			} else {
				
				//
				// A new value was found, just insert it
				//
				matrix.setValue(pt.x, pt.y, toInsert);
			}
		}

		
	}

	
	public void clearCollocations() {
		
		this.matrix.getValues().clear();
		
	}

	
	public List<WordStatistics> getCollocations() {
		
		return this.getCollocations (-1);
	}

	
	
	public List<WordStatistics> getCollocations(int limit) {

		System.out.println ("getCollocations");
		System.out.println ("\t> removing 1 occurence collocations");
		Iterator<Point> i = matrix.getValues().keySet().iterator();
		while (i.hasNext()) {
			
			Point pt = i.next();
			WordStatistics stats = matrix.getValues().get(pt);
			
			//
			// Remove collocations with 1 or 0 (this will never be the case) occurences
			//
			if (stats.getNumOccurences() < 1) {
				i.remove();
			}
		}
		System.out.println ("\t> done");

		int size = matrix.getValues().size();
		System.out.println ("Remaining " + size + " collocations");
		
		List<WordStatistics> list = new LinkedList<WordStatistics> ();
		//List<WordStatistics> list = 
		//			Arrays.asList(matrix.getValues().values().toArray(new WordStatistics [size]));
		
		Comparator<WordStatistics> comp = new Comparator<WordStatistics> () {
			public int compare(WordStatistics o1, WordStatistics o2) {
				
				return o2.getNumOccurences() - o1.getNumOccurences();
			}
		};
		
		Iterator<WordStatistics> j = matrix.getValues().values().iterator();
		while (j.hasNext()) {
			
			WordStatistics s = j.next();
			list.add(s);
			
			Collections.sort(list, comp);
			if (limit > 0) {
				
				if (list.size() > limit) {
					
					while (list.size() > limit) {
						list.remove(list.size() - 1);
					}
					
				}
				
			}
		}
		
		//
		// Perform the sort, by number of occurences (a larger number will most probably mean
		// the collocation is more accurate)
		//
		//Collections.sort(list, comp);
		
		//
		// Crop the list (if that's the case)
		//
		//if (limit > 0) {
		//	if (list.size() > limit)
		//		list = list.subList(0, limit);
		//}

		return list;
	}


	
	public Matrix<WordStatistics> getCollocationMatrix() {
		
		return matrix;
	}

}
