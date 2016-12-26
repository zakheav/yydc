package IOC;

import java.util.HashMap;
import java.util.Map;

public class ObjectGraphNode {// 对象依赖关系图节点
	public String className = "";// 类名，比如java.lang.Integer
	public Map<String, Object> propertyValue = new HashMap<String, Object>();// 非引用属性名称-值
	public Map<String, String> propertyType = new HashMap<String, String>();// 非引用属性名称-类型
	public Map<String, String> ref = new HashMap<String, String>();// key是对象中引用对象属性的name，value是引用的对象的id
	public Boolean singleton = true;
	public Object instance = null;// 单例对象
}
