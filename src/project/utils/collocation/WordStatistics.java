package project.utils.collocation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WordStatistics {

	private static long longestWordLength = -1;
	
	private float offset = 0.0f;
	private float mean = 0.0f;
	
	private int numOccurences = 0;
	private List<Float> offsetHistory = new LinkedList<Float> ();
	
	private String w1, w2;

	public String getW1() {
		return w1;
	}

	public void setW1(String w1) {
		if (w1.length() > longestWordLength)
			longestWordLength = w1.length();
		this.w1 = w1;
	}

	public String getW2() {
		return w2;
	}

	public void setW2(String w2) {
		if (w2.length() > longestWordLength)
			longestWordLength = w2.length();		
		this.w2 = w2;
	}

	public float getOffset() {
		return offset;
	}
	
	public void addOffset (float deltaOffset) {
		offsetHistory.add(new Float(deltaOffset));
		offset += deltaOffset;
		numOccurences ++;
	}
	
	/**
	 * Call this when counts have finished
	 */
	public void updateStats () throws NumberFormatException {
		
		if (numOccurences == 0)
			throw new NumberFormatException ("illegal numOccurences: 0");
		
		offset /= numOccurences;
		Iterator<Float> i = offsetHistory.iterator();
		mean = 0.0f;
		while (i.hasNext()) {
			float di = i.next().floatValue();
			mean += (di - offset) * (di - offset);
		}
		
		//if (numOccurences == 1)
		//	throw new NumberFormatException ("illegal numOccurences: 1");
		
		if (numOccurences > 1) {
			mean /= (numOccurences - 1);
			mean = (float) Math.sqrt(mean);
		} else {
			//System.out.println ("Num occurences is 1");
		}
	}

	public void setOffset(float offset) {
		this.offset = offset;
	}

	public float getMean() {
		return mean;
	}

	public void setMean(float mean) {
		this.mean = mean;
	}

	public int getNumOccurences() {
		return numOccurences;
	}

	public void setNumOccurences(int numOccurences) {
		this.numOccurences = numOccurences;
	}
	
	public String toString() {
		
		String s1="w1=" + String.format("%12s", w1);
		String s2="w2=" + String.format("%12s", w2);
		
		String s3="offset=" + String.format("%.2f", getOffset());
		String s4="mean=" + String.format("%.3f", getMean());
		
		String s5="#counts=" + String.format("%3d", getNumOccurences());
		
		return "stats[" + s1 + "," + s2 + "," + s3 + "," + s4 + "," + s5 + "]";
	}

	
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof WordStatistics))
			return false;
		
		WordStatistics other = (WordStatistics) obj;
		
		return (this.getW1().equals(other.getW1())) &&
		       (this.getW2().equals(other.getW2()));
	}
	
}
