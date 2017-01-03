package fp_growth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dbpool.Query;

public class DBDataSource implements DataSource {
	private List<String> macList = new ArrayList<String>();// 存储所有的客户
	private int macList_idx = 0;

	public DBDataSource() {
		// 搜索出所有的mac
		List<Map<String, Object>> results = new Query().select("distinct mac").table("mac_portal").where("true")
				.fetchAll();
		for (Map<String, Object> row : results) {
			macList.add((String) row.get("mac"));
		}
	}

	public Integer[] get_nextData() {
		if (macList_idx == macList.size()) {
			return null;
		} else {
			List<Map<String, Object>> results = new Query().select("*").table("mac_portal").where("mac")
					.equal(macList.get(macList_idx)).fetchAll();
			++macList_idx;
			Integer[] portals = new Integer[results.size()];
			for (int i = 0; i < results.size(); ++i) {
				portals[i] = (Integer) results.get(i).get("portalId");
			}
			return portals;
		}
	}

	public void refresh_dataSource() {
		macList_idx = 0;
	}

	public void clear_cache() {
	}
}
