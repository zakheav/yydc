package timedTask;

import java.util.List;
import java.util.Set;

import dbpool.DBpool;
import fp_growth.FP_growth;

public class PortalCorrelationAnalysis implements Runnable {
	@Override
	public void run() {
		// generate_mac_portalTable();// 生成每个客户访问过的店铺关联表
		FP_growth fpg = new FP_growth();
		Set<List<Integer>> result = fpg.start();
		for(List<Integer> freqSet : result) {
			System.out.println();
			for(Integer e : freqSet) {
				System.out.print(e+", ");
			}
		}
	}
	
	private void generate_mac_portalTable() {
		// 先为表yydc_probe1添加索引
		String addIndex = "alter table yydc_probe1 add index boxmacIdx(box_mac)";
		DBpool.get_instance().executeUpdate(addIndex);
		
		String drop_macPortal = "drop table mac_portal";
		DBpool.get_instance().executeUpdate(drop_macPortal);
		
		String create_macPortal = "create table mac_portal(mac varchar(12), portalId int(11))";
		DBpool.get_instance().executeUpdate(create_macPortal);
		
		String updateQuery = "insert into mac_portal(mac, portalId) "
				+ "select yydc_probe1.mac, portal_box.portalId "
				+ "from yydc_probe1, portal_box "
				+ "where portal_box.boxmac = yydc_probe1.box_mac "
				+ "group by yydc_probe1.mac, portal_box.portalId";
		DBpool.get_instance().executeUpdate(updateQuery);
		
		String add_idx = "alter table mac_portal add index macIdx(mac)";
		DBpool.get_instance().executeUpdate(add_idx);
	}

	public static void main(String[] args) {
		new PortalCorrelationAnalysis().run();
	}
}
