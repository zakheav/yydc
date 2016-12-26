package preprocessHandler;

import dbpool.DBpool;
import preprocess.Handler;
import serverUtil.Sql;

public class InsertInfoHandler extends Handler {
	@Override
	protected void preprocess(Object params) {
		Sql sql = (Sql)params;
		DBpool.get_instance().executeBatchesUpdate(sql);
	}
}
