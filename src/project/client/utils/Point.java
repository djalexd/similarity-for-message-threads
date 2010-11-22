package project.client.utils;

public class Point {

	private int x, y;
	
	public Point () {
		x = y = 0;
	}
	
	public Point(int _x, int _y) {
		this.x = _x;
		this.y = _y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public boolean equals(Object obj) {
		/*
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		*/
		
		if (obj == null)
			return false;
		
		try {			
			Point other = (Point) obj;
			//System.out.println ("equals " + this.toString() + " ? " + other.toString ());			
			return (other.getX() == this.getX() && 
			        other.getY() == this.getY());
			
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}		
	}

	@Override
	public String toString() {
		
		return "(" + this.getX() + "," + this.getY() + ")";
	}

	
	@Override
	public int hashCode() {
		

		return 7919 * this.getX() + 6203 * this.getY();
	}
	
}
