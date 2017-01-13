package timedTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dbpool.DBpool;
import fp_growth.FP_growth;

public class PortalCorrelationAnalysis implements Runnable {
	private double reliability = 0.4;

	@Override
	public void run() {
		// generate_mac_portalTable();// 生成每个客户访问过的店铺关联表
		FP_growth fpg = new FP_growth();
		Set<Map<String, Object>> freqSetList = fpg.start();
		List<List<Integer>> regularList = find_correlationRegular(freqSetList);
		for(List<Integer> regular : regularList) {
			System.out.println(regular.get(0)+" -> "+regular.get(1));
		}
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

	public static void main(String[] args) {
		long begin = System.currentTimeMillis();
		new PortalCorrelationAnalysis().run();
		long cost = System.currentTimeMillis() - begin;
		System.out.println("cost:" + cost);
	}
}
