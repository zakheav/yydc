package myServer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import boxInfo.ProbeInfoServlet;
import dbpool.DBpool;
import serverUtil.CacheViaRedis;
import threadPool.ThreadPool;

public class MyServer {
	private static Logger log = Logger.getLogger(MyServer.class);
	public static void main(String[] args) {
		
		try {
			PropertyConfigurator.configure("conf/log4j.properties");
			
			ThreadPool.get_instance();// 启动线程池
			DBpool.get_instance();// 启动连接池
			ProducerCustomerSet.get_instance();// 组装生产者和任务分发者，并启动分发者对象
			CacheViaRedis.getInstance();// 初始化redis
			
			Server server = new Server();
			ServerConnector connector = new ServerConnector(server);
			connector.setPort(8082);
			connector.setIdleTimeout(20000);
			
			server.setConnectors(new Connector[] { connector });
			ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/");
			
			// 接收上传数据任务
			context.addServlet(ProbeInfoServlet.class, "/probe");
			
			// 定时任务
			
			log.info("server start success");
			server.start();
			server.join();
		} catch(Exception e) {
			log.error(MyServer.class, e);
		}
	}
}
