package dbpool;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Query {
	public String queryString = "";

	public Query select(String attributes) {// "user,id"
		this.queryString = "select " + attributes;
		return this;
	}

	public Query update(String table) {
		this.queryString = "update " + table;
		return this;
	}

	public Query insertInto(String table_attr) {
		this.queryString = "insert into " + table_attr;
		return this;
	}
	
	public Query deleteFrom(String table){
		this.queryString = "delete from " + table;
		return this;
	}

	public Query values(List<Object> params) {
		this.queryString += " values (";
		for (int i = 0; i < params.size(); ++i) {
			if (params.get(i) instanceof Integer || params.get(i) instanceof Long || params.get(i) instanceof Float
					|| params.get(i) instanceof Double) {
				if (i < params.size() - 1) {
					this.queryString += params.get(i) + ", ";
				} else {
					this.queryString += params.get(i) + ") ";
				}
			}
			if (params.get(i) instanceof String) {
				if (i < params.size() - 1) {
					this.queryString += "'" + params.get(i) + "', ";
				} else {
					this.queryString += "'" + params.get(i) + "') ";
				}
			}
		}
		return this;
	}

	public Query set(HashMap<String, Object> attr_value) {
		this.queryString += " set ";

		Iterator<Entry<String, Object>> entries = attr_value.entrySet().iterator();
		int length = attr_value.size();
		int count = 0;
		while (entries.hasNext()) {
			++count;
			Map.Entry<String, Object> entry = entries.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			this.queryString += key + " = ";
			if (value instanceof Integer || value instanceof Long || value instanceof Float
					|| value instanceof Double) {
				if (count == length)
					this.queryString += value + " ";
				else
					this.queryString += value + ", ";
			} else if (value instanceof String) {
				if (count == length)
					this.queryString += "'" + value + "' ";
				else
					this.queryString += "'" + value + "', ";
			} else {
				return null;
			}
		}
		return this;
	}

	public Query table(String tables) {
		this.queryString += " from " + tables;
		return this;
	}

	public Query where(String attribute) {
		this.queryString += " where " + attribute;
		return this;
	}

	public Query andWhere(String attribute) {
		this.queryString += " and " + attribute;
		return this;
	}

	public Query orWhere(String attribute) {
		this.queryString += " or " + attribute;
		return this;
	}

	public Query equal(Object value) {
		if (value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) {
			this.queryString += " = " + value;
		} else if (value instanceof String) {
			this.queryString += " = '" + value + "'";
		} else {
			return null;// 非法数据类型
		}
		return this;
	}

	public Query bigger(Object value) {
		if (value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) {
			this.queryString += " > " + value;
		} else {
			return null;
		}
		return this;
	}

	public Query smaller(Object value) {
		if (value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) {
			this.queryString += " < " + value;
		} else {
			return null;
		}
		return this;
	}

	public Query notbigger(Object value) {
		if (value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) {
			this.queryString += " <= " + value;
		} else {
			return null;
		}
		return this;
	}

	public Query notSmaller(Object value) {
		if (value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) {
			this.queryString += " >= " + value;
		} else {
			return null;
		}
		return this;
	}

	public Query limit(int begin, int number) {
		this.queryString += " limit " + begin + " , " + number;
		return this;
	}

	public Query in(List<Object> list) {
		this.queryString += " in ( ";
		int i = 0;
		for (i = 0; i < list.size() - 1; ++i) {
			if (list.get(i) instanceof Integer || list.get(i) instanceof Long || list.get(i) instanceof Float
					|| list.get(i) instanceof Double) {
				this.queryString += list.get(i) + " , ";
			} else if (list.get(i) instanceof String) {
				this.queryString += "'" + list.get(i) + "' , ";
			} else {
				return null;
			}
		}
		if (list.get(i) instanceof Integer || list.get(i) instanceof Long || list.get(i) instanceof Float
				|| list.get(i) instanceof Double) {
			this.queryString += list.get(i) + " ) ";
		} else if (list.get(i) instanceof String) {
			this.queryString += "'" + list.get(i) + "' ) ";
		} else {
			return null;
		}
		return this;
	}

	public Query orderBy(String str) {
		this.queryString += " order by " + str;
		return this;
	}

	public void executeUpdate() {
		DBpool.get_instance().executeUpdate(queryString);
	}

	public List<Map<String, Object>> fetchAll() {
		return DBpool.get_instance().executeQuery(queryString);
	}

	public Map<String, Object> fetchOne() {
		List<Map<String, Object>> r = DBpool.get_instance().executeQuery(queryString);
		if (!r.isEmpty())
			return r.get(0);
		return null;
	}

	public List<Map<String, Object>> fetchGroup(String Group) {
		this.queryString += " group by " + Group;
		return DBpool.get_instance().executeQuery(queryString);
	}
}