package preprocessHandler;

import dbpool.DBpool;
import preprocess.Handler;
import serverUtil.Sql;

public class InsertInfoHandler implements Handler {
	@Override
	public void preprocess(Object params) {
		Sql sql = (Sql)params;
		DBpool.get_instance().executeBatchesUpdate(sql);
	}
}
