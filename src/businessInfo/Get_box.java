package businessInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import dbpool.Query;

public class Get_box {// 获取盒子
	public List<String> get_boxByUserID(String UID) {// 根据UID获取盒子
		List<Map<String, Object>> r;
		Long userID = Long.parseLong(UID);
		List<String> macList = new ArrayList<String>();// 用于存储所有附庸于这个用户，商铺的盒子mac
		r = new Query().select("*").table("mm_user").where("user_id").equal(userID).fetchAll();
		int rank = 0;// 记录这个用户的级别
		if (!r.isEmpty()) {
			if (r.get(0).get("user_merchant").toString().equals("FFFF")) {
				rank = 0;
			} else if (r.get(0).get("user_1agent").toString().equals("FFFF")) {
				rank = 1;
			} else if (r.get(0).get("user_2agent").toString().equals("FFFF")) {
				rank = 2;
			} else if (r.get(0).get("user_3agent").toString().equals("FFFF")) {
				rank = 3;
			} else if (r.get(0).get("user_4agent").toString().equals("FFFF")) {
				rank = 4;
			}
		}

		if (rank > 0) {
			r = new Query().select("box_uid").table("mm_box").where("box_" + rank + "agent").equal(UID).fetchAll();
			for (int i = 0; i < r.size(); ++i) {
				macList.add((String) r.get(i).get("box_uid"));
			}
		}

		return macList;
	}

	public List<String> get_boxByTerminal(String terminalMac) {
		List<Map<String, Object>> r;
		List<String> macList = new ArrayList<String>();
		r = new Query().select("*").table("terminal_box").where("terminalmac").equal(terminalMac).fetchAll();
		for (Map<String, Object> e : r) {
			macList.add((String) e.get("boxmac"));
		}
		return macList;
	}
}
