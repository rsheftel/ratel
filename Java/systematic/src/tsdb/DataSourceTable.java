package tsdb;

import db.clause.*;
import db.columns.*;
import db.tables.TSDB.*;

public class DataSourceTable extends DataSourceBase {
    private static final long serialVersionUID = 1L;
	public static final DataSourceTable DATA_SOURCE = new DataSourceTable();
	public DataSourceTable() {
		super("source");
	}

	public int id(String name) {
		return C_DATA_SOURCE_ID.value(C_DATA_SOURCE_NAME.is(name));
	}

	public Clause idMatches(IntColumn other, String name) {
		return C_DATA_SOURCE_ID.is(other).and(C_DATA_SOURCE_NAME.is(name));
	}

}
