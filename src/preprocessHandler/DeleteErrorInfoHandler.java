package preprocessHandler;

import java.util.List;

import preprocess.Handler;
import serverUtil.Sql;

public class DeleteErrorInfoHandler extends Handler {
	private Long time = new Long(1451577600 * 1000);
	@Override
	protected void preprocess(Object params) {
		Sql sql = (Sql)params;
		String sqlStr = sql.get_sqlStr();
		List<List<String>> paramsList = sql.get_paramsList();
		for (int i = 0; i < paramsList.size(); ++i) {
			List<String> p = paramsList.get(i);
			if (sqlStr.equals("insert into yydc_connectionlist(box_mac, upload_time, mac, ip) values(?,?,?,?)")) {
				if (Long.parseLong((p.get(1).split("_"))[1]) < time) {
					paramsList.remove(i);
					--i;
				}
			}

			if (sqlStr.equals(
					"insert into yydc_flowusing(box_mac, upload_time, flow, begintime, endtime) values(?,?,?,?,?)")) {
				if (Long.parseLong((p.get(1).split("_"))[1]) < time) {
					paramsList.remove(i);
					--i;
				}
			}

			if (sqlStr.equals(
					"insert into yydc_probe1(box_mac, upload_time, mac, rssi1, rssi2, rssi3, probe_time) values(?,?,?,?,?,?,?)")) {
				if (Long.parseLong((p.get(1).split("_"))[1]) < time) {
					paramsList.remove(i);
					--i;
				}
			}
		}
	}
}
