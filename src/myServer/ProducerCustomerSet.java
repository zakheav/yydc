package myServer;

import java.util.HashMap;
import java.util.Map;

import IOC.IOC;
import myTaskDistributor.ProbeDataDistributor;
import preprocess.TaskDistributor;
import producer.Producer;

public class ProducerCustomerSet {
	public Map<String, Producer> producerMap;
	public Map<String, TaskDistributor> taskDistributorMap;

	private static ProducerCustomerSet instance = new ProducerCustomerSet();

	public static ProducerCustomerSet get_instance() {
		return instance;
	}

	private ProducerCustomerSet() {// 这部分使用依赖注入的方式实现
		this.producerMap = new HashMap<String, Producer>();
		this.taskDistributorMap = new HashMap<String, TaskDistributor>();
		IOC ioc = IOC.get_instance();
		Producer producer1 = (Producer) ioc.get_object("producer1");
		ProbeDataDistributor probeDataDistributor = (ProbeDataDistributor) ioc.get_object("probeDataDistributor");
		producerMap.put("probe", producer1);
		taskDistributorMap.put("probe", probeDataDistributor);
		taskDistributorMap.get("probe").start();// 启动分发者对象
	}
	
	public static void main(String[] args) {
		ProducerCustomerSet.get_instance();
	}
}
