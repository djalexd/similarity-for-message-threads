package project.client.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SparseMatrix<E> {

	private boolean isDirty;
	private int width, height;
	
	private Map<Point, E> data;
	
	public SparseMatrix () {
		data = new HashMap<Point, E> ();
		isDirty = false;
	}
	
	public E getElement (int x, int y) {
		
		Point pt = new Point (x,y);
		if (data.containsKey(pt))
			return data.get (pt);
		else
			return null;
	}
	
	public void setElement (int x, int y, E v) {
		data.put(new Point (x,y), v);
		isDirty = true;
	}
	
	public void clear () {
		data.clear();
		isDirty = true;
	}
	
	public int size () {
		return data.size();
	}
	
	public Set<Point> keySet () {
		return data.keySet();
	}
	
	public Collection<E> values () {
		return data.values();
	}
	
	public int getWidth () {
		
		this.recalculateWidthAndHeight();
		
		return width;
	}
	
	public int getHeight () {
		
		this.recalculateWidthAndHeight();
		
		return height;
	}

	
	private void recalculateWidthAndHeight () {
		
		if (isDirty) {
			width = height = 0;
			Iterator<Point> i = data.keySet().iterator();
			while (i.hasNext()) {

				Point pt = i.next();
				if (pt.getX() > width)
					width = pt.getX();
				
				if (pt.getY() > height)
					height = pt.getY();
				
			}
			
			isDirty = false;
		}
	}

	@Override
	public String toString() {
		
		return data.toString();
	}
	
}
