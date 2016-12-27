package businessInfo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import net.sf.json.JSONObject;

public class Get_potentialUserCountServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Get_potentialUserCountServlet.class);

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		try {
			String beginTime = req.getParameter("from");
			String endTime = req.getParameter("end");
			String UID = req.getParameter("userId");
			String boxID = req.getParameter("boxId");
			String terminalMac = req.getParameter("terminalMac");
			Param_check pc = new Param_check();
			// 返回结果
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("success", true);// 初始假设返回内容正确
			List<String> macList = new ArrayList<String>();// 用于存储所有附庸于这个用户，商铺的盒子mac
			if (pc.boxID_check(boxID) || pc.UID_check(UID) || terminalMac != null) {// 输入了盒子ID或者店铺ID或者用户ID
				Get_box gb = new Get_box();
				// 找出满足条件的盒子的mac
				if (UID != null) {// 查找代理/商户所管辖的盒子
					macList = gb.get_boxByUserID(UID);
				} else if (boxID != null) {// 查找单一的盒子
					macList.add(boxID);
				} else {// 根据termianlmac查找与之绑定的盒子
					macList = gb.get_boxByTerminal(terminalMac);
				}
				if (macList.isEmpty())
					macList.add("null");
			}
			if (!macList.isEmpty()) {
				if (pc.time_check(beginTime, 10) && pc.time_check(endTime, 10)) {// 有输入时间
					// 分割时间段
					ArrayList<String> timeList = new ArrayList<String>();// 存放时间段的数组
					Long begin = Long.parseLong(beginTime);
					Long end = Long.parseLong(endTime);
					time_increase t = new time_increase();
					while (begin < end) {
						timeList.add(String.valueOf(begin));
						begin = t.increase(begin);
					} // 分割时间段
					if (timeList.isEmpty()) {
						resultMap.put("success", false);
					} // 时间序列和mac序列已经准备好了

					Map<String, Integer> time_user = new HashMap<String, Integer>();// 时间--人数
					Get_boxPotentialUser G_bpu = new Get_boxPotentialUser();

					for (int i = 0; i < timeList.size(); ++i) {
						int count = G_bpu.get_potentialUserFromPreprocess(timeList.get(i), macList);// 获取到所有盒子在所给时间段的probe数据
						time_user.put(timeList.get(i), count);
					}

					if (UID != null) {
						resultMap.put("userId", UID);
					} else {
						resultMap.put("boxId", boxID);
					}
					resultMap.put("userCount", time_user);

				} else {
					if (pc.time_check(beginTime, 10) || pc.time_check(endTime, 10)) {
						resultMap.put("success", false);
					} else {
						long cnt = 0;
						Iterator<String> macIt = macList.iterator();
						while (macIt.hasNext()) {
							String mac = macIt.next();
							Get_boxPotentialUser G_bpu = new Get_boxPotentialUser();
							cnt = cnt + G_bpu.get_potentialUser(mac);// 寻找每一个盒子在当前时间的接入用户数
						}

						if (UID != null) {
							resultMap.put("userId", UID);
						} else if (boxID != null) {
							resultMap.put("boxId", boxID);
						} else {
							resultMap.put("terminalMac", terminalMac);
						}
						resultMap.put("userCount", cnt);
					}
				}
			} else {
				resultMap.put("success", false);
			}

			JSONObject jsonObject = JSONObject.fromObject(resultMap);
			PrintWriter out = resp.getWriter();
			out.write(jsonObject.toString());// 返回结果
			out.close();
		} catch (Exception e) {
			log.error(Get_potentialUserCountServlet.class, e);
		}
	}
}
