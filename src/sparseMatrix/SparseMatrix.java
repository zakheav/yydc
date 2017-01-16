package sparseMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import iterationThreadPool.IterationThreadPool;

class CompareByRow_col implements Comparator<Triad> {
	@Override
	public int compare(Triad t0, Triad t1) {
		if (t0.rowIdx == t1.rowIdx) {
			return t0.colIdx - t1.colIdx;
		} else {
			return t0.rowIdx - t1.rowIdx;
		}
	}

}

public class SparseMatrix {
	public final int rowNum;
	public final int colNum;
	public List<Triad> triadList;
	public int[] rowBeginIdxList;// 每一行起始于matrix的第几个元素
	public int[] rowEndIdxList;// 每一行结束于matrix的

	public SparseMatrix(List<Triad> list, int rowNum, int colNum) {
		this.rowNum = rowNum;
		this.colNum = colNum;
		this.triadList = list;
		this.rowBeginIdxList = new int[rowNum];
		this.rowEndIdxList = new int[rowNum];
		for (int i = 0; i < rowNum; ++i) {
			rowBeginIdxList[i] = -1;
			rowEndIdxList[i] = -1;
		}
		Collections.sort(triadList, new CompareByRow_col());// 行优先排序
		int nowRow = -1;
		int i = 0;
		for (Triad triad : triadList) {
			if (triad.rowIdx > nowRow) {
				if (nowRow != -1) {
					rowEndIdxList[nowRow] = i - 1;
				}
				nowRow = triad.rowIdx;
				rowBeginIdxList[nowRow] = i;
			}
			++i;
		}
		rowEndIdxList[nowRow] = i - 1;
	}

	public SparseMatrix matrixProduct(SparseMatrix transMatrix) {// 传入转置后的稀疏矩阵
		Syn_triadList result = new Syn_triadList();
		List<Runnable> taskList = new ArrayList<Runnable>();
		// 假设本矩阵是a，传入的转置矩阵是b
		int r = transMatrix.rowNum;
		int c = transMatrix.colNum;
		if (this.colNum == c) {
			for (int i = 0; i < rowNum; ++i) {
				for (int j = 0; j < r; ++j) {
					if (this.rowBeginIdxList[i] != -1 && transMatrix.rowBeginIdxList[i] != -1) {
						// 把本矩阵的第i行和转置矩阵的第j行打包成一个任务，加入到迭代线程池
						int a_rowBegin = this.rowBeginIdxList[i];
						int a_rowEnd = this.rowEndIdxList[i];
						int b_rowBegin = transMatrix.rowBeginIdxList[j];
						int b_rowEnd = transMatrix.rowEndIdxList[j];
						taskList.add(new MatrixProductTask(result, i, j, this, transMatrix, a_rowBegin, a_rowEnd,
								b_rowBegin, b_rowEnd));
					}
				}
			}
			IterationThreadPool.get_instance().add_taskList(taskList);
			return new SparseMatrix(result.result, rowNum, r);
		} else {
			return null;
		}
	}

	public SparseMatrix matrixAdd(SparseMatrix matrix) {
		Syn_triadList result = new Syn_triadList();
		List<Runnable> taskList = new ArrayList<Runnable>();
		// 假设本矩阵是a，传入的矩阵是b
		int r = matrix.rowNum;
		int c = matrix.colNum;
		if (r == this.rowNum && c == this.colNum) {
			for (int i = 0; i < rowNum; ++i) {
				int a_rowBegin = this.rowBeginIdxList[i];
				int a_rowEnd = this.rowEndIdxList[i];
				int b_rowBegin = matrix.rowBeginIdxList[i];
				int b_rowEnd = matrix.rowEndIdxList[i];
				taskList.add(new MatrixAddTask(result, i, this, matrix, a_rowBegin, a_rowEnd, b_rowBegin, b_rowEnd));
			}
			IterationThreadPool.get_instance().add_taskList(taskList);
			return new SparseMatrix(result.result, rowNum, colNum);
		} else {
			return null;
		}
	}

	public void display() {
		for (Triad triad : triadList) {
			System.out.println(triad.rowIdx + " " + triad.colIdx + " " + triad.value);
		}
		System.out.println("----------------");
		for (int i : rowBeginIdxList) {
			System.out.print(i + " ");
		}
		System.out.println();
		for (int i : rowEndIdxList) {
			System.out.print(i + " ");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		List<Triad> list = new ArrayList<Triad>();
		list.add(new Triad(0, 0, 1.0));
		list.add(new Triad(0, 1, 2.0));
		list.add(new Triad(3, 2, 2.0));
		list.add(new Triad(0, 2, 3.0));
		list.add(new Triad(0, 3, 4.0));
		list.add(new Triad(1, 2, 2.0));

		SparseMatrix a = new SparseMatrix(list, 4, 4);

		list = new ArrayList<Triad>();
		list.add(new Triad(0, 0, 1.0));
		list.add(new Triad(1, 0, 2.0));
		list.add(new Triad(2, 0, 3.0));
		list.add(new Triad(3, 0, 4.0));
		list.add(new Triad(2, 1, 2.0));
		list.add(new Triad(2, 3, 2.0));

		SparseMatrix b = new SparseMatrix(list, 4, 4);

		a.matrixAdd(b).display();

	}
}
