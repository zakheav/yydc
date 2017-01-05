package fp_growth;

import java.util.HashMap;
import java.util.Map;

public class FP_tree {
	public Map<Integer, TreeNode> headerList = new HashMap<Integer, TreeNode>();// nodeList的头节点表
	public Integer[] rank;// 每个元素出现次数从大到小的排序（原因是上面的headerList是map结构的，没办法保证次序）
	public TreeNode root;

	public FP_tree(DataSource dataSource) {
		OfferData od = new OfferData(dataSource);
		rank = od.get_dataFreqRank();

		root = new TreeNode(true, -1, 0, null);
		Map<String, Object> map = od.get_nextData();

		while (map != null) {
			Integer[] path = (Integer[]) map.get("data");
			Integer counter = (Integer) map.get("counter");

			add_pathToTree(path, counter);

			map = od.get_nextData();
		}

	}

	private void add_pathToTree(Integer[] path, int counter) {
		TreeNode now = root;
		for (Integer e : path) {
			// 看是否需要新建一个节点
			if (!now.children.containsKey(e)) {// 需要新建节点
				TreeNode newNode = new TreeNode(false, e, counter, now);
				now.children.put(e, newNode);
				if (headerList.containsKey(e)) {// 构造nodeList
					newNode.nodeLink = headerList.get(e);
				}
				headerList.put(e, newNode);
				now = newNode;
			} else {// 不需要新建节点
				now = now.children.get(e);
				now.count += counter;
			}
		}
	}

}
