package businessInfo;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import dbpool.Query;
import serverUtil.MemCache;

public class Get_boxPotentialUser {// 获取盒子周围客流情况
	private Logger log = Logger.getLogger(Get_boxPotentialUser.class);

	public long get_potentialUser(String boxMac) {// 当前时刻某个盒子的周围人数
		return MemCache.getInstance().get_boxProbenumberNow(boxMac);
	}

	public int get_potentialUserFromPreprocess(String timeSpot, List<String> macList) {
		// 把timeSpot转化为时间戳，timeSpot代表的时间区间是从timeSpot开始的往后一小时，与yydc_probecache中的time意义一致
		Long timeStamp = new Long(0);
		try {
			SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHH");
			timeStamp = sd.parse(timeSpot).getTime() / 1000;
		} catch (ParseException e) {
			log.error(Get_boxPotentialUser.class, e);
			e.printStackTrace();
		}

		List<Map<String, Object>> results = new Query().select("*").table("yydc_probecache").where("time")
				.equal(timeStamp).fetchAll();
		int count = 0;
		for (int i = 0; i < results.size(); ++i) {
			String boxmac = (String) (results.get(i).get("box_mac"));
			if (macList.contains(boxmac)) {
				count = count + (int) (results.get(i).get("count"));
			}
		}
		return count;
	}

	public int get_potentialUserFromPreprocess_period(Long begin, Long end, List<String> macList) {// 求解一段区间之内的某个macList内盒子的潜在用户数
		BigDecimal count = new BigDecimal(0);
		for (int i = 0; i < macList.size(); ++i) {
			String boxmac = macList.get(i);

			List<Map<String, Object>> results = new Query().select("sum(count) as count").table("yydc_probecache")
					.where("time").notSmaller(begin).andWhere("time").smaller(end).andWhere("box_mac").equal(boxmac)
					.fetchGroup("box_mac");
			if (!results.isEmpty())
				count = count.add((BigDecimal) results.get(0).get("count"));
		}
		return count.intValue();
	}
}
