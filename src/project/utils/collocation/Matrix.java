package project.utils.collocation;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class Matrix<T> {

	private Map<Point, T> values = null;
	
	public Matrix () {
		values = new HashMap<Point, T> ();
	}
	
	
	public Map<Point, T> getValues () {
		return values;
	}
	
	public int getSize () {
		return values.size();
	}
	
	public T getValue (int x, int y) {
		Point p = new Point(x, y);
		return values.get(p);
	}
	
	public void setValue (int x, int y, T v) {
		Point p = new Point (x, y);
		values.put(p, v);
	}
}
