package sparseMatrix;

import java.util.List;

public class MatrixProductTask implements Runnable {
	private Syn_triadList result;
	private SparseMatrix sma;
	private SparseMatrix smb;
	private final int i;
	private final int j;
	private final int a_rowBegin;
	private final int b_rowBegin;
	private final int a_rowEnd;
	private final int b_rowEnd;

	public MatrixProductTask(Syn_triadList result, int i, int j, SparseMatrix a, SparseMatrix b, int a_rowBegin,
			int a_rowEnd, int b_rowBegin, int b_rowEnd) {// 矩阵a的i行，转置矩阵b的j行
		this.result = result;
		this.sma = a;
		this.smb = b;
		this.i = i;
		this.j = j;
		this.a_rowBegin = a_rowBegin;
		this.a_rowEnd = a_rowEnd;
		this.b_rowBegin = b_rowBegin;
		this.b_rowEnd = b_rowEnd;
	}

	@Override
	public void run() {
		List<Triad> atl = sma.triadList;
		List<Triad> btl = smb.triadList;
		int ptra = a_rowBegin;
		int ptrb = b_rowBegin;
		double value = 0.0;
		while (ptra <= a_rowEnd && ptrb <= b_rowEnd) {
			if (atl.get(ptra).colIdx > btl.get(ptrb).colIdx) {
				++ptrb;
			} else if (atl.get(ptra).colIdx < btl.get(ptrb).colIdx) {
				++ptra;
			} else {
				value += atl.get(ptra).value * btl.get(ptrb).value;
				++ptra;
				++ptrb;
			}
		}
		if (value != 0.0) {
			result.add(new Triad(i, j, value));
		}
	}
}
