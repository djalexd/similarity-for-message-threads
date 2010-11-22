package project.client.utils;

public class FloatSparseMatrix extends SparseMatrix<Float> {

	public void updateElement (int x, int y, float delta) {
		
		if (this.getElement(x, y) == null)
			this.setElement(x, y, delta);
		else
			this.setElement(x, y, this.getElement(x, y) + delta);
		
	}
}
