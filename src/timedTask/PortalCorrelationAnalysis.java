package timedTask;

import dbpool.DBpool;

public class PortalCorrelationAnalysis implements Runnable{
	@Override
	public void run() {
		generate_mac_portalTable();// 生成每个客户访问过的店铺关联表
	}
	
	private void generate_mac_portalTable() {
		// 先为表yydc_probe1添加索引
		String addIndex = "alter table yydc_probe1 add index boxmacIdx(box_mac)";
		DBpool.get_instance().executeUpdate(addIndex);
		
		String updateQuery = "insert into mac_portal(mac, portalId) "
				+ "select yydc_probe1.mac, portal_box.portalId "
				+ "from yydc_probe1, portal_box "
				+ "where portal_box.boxmac = yydc_probe1.box_mac "
				+ "group by yydc_probe1.mac, portal_box.portalId";
		DBpool.get_instance().executeUpdate(updateQuery);
	}
}
