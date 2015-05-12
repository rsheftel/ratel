package db;

import static util.Errors.*;
import static util.Objects.*;

import java.sql.*;
import java.util.*;

import util.*;
public class BatchInsert {

	private static final int BATCH_SIZE = 10000;
	private final Table table;
	private List<Row> rows;

	public BatchInsert(Table table, List<Row> rows) {
		this.table = table;
		bombIf(rows.isEmpty(), "cannot use batch insert to insert zero rows. please check for this condition.");
		this.rows = rows;
	}

	public void execute() {
		while(rows.size() > BATCH_SIZE) {
			List<Row> nextRows = rows.subList(0, BATCH_SIZE);
			rows = rows.subList(BATCH_SIZE, rows.size());
			Db.batchInsert(new BatchInsert(table, nextRows));
			Log.dot("%");
		}
		Db.batchInsert(this);
	}

	public String toSql() {
		return new Insert(table, first(rows)).toSql();
	}

	public void bindInto(LoggingPreparedStatement st) throws SQLException {
		int i = 0;
		for (Row row : rows) {
			if (i++ % 1000 == 999) Log.dot();
			row.bindInto(1, st, first(rows));
			st.addBatch();
		}
	}
	
	

}
