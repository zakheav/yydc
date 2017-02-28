package dbpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import lockFreeParallelFrameWorkUtil.RingBuffer;
import serverUtil.Sql;
import serverUtil.XML;

public class DBpool {
	private static Logger log = Logger.getLogger(DBpool.class);
	private final String url;
	private final String user;
	private final String password;
	private static final String driverClassName = "com.mysql.jdbc.Driver";

	private final int POOL_SIZE;
	private final RingBuffer pool;

	private static DBpool instance = new DBpool();

	private DBpool() {
		Map<String, String> DBpoolConf = new XML().getDBpoolConf();
		this.url = DBpoolConf.get("url");
		this.user = DBpoolConf.get("user");
		this.password = DBpoolConf.get("password");
		this.POOL_SIZE = Integer.parseInt(DBpoolConf.get("poolSize"));
		this.pool = new RingBuffer(POOL_SIZE);
		try {
			Class.forName(DBpool.driverClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} // 加载类到内存

		add_connection(this.POOL_SIZE - 1);
		System.out.println("dbpool start");
	}

	// ××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××
	// 连接池核心函数
	private void add_connection(int num) {// 向连接池pool添加连接
		for (int i = 0; i < num; ++i) {
			try {
				Connection conn = DriverManager.getConnection(this.url, this.user, this.password);
				pool.add_element(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private Connection get_conection() {// 相当于消费者
		Object conn = pool.get_element();
		if (conn != null) {// 连接池不为空
			return (Connection) conn;
		}

		pool.block.increase();

		synchronized (pool) {
			while (pool.isEmpty()) {
				try {
					pool.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			pool.block.decrease();
			
			conn = pool.get_element();
			return (Connection) conn;
		}
	}

	private void release_conection(Connection conn) {// 相当于生产者
		pool.add_element(conn);

		if (pool.block.get() > 0) {// 存在阻塞的线程
			synchronized (pool) {
				pool.notifyAll();
			}
		}
	}

	public static DBpool get_instance() {
		return instance;
	}

	// ××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××
	// 连接池外围函数
	private List<Map<String, Object>> resultSet_to_obj(ResultSet r) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
			ResultSetMetaData rsmd = r.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();
			while (r.next()) {
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 1; i <= numberOfColumns; ++i) {
					String name = rsmd.getColumnName(i);
					Object value = r.getObject(name);
					row.put(name, value);
				}
				result.add(row);
			}
		} catch (Exception e) {
			log.error(DBpool.class, e);
		}
		return result;
	}

	public List<Map<String, Object>> executeQuery(String queryString) {// 查询
		Connection conn = null;
		Connection e_conn = null;// 错误状态下重新分配的connection
		Statement stmt = null;
		Statement e_stmt = null;// e_conn生成的statement
		ResultSet rs = null;
		ResultSet e_rs = null;// e_stmt返回的结果集
		boolean connection_timeout = false;
		List<Map<String, Object>> result = null;
		try {
			conn = get_conection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(queryString);// 出问题
			result = resultSet_to_obj(rs);
			return result;
		} catch (SQLException e1) {// 可能连接失效
			try {
				connection_timeout = true;
				e_conn = DriverManager.getConnection(this.url, this.user, this.password);
				// 重新查询
				e_stmt = e_conn.createStatement();
				e_rs = e_stmt.executeQuery(queryString);
				result = resultSet_to_obj(e_rs);
				return result;
			} catch (SQLException e2) {
				log.error(DBpool.class, e2);
				return null;
			} finally {// 释放资源
				try {
					e_rs.close();
					e_stmt.close();
					release_conection(e_conn);
				} catch (SQLException e3) {
					log.error(DBpool.class, e3);
				}
			}
		} finally {// 释放资源
			try {
				if (!connection_timeout) {
					rs.close();
					stmt.close();
					release_conection(conn);
				} else {
					stmt.close();
					conn.close();
				}
			} catch (SQLException e4) {
				log.error(DBpool.class, e4);
			}
		}
	}

	public boolean executeUpdate(String queryString) {// 更新
		Connection conn = null;
		Connection e_conn = null;// 错误状态下重新分配的connection
		Statement stmt = null;
		Statement e_stmt = null;// e_conn生成的statement
		boolean connection_timeout = false;
		try {
			conn = get_conection();
			stmt = conn.createStatement();
			stmt.executeUpdate(queryString);// 出问题
			return true;
		} catch (SQLException e1) {// 可能连接失效
			try {
				connection_timeout = true;
				e_conn = DriverManager.getConnection(this.url, this.user, this.password);
				// 重新查询
				e_stmt = e_conn.createStatement();
				e_stmt.executeUpdate(queryString);
				return true;
			} catch (SQLException e2) {
				System.out.println(e2);
				log.error(DBpool.class, e2);
				return false;
			} finally {// 释放资源
				try {
					e_stmt.close();
					release_conection(e_conn);
				} catch (SQLException e3) {
					System.out.println(e3);
					log.error(DBpool.class, e3);
				}
			}
		} finally {// 释放资源
			try {
				if (!connection_timeout) {
					stmt.close();
					release_conection(conn);
				} else {
					stmt.close();
					conn.close();
				}
			} catch (SQLException e4) {
				System.out.println(e4);
				log.error(DBpool.class, e4);
			}
		}
	}

	public boolean executeBatchesUpdate(Sql sql) {
		Connection conn = null;
		Connection e_conn = null;// 错误状态下重新分配的connection
		PreparedStatement pstmt = null;
		PreparedStatement e_pstmt = null;// e_conn生成的statement
		boolean connection_timeout = false;
		try {
			conn = get_conection();
			pstmt = processPreparedStatement(conn, sql);
			pstmt.executeBatch();
			return true;
		} catch (SQLException e1) {// 由于连接过期导致异常
			try {
				connection_timeout = true;
				e_conn = DriverManager.getConnection(this.url, this.user, this.password);
				// 重新查询
				e_pstmt = processPreparedStatement(conn, sql);
				e_pstmt.executeBatch();
				return true;
			} catch (SQLException e2) {
				log.error(DBpool.class, e2);
				return false;
			} finally {
				try {
					e_pstmt.close();
					release_conection(e_conn);
				} catch (SQLException e3) {
					log.error(DBpool.class, e3);
				}
			}
		} finally {
			try {
				if (!connection_timeout) {
					pstmt.close();
					release_conection(conn);
				} else {
					pstmt.close();
					conn.close();
				}
			} catch (SQLException e4) {
				log.error(DBpool.class, e4);
			}
		}
	}

	private PreparedStatement processPreparedStatement(Connection conn, Sql sql) throws SQLException {// 处理sql结构，返回PreparedStatement
		String query = sql.get_sqlStr();
		List<List<String>> paramsList = sql.get_paramsList();
		PreparedStatement pstmt = conn.prepareStatement(query);
		for (int i = 0; i < paramsList.size(); ++i) {// 迭代每一条记录中
			// 迭代每一条记录中的每个参数
			int index = 0;
			List<String> p = paramsList.get(i);
			for (int j = 0; j < p.size(); ++j) {
				++index;
				String[] param = p.get(j).split("_");
				if (param[0].equals("String")) {
					if (param.length == 1)
						pstmt.setString(index, "null");
					else
						pstmt.setString(index, (String) param[1]);
				} else if (param[0].equals("long")) {
					if (param.length == 1 || param[1].equals(""))
						pstmt.setLong(index, Long.parseLong("0"));
					else
						pstmt.setLong(index, Long.parseLong(param[1]));
				} else if (param[0].equals("int")) {
					if (param.length == 1 || param[1].equals(""))
						pstmt.setInt(index, Integer.parseInt("0"));
					else
						pstmt.setInt(index, Integer.parseInt(param[1]));
				}
			}
			pstmt.addBatch();
		}
		return pstmt;
	}
}
