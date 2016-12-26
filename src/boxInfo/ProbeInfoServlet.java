package boxInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import myServer.ProducerCustomerSet;
import serverUtil.Sql;

public class ProbeInfoServlet extends HttpServlet {
	private static final long serialVersionUID = -5900315927731373139L;
	private static Logger log = Logger.getLogger(ProbeInfoServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			if (!AuthUtil.auth(req, resp)) {
				return;
			}
			String box_mac = req.getParameter("box_mac");
			Long createtime = Long.parseLong(req.getParameter("createtime"));
			log.info("box_mac: " + box_mac + " createtime: " + createtime);

			// 创建sql对象
			List<List<String>> paramsList = new ArrayList<List<String>>();
			String query = "insert into yydc_probe1(box_mac, upload_time, mac, rssi1, rssi2, rssi3, probe_time) values(?,?,?,?,?,?,?)";
			BufferedReader reader = new BufferedReader(req.getReader());
			String str = null;

			while ((str = reader.readLine()) != null) {

				ArrayList<String> paramsMap = new ArrayList<String>();

				String[] one_arr = str.split("\\|");
				String mac = one_arr[0];
				// 处理rssi
				String[] rssi = one_arr[1].split(",");
				String rssi1 = rssi[0];
				String rssi2 = rssi[1];
				String rssi3 = rssi[2];

				String time = one_arr[2];
				if (time.length() == 13) {
					time = time.substring(0, 10);
				}

				paramsMap.add("String_" + box_mac);
				paramsMap.add("long_" + createtime);
				paramsMap.add("String_" + mac);
				paramsMap.add("int_" + rssi1);
				paramsMap.add("int_" + rssi2);
				paramsMap.add("int_" + rssi3);
				paramsMap.add("long_" + time);

				paramsList.add(paramsMap);
			}

			if (!paramsList.isEmpty()) {
				Sql sql = new Sql(query, paramsList);
				ProducerCustomerSet.get_instance().producerMap.get("probe").add_Object(sql);// 使用名叫probe的Producer向任务队列添加任务
			}
			AuthUtil.generateResponse("0", resp);

		} catch (Exception e) {
			log.error(ProbeInfoServlet.class, e);
		}
	}
}
