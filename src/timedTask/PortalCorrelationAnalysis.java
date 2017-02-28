package timedTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dbpool.DBpool;
import fp_growth.FP_growth;
import sparseMatrix.SparseMatrix;
import sparseMatrix.Triad;

public class PortalCorrelationAnalysis implements Runnable {
	private double reliability = 0.4;
	private int portalNum = 100;// 总商铺数量
	private int iterationTimes = 50;

	@Override
	public void run() {
		// generate_mac_portalTable();// 生成每个客户访问过的店铺关联表
		FP_growth fpg = new FP_growth();
		Set<Map<String, Object>> freqSetList = fpg.start();
		List<List<Integer>> regularList = find_correlationRegular(freqSetList);
		SparseMatrix Pmatrix = get_transitionMatrix(regularList);
		SparseMatrix Rmatrix = init_pageRank();
		SparseMatrix newRmatrix;
		SparseMatrix deltaMatrix;
		boolean finish = false;
		int i = 0;
		for (i = 0; i < iterationTimes && !finish; ++i) {
			newRmatrix = Rmatrix.scalarProduct(0.2).matrixAdd(Rmatrix.matrixProduct(Pmatrix).scalarProduct(0.8));
			deltaMatrix = Rmatrix.matrixAdd(newRmatrix.scalarProduct(-1.0));
			double error = 0.0;
			for (Triad triad : deltaMatrix.triadList) {
				error += Math.abs(triad.value);
			}
			if (error < 0.0001) {
				finish = true;
			}
			Rmatrix = newRmatrix;
		}
		System.out.println(i);
		Rmatrix.display();
	}

	private List<List<Integer>> find_correlationRegular(Set<Map<String, Object>> freqSetList) {// 发现关联规则
		Map<Integer, Integer> singleMap = new HashMap<Integer, Integer>();// 存储单个元素的频繁集以及其出现次数
		List<List<Integer>> result = new ArrayList<List<Integer>>();

		for (Map<String, Object> freqData : freqSetList) {
			@SuppressWarnings("unchecked")
			List<Integer> freqSet = (List<Integer>) freqData.get("data");
			Integer counter = (Integer) freqData.get("counter");
			if (freqSet.size() == 1) {
				singleMap.put(freqSet.get(0), counter);
			}
		}

		for (Map<String, Object> freqData : freqSetList) {
			@SuppressWarnings("unchecked")
			List<Integer> freqSet = (List<Integer>) freqData.get("data");
			int counter = (Integer) freqData.get("counter");
			if (freqSet.size() == 2) {
				Integer left = freqSet.get(0);// 关联规则的左部
				Integer right = freqSet.get(1);// 关联规则的右部
				int counterLeft = singleMap.get(left);
				int counterRight = singleMap.get(right);

				// 计算可信度
				if (counter * 1.0 / counterLeft >= reliability) {
					List<Integer> regular = new ArrayList<Integer>();
					regular.add(left);
					regular.add(right);
					result.add(regular);
				}
				if (counter * 1.0 / counterRight >= reliability) {
					List<Integer> regular = new ArrayList<Integer>();
					regular.add(right);
					regular.add(left);
					result.add(regular);
				}
			}
		}

		return result;
	}

	private void generate_mac_portalTable() {
		// 先为表yydc_probe1添加索引
		String addIndex = "alter table yydc_probe1 add index boxmacIdx(box_mac)";
		DBpool.get_instance().executeUpdate(addIndex);

		String drop_macPortal = "drop table mac_portal";
		DBpool.get_instance().executeUpdate(drop_macPortal);

		String create_macPortal = "create table mac_portal(mac varchar(12), portalId int(11))";
		DBpool.get_instance().executeUpdate(create_macPortal);

		String updateQuery = "insert into mac_portal(mac, portalId) select yydc_probe1.mac, portal_box.portalId "
				+ "from yydc_probe1, portal_box where portal_box.boxmac = yydc_probe1.box_mac "
				+ "group by yydc_probe1.mac, portal_box.portalId";
		DBpool.get_instance().executeUpdate(updateQuery);

		String add_idx = "alter table mac_portal add index macIdx(mac)";
		DBpool.get_instance().executeUpdate(add_idx);
	}

	private SparseMatrix get_transitionMatrix(List<List<Integer>> regularList) {
		Map<Integer, List<Integer>> adjTable = new HashMap<Integer, List<Integer>>();// 商铺邻接表
		for (List<Integer> regular : regularList) {
			if (!adjTable.containsKey(regular.get(0))) {
				List<Integer> list = new ArrayList<Integer>();
				adjTable.put(regular.get(0), list);
			}
			adjTable.get(regular.get(0)).add(regular.get(1));
		}
		List<Triad> triadList = new ArrayList<Triad>();
		for (int key : adjTable.keySet()) {
			double adjNum = adjTable.get(key).size() + 1;
			for (int adjNode : adjTable.get(key)) {
				double p = 1.0 / adjNum;
				triadList.add(new Triad(adjNode, key, p));
				// triadList.add(new Triad(key, adjNode, p));
			}
		}
		// 向三元组表中加上稀疏矩阵的对角线元素
		for (int i = 0; i < portalNum; ++i) {
			double adjNum = adjTable.get(i) == null ? 1 : (adjTable.get(i).size() + 1);
			double p = 1.0 / adjNum;
			triadList.add(new Triad(i, i, p));
		}
		SparseMatrix transitionMatrix = new SparseMatrix(triadList, portalNum, portalNum);
		return transitionMatrix;
	}

	private SparseMatrix init_pageRank() {// 初始化pagerank向量
		List<Triad> triadList = new ArrayList<Triad>();
		for (int i = 0; i < portalNum; ++i) {
			double p = 1.0 / portalNum;
			triadList.add(new Triad(0, i, p));
		}
		return new SparseMatrix(triadList, 1, portalNum);
	}

	public static void main(String[] args) {
		long begin = System.currentTimeMillis();
		new PortalCorrelationAnalysis().run();
		long cost = System.currentTimeMillis() - begin;
		System.out.println("cost:" + cost);
	}
}
