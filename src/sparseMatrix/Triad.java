package sparseMatrix;

public class Triad {
	public int rowIdx;
	public int colIdx;
	public double value;

	public Triad(int i, int j, double value) {
		this.rowIdx = i;
		this.colIdx = j;
		this.value = value;
	}
}
