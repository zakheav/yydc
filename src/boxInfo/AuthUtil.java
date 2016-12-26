package boxInfo;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import serverUtil.CommUtil;
import serverUtil.MD5Util;


public class AuthUtil {

	private static Logger log = Logger.getLogger(AuthUtil.class);

	private static String token = "multi-screen";

	public static void generateResponse(String result, HttpServletResponse resp) throws IOException {
		String nonce = CommUtil.getUUID();
		String createtime = (System.currentTimeMillis() / 1000) + "";
		String digest = MD5Util.MD5(result + nonce + createtime + AuthUtil.token);
		resp.addHeader("result", result);
		resp.addHeader("nonce", nonce);
		resp.addHeader("createtime", createtime);
		resp.addHeader("digest", digest);
		resp.setStatus(200);
		PrintWriter out = resp.getWriter();
		out.write("success");
		out.close();
	}

	public static boolean auth(HttpServletRequest req, HttpServletResponse resp) {
		String box_mac = req.getParameter("box_mac");
		String createtime = req.getParameter("createtime");
		String digest = req.getParameter("digest");
		
		if (box_mac == null || createtime == null || digest == null || box_mac.length() != 12) {
			log.error("box_mac or createtime or digest is null!");
			return false;
		}
		
		String t = MD5Util.MD5(box_mac + AuthUtil.token).substring(0, 4).toLowerCase();
		String mydigest = MD5Util.MD5(box_mac + createtime + t);

		if (mydigest.equalsIgnoreCase(digest)) {
			return true;
		}
		log.info("box_mac: " + box_mac + " auth error!");
		return false;
	}

}
