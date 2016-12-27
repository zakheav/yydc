package serverUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MemCache {
	private static MemCache instance = new MemCache();

	private final ConcurrentMap<String, Integer> box_probeNum;
	private final ConcurrentMap<String, Long> box_timer;

	private MemCache() {
		box_probeNum = new ConcurrentHashMap<String, Integer>();
		box_timer = new ConcurrentHashMap<String, Long>();
	}

	public static MemCache getInstance() {
		return instance;
	}

	public Long get_boxTimeStamp(String mac) {
		Long timestamp = box_timer.get(mac);
		if (timestamp == null)
			timestamp = new Long(0);
		return timestamp;
	}

	public Integer get_boxProbenumberNow(String mac) {
		Long timestamp = get_boxTimeStamp(mac);
		long nowTime = System.currentTimeMillis();
		if (nowTime - timestamp < 600000) {
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
