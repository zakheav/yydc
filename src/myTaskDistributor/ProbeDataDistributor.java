package myTaskDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import preprocess.Handler;
import preprocess.PipelineTask;
import preprocess.TaskDistributor;
import preprocessHandler.DeleteErrorInfoHandler;
import preprocessHandler.InsertInfoHandler;
import preprocessHandler.ProbeDataPreprocessHandler;
import threadPool.ThreadPool;

public class ProbeDataDistributor extends TaskDistributor {
	private Map<String, List<Handler>> pipelineCache;// 流水线结构缓存
	public ProbeDataDistributor() {
		pipelineCache = new HashMap<String, List<Handler>>();
		// 构造流水线结构缓存
		List<Handler> pl;
		pl = new ArrayList<Handler>();
		pl.add(new DeleteErrorInfoHandler());
		pl.add(new InsertInfoHandler());
		pl.add(new ProbeDataPreprocessHandler());
		pipelineCache.put("probePreprocess", pl);
	}
	
	@Override
	protected void distribute_task(Object o) {
		PipelineTask task = new PipelineTask(pipelineCache.get("probePreprocess"), o);
		ThreadPool.get_instance().add_task(task);// 加入到线程池中
	}
	
}
