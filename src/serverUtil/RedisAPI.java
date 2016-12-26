package serverUtil;

import java.util.Map;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisAPI {
	private static JedisPool pool;
	private static RedisAPI instance = new RedisAPI();
	private static Logger log = Logger.getLogger(RedisAPI.class);

	private RedisAPI() {
		if (pool == null) {
			Map<String, String> portConf = new XML().getRedisPoolConf();
			JedisPoolConfig config = new JedisPoolConfig();
			// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
			// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
			config.setMaxActive(500);
			// 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
			config.setMaxIdle(5);
			// 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
			config.setMaxWait(1000 * 100);
			// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
			config.setTestOnBorrow(true);
			pool = new JedisPool(config, "127.0.0.1", Integer.parseInt(portConf.get("port")));
		}
	}

	public static RedisAPI getInstance() {
		return instance;
	}

	/**
	 * 返还到连接池
	 * 
	 * @param pool
	 * @param redis
	 */
	public void returnResource(JedisPool pool, Jedis redis) {
		if (redis != null) {
			pool.returnResource(redis);
		}
	}

	public String hget(String tableName, String field) {
		String value = null;
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			value = jedis.hget(tableName, field);
		} catch (Exception e) {
			// 释放redis对象
			pool.returnBrokenResource(jedis);
			log.error(RedisAPI.class, e);
		} finally {
			// 返还到连接池
			returnResource(pool, jedis);
		}
		return value;
	}
	
	public void hset(String tableName, String field, String value) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.hset(tableName, field, value);
		} catch (Exception e){
			// 释放redis对象
			pool.returnBrokenResource(jedis);
			log.error(RedisAPI.class, e);
		} finally {
			// 返还到连接池
			returnResource(pool, jedis);
		}
		
	}
}
