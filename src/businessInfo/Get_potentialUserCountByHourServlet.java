package businessInfo;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import net.sf.json.JSONObject;

public class Get_potentialUserCountByHourServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Get_potentialUserCountByHourServlet.class);

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		try {
			String day = req.getParameter("day");
			String hour = req.getParameter("hour");
			String UID = req.getParameter("userId");
			String portalID = req.getParameter("portalId");
			String boxID = req.getParameter("boxId");

			// 返回结果
			Map<String, Object> resultMap = new HashMap<String, Object>();
			Param_check pc = new Param_check();
			List<Integer> hours = (hour == null ? null : pc.hourString_check(hour));

			resultMap.put("success", true);// 初始假设返回内容正确
			List<String> macList = new ArrayList<String>();// 用于存储所有附庸于这个用户，商铺的盒子mac
			if (pc.time_check(day, 8) && hours != null && (pc.boxID_check(boxID) || pc.UID_check(UID))) {
				if (UID != null) {// 查找代理/商户所管辖的盒子
					macList = new Get_box().get_boxByUserID(UID);
				} else {// 查找单一的盒子
					macList.add(boxID);
				} // 找出满足条件的盒子的mac
				if (macList.isEmpty())
					macList.add("null");
			}

			if (!macList.isEmpty()) {
				// 周围人群列表
				List<Integer> probeNum = new ArrayList<Integer>();
				Integer cnt = 0;
				Get_boxPotentialUser G_bpu = new Get_boxPotentialUser();
				for (int i = 0; i < hours.size(); ++i) {
					String begin = "", end = "";
					if (i == 0) {
						begin = day + "00";
						int temp = hours.get(i) + 1;
						end = temp < 10 ? day + "0" + temp : day + temp;
					} else {
						int temp1 = hours.get(i - 1) + 1;
						begin = temp1 < 10 ? day + "0" + temp1 : day + temp1;
						int temp2 = hours.get(i) + 1;
						end = temp2 < 10 ? day + "0" + temp2 : day + temp2;
					} // 得到每個時間段
					SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHH");
					long b = sd.parse(begin).getTime() / 1000;
					long e = sd.parse(end).getTime() / 1000;
					cnt = G_bpu.get_potentialUserFromPreprocess_period(b, e, macList);
					probeNum.add(cnt);
				}

				if (UID != null) {
					resultMap.put("userId", UID);
				} else if (portalID != null) {
					resultMap.put("portalId", portalID);
				} else {
					resultMap.put("boxId", boxID);
				}
				resultMap.put("userCount", probeNum);
			} else {
				resultMap.put("success", false);
			}
			JSONObject jsonObject = JSONObject.fromObject(resultMap);
			PrintWriter out = resp.getWriter();
			out.write(jsonObject.toString());// 返回结果
			out.close();
		} catch (Exception e) {
			log.error(Get_potentialUserCountByHourServlet.class, e);
		}
	}
}