package threadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import lockFreeParallelFrameWorkUtil.RingBuffer;

public class ThreadPool {
	private List<Worker> workerList;
	private ConcurrentLinkedQueue<Runnable> overFlowTasks;// 任务溢出区
	private final int WORK_NUM;
	private static ThreadPool instance = new ThreadPool();

	private ThreadPool() {
		this.WORK_NUM = 4;
		this.workerList = new ArrayList<Worker>();
		this.overFlowTasks = new ConcurrentLinkedQueue<Runnable>();
		for (int i = 0; i < WORK_NUM; ++i) {
			add_worker();
		}
	}

	public static ThreadPool get_instance() {
		return instance;
	}

	private void add_worker() {
		RingBuffer taskBuffer = new RingBuffer(65536);
		Worker worker = new Worker(taskBuffer);
		worker.start();
		workerList.add(worker);
	}

	class Worker extends Thread {
		private volatile boolean block;// 用于判断这个worker是否已经阻塞等待新的任务
		public RingBuffer taskBuffer;

		public Worker(RingBuffer taskBuffer) {
			this.taskBuffer = taskBuffer;
			this.block = false;
		}

		public void run() {
			int noBlockTimer = 10000;// 用于减少不必要的线程阻塞,尤其在大量简单的小任务加入线程池的时候
			while (true) {
				Object task = null;
				do {
					task = taskBuffer.get_element();
					if (task != null) {
						((Runnable) task).run();
					}
				} while (task != null);

				do {// 检查溢出区是否存在任务
					task = overFlowTasks.poll();
					if (task != null) {
						((Runnable) task).run();
					}
				} while (task != null);

				if (noBlockTimer > 0) {
					--noBlockTimer;
				} else {
					noBlockTimer = 10000;
					this.block = true;

					synchronized (taskBuffer) {
						while (taskBuffer.isEmpty()) {
							try {
								taskBuffer.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						this.block = false;
					}
				}
			}
		}
	}

	public void add_task(Runnable task) {
		int idx = (int) (Math.random() * WORK_NUM);
		if (idx == WORK_NUM)
			--idx;
		Worker worker = workerList.get(idx);

		if (!worker.taskBuffer.add_element(task)) {// 无法向buffer中添加任务（buffer满）
			overFlowTasks.offer(task);
		}
		
		if (worker.block) {// 这个worker在阻塞等待新的任务
			synchronized (worker.taskBuffer) {
				worker.taskBuffer.notify();
			}
		} 
	}
}
