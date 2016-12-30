package preprocessHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import preprocess.Handler;
import serverUtil.MemCache;
import serverUtil.Sql;

public class ProbeDataPreprocessHandler implements Handler {
	private Long duration = new Long(3600);

	private long get_now() {
		Long now = System.currentTimeMillis() / (1000 * duration);
		return now * duration;
	}

	@Override
	public void preprocess(Object params) {
		Sql sql = (Sql) params;

		Long now = get_now();// 当前时间段的起始

		if (now > MemCache.getInstance().thisHour) {// 到了下一个小时，把box_customer的数据写到硬盘上
			MemCache.getInstance().storeToDB(now);
		}

		if (!sql.get_paramsList().isEmpty()) {
			long createTime = System.currentTimeMillis() / 1000;
			Set<String> set = new HashSet<String>();// 用于去重
			List<List<String>> paramsList = sql.get_paramsList();

			String boxMac = (sql.get_paramsList().get(0).get(0).split("_"))[1];// 上传信息的盒子
			if (!MemCache.getInstance().box_customer.containsKey(boxMac)) {
				MemCache.getInstance().box_customer.put(boxMac, new HashSet<String>());
			}
			for (List<String> param : paramsList) {
				String mac = (param.get(2).split("_"))[1];
				Integer rssi = Integer.parseInt((param.get(3).split("_"))[1]);
				if (rssi > -70) {
					MemCache.getInstance().box_customer.get(boxMac).add(mac);
					set.add(mac);
				}
			}
			int probeNum = set.size();
			
			MemCache.getInstance().set_boxProbenumberNow(boxMac, createTime, probeNum);// 记录当前盒子的在线数目
		}
	}
}
