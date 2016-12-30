package serverUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import dbpool.Query;

public class MemCache {
	private static MemCache instance = new MemCache();

	private final ConcurrentMap<String, Integer> box_probeNum;
	private final ConcurrentMap<String, Long> box_timer;
	public Map<String, Set<String>> box_customer;// 缓存这一小时每个盒子的访客
	public Long thisHour;

	private MemCache() {
		box_probeNum = new ConcurrentHashMap<String, Integer>();
		box_timer = new ConcurrentHashMap<String, Long>();
		box_customer = new HashMap<String, Set<String>>();
		thisHour = (System.currentTimeMillis() / (1000 * 3600)) * 3600;
	}

	public static MemCache getInstance() {
		return instance;
	}

	public synchronized void storeToDB(Long now) {
		if (now > thisHour) {
			for (String boxMac : box_customer.keySet()) {
				Integer userCount = box_customer.get(boxMac).size();
				ArrayList<Object> params = new ArrayList<Object>();
				params.add(MemCache.getInstance().thisHour);
				params.add(boxMac);
				params.add(userCount);
				new Query().insertInto("yydc_probecache(time, box_mac, count)").values(params).executeUpdate();
			}
			box_customer = new ConcurrentHashMap<String, Set<String>>();
			thisHour = now;
		}
	}

	private Long get_boxTimeStamp(String mac) {
		Long timestamp = box_timer.get(mac);
		if (timestamp == null)
			timestamp = new Long(0);
		return timestamp;
	}

	public Integer get_boxProbenumberNow(String mac) {
		Long timestamp = get_boxTimeStamp(mac);
		long nowTime = System.currentTimeMillis();
		if (nowTime - timestamp < 600 * 1000) {
			return box_probeNum.get(mac);
		} else {
			return 0;
		}
	}

	public void set_boxProbenumberNow(String mac, Long probeTimeStamp, Integer probeNum) {
		box_probeNum.put(mac, probeNum);
		box_timer.put(mac, probeTimeStamp);
	}

}
