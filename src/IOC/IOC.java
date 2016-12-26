package IOC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import serverUtil.XML;

public class IOC {
	private Map<String, ObjectGraphNode> objectGraph = null;// key是每个对象在xml文件中的id属性

	private static IOC instance = new IOC();

	private boolean singletonObjectReferenceCheck(Map<String, ObjectGraphNode> objectGraph) {// 保证单例对象引用的对象一定要是单例
		Set<String> visited = new HashSet<String>();
		for(String objId : objectGraph.keySet()) {
			if(!visited.contains(objId)) {
				visited.add(objId);
				ObjectGraphNode node = objectGraph.get(objId);
				if(node.singleton) {// 当前节点是单例的
					for(String propertyName : node.ref.keySet()) {
						String refObjId = node.ref.get(propertyName);
						if(objectGraph.get(refObjId) == null) {
							return false;
						} else if(objectGraph.get(refObjId).singleton == false){
							System.out.println("单例对象引用的对象应该是单例的！请重新检查对象配置文件"+objId);
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private IOC() {
		XML xml = new XML();
		objectGraph = xml.get_objectGraph();
		// 需要检查，单例对象引用的对象一定要是单例
		singletonObjectReferenceCheck(objectGraph);
	}
	
	public static IOC get_instance() {
		return instance;
	}

	public Map<String, ObjectGraphNode> bfs(String objectId) {// 广度优先搜索，获取到和指定ObjectId对象相关联的所有对象（包括自己），以hash表方式存储
		Map<String, ObjectGraphNode> list = new HashMap<String, ObjectGraphNode>();
		Queue<ObjectGraphNode> queue = new LinkedList<ObjectGraphNode>();
		ObjectGraphNode nowNode = objectGraph.get(objectId);
		if (nowNode != null) {
			queue.offer(nowNode);
			list.put(objectId, nowNode);
		} else {
			System.out.println("there is a wrong object id in xml file.");
		}
		while (!queue.isEmpty()) {
			nowNode = queue.poll();
			for (String propertyName : nowNode.ref.keySet()) {
				String id = nowNode.ref.get(propertyName);// 引用对象的id
				ObjectGraphNode child = objectGraph.get(id);
				if (child != null) {
					if (!list.containsKey(id)) {
						queue.offer(child);
						list.put(id, child);
					}
				} else {
					System.out.println("there is a wrong object id in xml file.");
				}
			}
		}
		return list;
	}

	public synchronized Object get_object(String objectId) {// 输入objectid，获取object
		// 搜索出和objectId相关联的对象图节点对象（包括objectId本身对应的节点对象）
		Map<String, ObjectGraphNode> subGraph = bfs(objectId);// 这相当于一个子图
		Map<String, Object> objectMap = new HashMap<String, Object>();
		Map<String, Boolean> singletonAlreadyExist = new HashMap<String, Boolean>();// 表示这个objId对应的单例对象已经构建完毕
		Reflection reflection = new Reflection();
		for (String objId : subGraph.keySet()) {// 把子图中的对象生成后存储在objectMap中（不进行对象引用的组装）
			ObjectGraphNode graphNode = subGraph.get(objId);
			if (graphNode.singleton) {// 这个对象是一个单例
				if (graphNode.instance != null) {// 之前已经生成了这个对象的单例
					singletonAlreadyExist.put(objId, true);
					objectMap.put(objId, graphNode.instance);
				} else {
					String className = graphNode.className;
					Object obj = reflection.get_object(className);
					// 对于非引用属性进行赋值
					for (String propertyName : graphNode.propertyValue.keySet()) {
						Object propertyValue = graphNode.propertyValue.get(propertyName);
						String propertyType = graphNode.propertyType.get(propertyName);
						reflection.set(obj, propertyName, propertyValue, propertyType);
					}
					objectMap.put(objId, obj);
					graphNode.instance = obj;
				}
			} else {// 这个对象不是单例
				String className = graphNode.className;
				Object obj = reflection.get_object(className);
				// 对于非引用属性进行赋值
				for (String propertyName : graphNode.propertyValue.keySet()) {
					Object propertyValue = graphNode.propertyValue.get(propertyName);
					String propertyType = graphNode.propertyType.get(propertyName);
					reflection.set(obj, propertyName, propertyValue, propertyType);
				}
				objectMap.put(objId, obj);
			}
		}
		// 把objectMap中的对象按照subGraph中的关系进行组装
		for(String objId : subGraph.keySet()) {
			if(singletonAlreadyExist.get(objId) == null) {// 这个对象是一个没有组装过的对象
				ObjectGraphNode graphNode = subGraph.get(objId);
				Object obj = objectMap.get(objId);
				for(String propertyName : graphNode.ref.keySet()) {
					String refObjId = graphNode.ref.get(propertyName);
					Object propertyValue = objectMap.get(refObjId);
					String propertyType = subGraph.get(refObjId).className; 
					reflection.set(obj, propertyName, propertyValue, propertyType);
				}
			}
		}
		return objectMap.get(objectId);
	}
}
