package preprocessHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dbpool.Query;
import preprocess.Handler;
import serverUtil.CacheViaRedis;
import serverUtil.Sql;

public class ProbeDataPreprocessHandler extends Handler {
	private Long duration = new Long(3600);

	private long get_now() {
		Long now = System.currentTimeMillis() / (1000 * duration);
		return now * duration;
	}

	@Override
	protected void preprocess(Object params) {
		Sql sql = (Sql) params;
		String boxMac = (sql.get_paramsList().get(0).get(0).split("_"))[1];// 上传信息的盒子
		Long now = get_now();// 当前时间段的起始

		// 搜索当前上传信息的盒子在这一小时的周围人数
		List<Map<String, Object>> results = new Query().select("*").table("yydc_probe1").where("probe_time").bigger(now)
				.andWhere("box_mac").equal(boxMac).fetchAll();
		Integer userCount = 0;
		Map<String, Boolean> hash = new HashMap<String, Boolean>();// 用于去重
		for (int i = 0; i < results.size(); ++i) {
			String mac = (String) results.get(i).get("mac");
			if (hash.get(mac) == null) {
				hash.put(mac, true);
				++userCount;
			}
		}
		results = new Query().select("*").table("yydc_probecache").where("box_mac").equal(boxMac).andWhere("time")
				.equal(now).fetchAll();
		if (results.isEmpty()) {
			List<Object> p = new ArrayList<Object>();
			p.add(now);
			p.add(boxMac);
			p.add(userCount);
			new Query().insertInto("yydc_probecache(time, box_mac, count)").values(p).executeUpdate();
		} else {
			HashMap<String, Object> attr_value = new HashMap<String, Object>();
			attr_value.put("count", userCount);
			new Query().update("yydc_probecache").set(attr_value).where("box_mac").equal(boxMac).andWhere("time")
					.equal(now).executeUpdate();
		}

		// 在redis数据库中缓存盒子当前周围人数
		int probeNum = 0;
		long createTime = System.currentTimeMillis() / 1000;
		hash = new HashMap<String, Boolean>();// 用于去重
		List<List<String>> paramsList = sql.get_paramsList();
		for (int i = 0; i < paramsList.size(); ++i) {
			List<String> p = paramsList.get(i);
			String mac = p.get(2).split("_")[1];
			if (hash.get(mac) == null) {
				++probeNum;
				hash.put(mac, true);
			}
		}
		CacheViaRedis.getInstance().set_boxProbenumberNow(boxMac, createTime, probeNum);//记录当前盒子的在线数目
	}
}
