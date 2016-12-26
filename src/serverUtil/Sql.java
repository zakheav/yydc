package serverUtil;

import java.util.List;

public class Sql{
	private String sqlStr;
	private List<List<String>> paramsList;
	public Sql( String str,  List<List<String>> params ){
		this.sqlStr = str;
		this.paramsList = params;
	}
	public List<List<String>> get_paramsList(){
		return paramsList;
	}
	public String get_sqlStr(){
		return sqlStr;
	}
}
