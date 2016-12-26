package serverUtil;

public class CacheViaRedis {
	/*
	 * 四个hashes结构
	 * 
	 * 第一个hashes存储 probeTimeStamp key名字叫probeTimeStamp 第二个hashes存储 probeNum
	 * key名字叫probeNum 第三个hashes存储 connectionTimeStamp key名字叫connectionTimeStamp
	 * 第四个hashes存储 connectionNum key名字叫connectionNum
	 * 
	 */

	private static CacheViaRedis instance = new CacheViaRedis();

	private CacheViaRedis() {
		RedisAPI redis = RedisAPI.getInstance();
		// 初始化redis数据库
		redis.hset("probeTimeStamp", "null", "0");
		redis.hset("probeNum", "null", "0");
		redis.hset("connectionTimeStamp", "null", "0");
		redis.hset("connectionNum", "null", "0");
	}

	public static CacheViaRedis getInstance() {
		return instance;
	}

	public Long get_boxTimeStamp(String mac) {
		RedisAPI redis = RedisAPI.getInstance();
		Long result = null;
		String pts = redis.hget("probeTimeStamp", mac);
		String cts = redis.hget("connectionTimeStamp", mac);

		if (pts == null)
			pts = "0";
		if (cts == null)
			cts = "0";
		result = Long.parseLong(pts) > Long.parseLong(cts) ? Long.parseLong(pts) : Long.parseLong(cts);
		return result;
	}

	public int get_boxProbenumberNow(String mac) {
		RedisAPI redis = RedisAPI.getInstance();
		String pts = redis.hget("probeTimeStamp", mac);
		String probeNum = redis.hget("probeNum", mac);

		if (pts == null)
			pts = "0";
		long nowTime = System.currentTimeMillis();
		if (nowTime - Long.parseLong(pts) < 600000) {
			return Integer.parseInt(probeNum);
		} else {
			return 0;
		}
	}

	public void set_boxProbenumberNow(String mac, long probeTimeStamp, int probeNum) {
		RedisAPI redis = RedisAPI.getInstance();
		redis.hset("probeTimeStamp", mac, probeTimeStamp + "");
		redis.hset("probeNum", mac, probeNum + "");
	}

	public int get_boxConnectionNum(String mac) {// 的到當前指定盒子的連接數
		RedisAPI redis = RedisAPI.getInstance();
		String cts = redis.hget("connectionTimeStamp", mac);
		String connectNum = redis.hget("connectionNum", mac);

		if (cts == null)
			cts = "0";
		long nowTime = System.currentTimeMillis();
		if (nowTime - Long.parseLong(cts) < 600000) {
			return Integer.parseInt(connectNum);
		} else {
			return 0;
		}
	}

	public void set_boxConnectionnumberNow(String mac, long connectionTimeStamp, int connectionNum) {
		RedisAPI redis = RedisAPI.getInstance();
		redis.hset("connectionTimeStamp", mac, connectionTimeStamp + "");
		redis.hset("connectionNum", mac, connectionNum + "");
	}
}
