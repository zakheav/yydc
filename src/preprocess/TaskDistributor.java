package preprocess;

import producer.Producer;
import producer.SpillTaskQueue;

public abstract class TaskDistributor extends Thread {
	protected Producer producer;
	public volatile boolean block;// 用于判断是否已经阻塞等待新的任务
	
	private volatile boolean memoryBarrier = true;// 提供内存屏障支持
	@SuppressWarnings("unused")
	private volatile boolean mb = true;// 提供内存屏障支持
	
	public TaskDistributor() {
		this.block = false;
	}

	public void set_producer(Producer producer) {
		this.producer = producer;
	}
	
	public void run() {
		
		while(true) {
			while(!producer.taskBuffer.isEmpty()) {
				Object o = producer.taskBuffer.get_element();
				distribute_task(o);
			}
			while(!SpillTaskQueue.overFlowTasks.isEmpty()) {// 检查溢出区是否存在任务
				Object o = SpillTaskQueue.overFlowTasks.poll();
				if(o != null) {
					distribute_task(o);
				}
			}
			this.block = true;
			mb = memoryBarrier;// 在block变量之后添加内存屏障，该指令后面的指令不会被重排序到前面
			
			synchronized (producer.taskBuffer) {
				while(producer.taskBuffer.isEmpty()) {
					try {
						producer.taskBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				this.block = false;
				mb = memoryBarrier;// 在block变量之后添加内存屏障，该指令后面的指令不会被重排序到前面
			}
		}
	}// 获取任务

	protected abstract void distribute_task(Object o);// 分发任务，相当于一个工厂，生成任务PipelineTask放入线程池
}
