package db;

import static amazon.S3Cache.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Systematic.*;

import java.sql.*;
import java.util.*;

import util.*;

public class Db {
	public static abstract class Transaction {
        public void execute() {
            try {
                startTransaction();
                transact();
                commit();
            } catch (RuntimeException e) {
                Db.rollback();
                throw bomb("transaction failed and rolled back", e);
            }
        }
        public abstract void transact();
    }

    private final static ThreadLocal<Connection> connection = new ThreadLocal<Connection>() {
        @Override protected void finalize() throws Throwable {
            bombNotNull(get(), "unclosed connection leaked!");
        }
    };
    private final static ThreadLocal<Long> lastTransactionStart = local();
	private final static ThreadLocal<Integer> timeoutSecs = local(300);
	private static boolean noCommitTestMode = false;
	private static boolean inReadOnlyMode = false;
	private final static ThreadLocal<Boolean> explicitlyCommitted = new ThreadLocal<Boolean>();

	static { explicitlyCommitted.set(false); }

	public static String currentDb() {
		return string("select db_name()");
	}
	
	public static String string(String sql) {
		Connection c = connection();
		ResultSet rs = null;
		Statement st = null;
		try {
			st = c.createStatement();
			st.setQueryTimeout(timeoutSecs.get());
			rs = st.executeQuery(sql);
			rs.next();
			return rs.getString(1);
		} catch (SQLException e) {
			throw bomb("sql failed\n" + sql, e);
		} finally {
			close(rs, st);
		}
	}

	private static void close(ResultSet rs, Statement st) {
		try {
			if (rs != null) rs.close();
		} catch (SQLException uncatchable) {
			uncatchable.printStackTrace(); 
		}
		close(st);
	}

	private static void close(Statement st) {
		try {
			if (st != null) st.close();
		} catch (SQLException uncatchable) {
			uncatchable.printStackTrace(); 
		}
	}

	private static Connection connection() {
		if (connection.get() == null) {
			connection.set(uncachedConnection());
		}
		return connection.get();
	}

