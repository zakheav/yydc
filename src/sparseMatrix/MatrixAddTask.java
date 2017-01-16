package sparseMatrix;

import java.util.List;

public class MatrixAddTask implements Runnable {
	private Syn_triadList result;
	private SparseMatrix sma;
	private SparseMatrix smb;
	private final int i;
	private final int a_rowBegin;
	private final int b_rowBegin;
	private final int a_rowEnd;
	private final int b_rowEnd;

	public MatrixAddTask(Syn_triadList result, int i, SparseMatrix a, SparseMatrix b, int a_rowBegin, int a_rowEnd,
			int b_rowBegin, int b_rowEnd) {// 矩阵a的i行，矩阵b的i行
		this.result = result;
		this.sma = a;
		this.smb = b;
		this.i = i;
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
		if (ptra == -1) {// a矩阵在i行是全0
			if (ptrb != -1) {// b矩阵在i行有非0值
				while (ptrb <= b_rowEnd) {
					result.add(new Triad(i, btl.get(ptrb).colIdx, btl.get(ptrb).value));
					++ptrb;
				}
			}
		} else if (ptrb == -1) {// b矩阵在i行是全0
			if (ptra != -1) {// a矩阵在i行有非0值
				while (ptra <= a_rowEnd) {
					result.add(new Triad(i, atl.get(ptra).colIdx, atl.get(ptra).value));
					++ptra;
				}
			}
		} else {
			while (ptra <= a_rowEnd && ptrb <= b_rowEnd) {
				if (atl.get(ptra).colIdx > btl.get(ptrb).colIdx) {
					result.add(new Triad(i, btl.get(ptrb).colIdx, btl.get(ptrb).value));
					++ptrb;
				} else if (atl.get(ptra).colIdx < btl.get(ptrb).colIdx) {
					result.add(new Triad(i, atl.get(ptra).colIdx, atl.get(ptra).value));
					++ptra;
				} else {
					result.add(new Triad(i, atl.get(ptra).colIdx, atl.get(ptra).value + btl.get(ptrb).value));
					++ptra;
					++ptrb;
				}
			}
			while (ptra <= a_rowEnd) {
				result.add(new Triad(i, atl.get(ptra).colIdx, atl.get(ptra).value));
				++ptra;
			}
			while (ptrb <= b_rowEnd) {
				result.add(new Triad(i, btl.get(ptrb).colIdx, btl.get(ptrb).value));
				++ptrb;
			}
		}
	}

}
