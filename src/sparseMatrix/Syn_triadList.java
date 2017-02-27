package sparseMatrix;

import java.util.ArrayList;
import java.util.List;
import lockFreeParallelFrameWorkUtil.SequenceNum;

public class Syn_triadList {
	public List<Triad> result = new ArrayList<Triad>();
	private SequenceNum getLock = new SequenceNum();
	public void add(Triad t) {
		while (!getLock.compareAndSet(0, 1));// 加锁
		result.add(t);
		getLock.set(0);// 释放锁
	}
}