	private static Connection uncachedConnection() {
	    bombIf(sqsDbMode(), "cannnot connect to DB in SQS DB mode!");
		long start = System.currentTimeMillis();
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			throw bomb("driver class not found!", e1);
		}
		String server = Systematic.dbServer();
		String user = Systematic.dbUser();
		String password = Systematic.dbPassword();
		String url = "jdbc:jtds:sqlserver://" + server + ":2433;prepareSQL=2";
		try {
			Connection c = DriverManager.getConnection(url, user, password);
			if(Log.debugSql())
			    Log.info("Acquired connection for " + user + "@" + server + " in " + Times.reallyMillisSince(start) + " millis");
			return c;
		} catch (SQLException e) {
			throw bomb(e);
		}
	}
	
    public static boolean tableExists(String dbName, String tableName) {
        Connection c = connection();
        ResultSet rs = null;
        try {
            DatabaseMetaData meta = c.getMetaData();
            rs = meta.getTables(dbName, null, tableName, array("TABLE"));
            return rs.next();
        } catch (SQLException e) {
            throw bomb("failed to get meta data", e);
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (SQLException doNotThrowFromFinally) {
                doNotThrowFromFinally.printStackTrace();
            }
        }
    }

	static List<String> tableNames(String dbName) {
		Connection c = connection();
		try {
			DatabaseMetaData meta = c.getMetaData();
			ResultSet rs = meta.getTables(dbName, null, "%", array("TABLE"));
			List<String> result = new ArrayList<String>();
			while (rs.next()) result.add(rs.getString("TABLE_CAT") + ".." + rs.getString("TABLE_NAME"));
			return result;
		} catch (SQLException e) {
			throw bomb("failed to get meta data", e);
		}
	}

	static final List<String> SCHEMA_NAMES = list("TSDB", "SystemDB", "IvyDB");
    
    
	static StringRow tableDefinition(String name) {
		Connection c = connection();

		String schema = name.replaceFirst("(.*)\\.\\..*", "$1");
		String tableName = name.replaceFirst(".*\\.\\.(.*)", "$1");
		try {
			DatabaseMetaData meta = c.getMetaData();
			if (schema.equals(tableName)) 
				for (String possible : SCHEMA_NAMES) {
					List<StringRow> definitions = definitions(possible, tableName, meta);
					if (definitions.isEmpty()) continue;
					return the(definitions);
				}
			return the(definitions(schema, tableName, meta));
		} catch (Exception e) {
			throw bomb("could not find table " + Sql.quote(name) + ": failed to get meta data", e);
		}
	}


	private static List<StringRow> definitions(String schema, String tableName, DatabaseMetaData meta)
		throws SQLException {
		ResultSet rs = meta.getTables(schema, null, tableName, array("TABLE"));
		List<StringRow> definitions = stringMaps(rs);
		return definitions;
	}

	private static List<StringRow> stringMaps(ResultSet rs) {
		List<StringRow> rows = new ArrayList<StringRow>();
		try {
			while (rs.next()) rows.add(extractOneStringRow(rs));
			return rows;
		} catch (SQLException e) {
			throw bomb("result set operation failed getting one map", e);
		}
	}

	private static StringRow extractOneStringRow(ResultSet rs) throws SQLException {
		StringRow result = new StringRow();
		ResultSetMetaData meta = rs.getMetaData();
		int numColumns = meta.getColumnCount();
		for(int i = 1; i <= numColumns; i++)
			result.put(meta.getColumnName(i), rs.getString(i));
		return result;
	}

	static List<StringRow> columnDefinitions(SchemaTable table) {
		Connection c = connection();
		try {
			DatabaseMetaData meta = c.getMetaData();
			ResultSet rs = meta.getColumns(table.catalog(), null, table.tableName(), null);
			return nonEmpty(stringMaps(rs));
		} catch (SQLException e) {
			throw bomb("failed to get meta data", e);
		}
	}

	static List<Row> rows(Select select) {
		return rows(select, false);
	}

	static List<Row> rows(Select select, boolean expectOne) {
        return rowsWithRestart(select, expectOne, 30);
	}

	public static String fakeSql(Select select) {
	    String sql = select.toSql("\n");
	    LoggingPreparedStatement st = new LoggingPreparedStatement(new NothingPreparedStatement());
        try {
            select.bindInto(1, st);
        } catch (SQLException e) {
            throw bomb("fake sql failed (for real)\n" + sql, e);
        }
        return st.replaceBinds(sql);
	}
	
	

    private static List<Row> rowsWithRestart(Select select, boolean expectOne, int timeout) {
        long start = System.currentTimeMillis();
        if (sqsDbMode()) return sqsRows(select, expectOne);
        
		String sql = select.toSql("\n");

		Connection c = connection();
		ResultSet rs = null;
		LoggingPreparedStatement st = null;
		try {
			st = new LoggingPreparedStatement(c.prepareStatement(sql));
			st.setQueryTimeout(timeout);
			select.bindInto(1, st);
			if (Log.debugSql()) {
				sql = st.replaceBinds(sql);
				Log.info("executing " + sql);
			}
			rs = st.executeQuery();
			List<Row> result = maps(rs, select.columns());
			if(Log.debugSql()) Log.info("took " + Times.reallyMillisSince(start) + " millis");
			if (expectOne && result.size() != 1)
				bomb("expected single result but got " + result.size() + " from\n" + sql + "\nbound with\n" + st.params);
			return saveResultsIfNeeded(select, result);
		} catch (SQLException e) {
		    if(timeout == 30 && e.getMessage().matches(".*query has timed out.*")) {
		        int newTimeout = timeoutSecs.get();
                Log.info("query timed out.  Rerunning with timeout " + newTimeout);
		        return rowsWithRestart(select, expectOne, newTimeout);
		    }
			if(st != null)
				sql = st.replaceBinds(sql);
			throw bomb("sql failed\n" + sql, e);
		} finally {
			close(rs, st);
		}
    }

    @SuppressWarnings("unchecked") private static List<Row> sqsRows(Select select, boolean expectOne) {
        List<Row> result = s3cache().retrieve(select);
        if(expectOne && result.size() != 1)
            bomb("rows returned != 1\nselect:\n" + select + "\nrows\n" + result);
        return result;
    }


    private static List<Row> maps(ResultSet rs, List<Column<?>> columns) {
		List<Row> rows = new ArrayList<Row>();
		try {
			int i = 0;
			while (rs.next()) {
				if (i++ % 1000 == 999) Log.dot();
				rows.add(extractRow(rs, columns));
			}
			return rows;
		} catch (SQLException e) {
			throw bomb("result set operation failed getting one map", e);
		}
	}

	private static Row extractRow(ResultSet rs, List<Column<?>> columns) throws SQLException {
		Row result = new Row();
		ResultSetMetaData meta = rs.getMetaData();
		int numColumns = meta.getColumnCount();
		if (numColumns != columns.size()) 
			bomb("did not get correct number of columns, expected \n" + columns + "\nbut got\n" + columns(meta));
		int index = 1;
		for (Column<?> column : columns) {
			result.put(column.cellFrom(rs, index++));
		}
		return result;
	}

	private static List<String> columns(ResultSetMetaData meta) throws SQLException {
		int columns = meta.getColumnCount(); 
		List<String> result = new ArrayList<String>();
		for(int i = 1; i <= columns; i++) 
			result.add(meta.getColumnName(i));
		return result; 
	}

	static <T> T value(SelectOne<T> select) {
		return select.value(row(select));
	}

	static <T> List<T> values(SelectOne<T> select) {
		return select.values(rows(select));
	}
	
	static Row row(Select select) {
		return the(rows(select, true));
	}

	static void close() {
		try {
			if (connection.get() == null) return;
			connection().close();
			connection.set(null);
		} catch (SQLException e) {
			throw bomb("failed to close connection", e);
		}
	}

	public static void commit() {
        if (connection.get() == null) return;
        explicitlyCommitted.set(true);
        if (noCommitTestMode) return;
        reallyCommit();
	}

    public static void reallyCommit() {
        try {
            Connection c = connection();
            if (inTransaction()) {
                c.commit();
                c.setAutoCommit(true);
                double footprintSeconds = Times.reallySecondsSince(lastTransactionStart.get());
                if(Log.debugSql()) Log.info("COMMIT committed existing transaction with footprint " + footprintSeconds + " seconds.");
                else if (footprintSeconds > 1) Log.info("COMMIT with footprint " + footprintSeconds + " seconds.");
            }
        } catch (SQLException e) {
            throw bomb("failed to commit", e);
        }
    }


    public static boolean inTransaction() {
        if(sqsDbMode()) return false;
        try {
            return !connection().getAutoCommit();
        } catch (SQLException e) {
            throw bomb("failed to get AutoCommit setting", e);
        }
    }
	
	public static void rollback() {
		if(!noCommitTestMode) reallyRollback();
	}

	public static void reallyRollback() {
		try {
			if (connection.get() == null) return;
			Connection c = connection();
			if (inTransaction()) {
				c.rollback();
				c.setAutoCommit(true);
				if(Log.debugSql()) Log.info("ROLLBACK rolled back existing transaction");
				explicitlyCommitted.set(false);
			}
		} catch (SQLException e) {
			throw bomb("failed to rollback and close connection", e);
		}
	}
	
	static void executeUnprepared(Executable executable, boolean expectOne) {
		long start = System.currentTimeMillis();
		String sql = executable.toSql();
		Connection c = connection();
		LoggingPreparedStatement st = null;
		Statement replacement = null;
		try {
			beginTransactionIfNeeded();
			st = new LoggingPreparedStatement(c.prepareStatement(sql));
			executable.bindInto(1, st);
			sql = st.replaceBinds(sql);
			if(Log.debugSql())  {
				Log.info("executing " + sql);
			}
			replacement = c.createStatement();
			replacement.setQueryTimeout(timeoutSecs.get());
			int updated = replacement.executeUpdate(sql);
			if(Log.debugSql()) Log.info("took " + Times.reallyMillisSince(start) + " millis");
			if (expectOne && updated != 1)
				bomb("expected 1 update but got " + updated + " from\n" + sql + 
						"\nbound with\n" + st.params);
		} catch (SQLException e) {
			if(st != null)
				sql = st.replaceBinds(sql);
			throw bomb("sql failed\n" + sql, e);
		} finally {
			close(st);
			close(replacement);
		}
	}

	static void execute(Executable executable, boolean expectOne) {
		long start = System.currentTimeMillis();
		String sql = executable.toSql();
		Connection c = connection();
		LoggingPreparedStatement st = null;
		try {
			beginTransactionIfNeeded();
			st = new LoggingPreparedStatement(c.prepareStatement(sql));
			st.setQueryTimeout(timeoutSecs.get());
			executable.bindInto(1, st);
			if(Log.debugSql())  {
				sql = st.replaceBinds(sql);
				Log.info("executing " + sql);
			}
			int updated = st.executeUpdate();
			if(Log.debugSql()) Log.info("took " + Times.reallyMillisSince(start) + " millis");
			if (expectOne && updated != 1)
				bomb("expected 1 update but got " + updated + " from\n" + sql + 
						"\nbound with\n" + st.params);
		} catch (SQLException e) {
			if(st != null)
				sql = st.replaceBinds(sql);
			throw bomb("sql failed\n" + sql, e);
		} finally {
			close(st);
		}
	}
	
	static void batchInsert(BatchInsert batchInsert) {
		long start = System.currentTimeMillis();
		String sql = batchInsert.toSql();
		Connection c = connection();
		LoggingPreparedStatement st = null;
		try {
			beginTransactionIfNeeded();
			st = new LoggingPreparedStatement(c.prepareStatement(sql));
			st.setQueryTimeout(timeoutSecs.get());
			batchInsert.bindInto(st);
			if(Log.debugSql()) Log.info("executing BATCH" + sql);
			st.executeBatch();
			if(Log.debugSql()) Log.info("took " + Times.reallyMillisSince(start) + " millis");
		} catch (SQLException e) {
			throw bomb("sql failed\n" + sql, e);
		} finally {
			close(st);
		}
	}

	private static void beginTransactionIfNeeded() {
	    if (inTransaction()) return;
	    bombIf(inReadOnlyMode, "cannot start transaction, db in readonly mode!");
		startTransactionIgnoringReadonly();
	}


    public static void startTransactionIgnoringReadonly() {
        bombIf(inTransaction(), "cannot start transaction from within transaction!");
        bombIf(sqsDbMode(), "cannot start transactions in SQS DB mode");
        if(Log.debugSql()) Log.info("begin transaction");
		try {
		    lastTransactionStart.set(System.currentTimeMillis());
            connection().setAutoCommit(false);
        } catch (SQLException e) {
            throw bomb("failed to set autocommit on (start transaction)", e);
        }
    }


	public static void execute(String sql) {
		long start = System.currentTimeMillis();
		try {
			beginTransactionIfNeeded();
			if(Log.debugSql()) Log.info("executing arbitrary sql:" + sql);
			Statement st = connection().createStatement();
			st.setQueryTimeout(timeoutSecs.get());
			st.execute(sql);
			if(Log.debugSql()) Log.info("took " + Times.reallyMillisSince(start) + " millis");
		} catch (SQLException e) {
			throw bomb("sql failed\n" + sql, e);
		} 
	}

	public static int setQueryTimeout(int timeoutSecs) {
		int oldTimeout = Db.timeoutSecs.get();
		Db.timeoutSecs.set(timeoutSecs);
		return oldTimeout;
	}


	public static void beInNoCommitTestMode() {
		Db.noCommitTestMode  = true;
	}
	
	public static void getOutOfNoCommitTestMode() {
		bombUnless(isDevDb(), "cannot get out of no commit test mode unless you are on dev");
		Db.rollback();
		Db.noCommitTestMode  = false;
	}
	
	public static void beInReadOnlyMode() { 
	    beInReadOnlyMode(true);
	}

	public static boolean explicitlyCommitted() {
		Boolean result = explicitlyCommitted.get();
		explicitlyCommitted.set(false);
		return result;
	}


	public static int identity() {
		return Integer.valueOf(string("select @@IDENTITY"));
	}


    public static boolean hasPrimaryKey(String dbName, String tableName) {
        ResultSet keys = null;
        try {
            DatabaseMetaData meta = connection().getMetaData();
            keys = meta.getPrimaryKeys(null, dbName, tableName);
            return !stringMaps(keys).isEmpty();
        } catch (SQLException e) {
            throw bomb("failed", e);
        } finally {
            if (keys != null) 
                try { keys.close(); } 
                catch (SQLException ignoreDoNotThrowFromCatch) { 
                    ignoreDoNotThrowFromCatch.printStackTrace(); 
                }
        }
    }

    public static void startTransaction() {
        bombIf(inTransaction(), "cannot start transaction from within transaction!");
        beginTransactionIfNeeded();
    }

    public static void beInReadOnlyMode(boolean b) {
        inReadOnlyMode = b;
    }

    /** "side-step" in this context, means, in a separate thread (new thread locals)
     * this has the effect of sidestepping the current transaction while writing committed data to the db */
    public static <T> T doInSidestepTransaction(final SidestepThreadResult<T> transaction) {
        final List<T> result = empty();
        Thread t = new Thread() {
            @Override public void run() {
                result.add(transaction.resultProtected());
                Db.commit();
            }
        };
        t.start();
        join(t);
        return the(result);
    }



}
