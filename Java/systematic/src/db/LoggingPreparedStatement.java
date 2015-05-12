package db;

import static util.Sql.*;

import java.io.*;
import java.math.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import util.*;

public class LoggingPreparedStatement implements PreparedStatement {

	
	
	private final PreparedStatement statement;
	List<Object> params = new ArrayList<Object>();

	public LoggingPreparedStatement(PreparedStatement statement) {
		this.statement = statement;
	}

	public void addBatch() throws SQLException {
		statement.addBatch();
	}

	public void addBatch(String sql) throws SQLException {
		statement.addBatch(sql);
	}

	public void cancel() throws SQLException {
		statement.cancel();
	}

	public void clearBatch() throws SQLException {
		statement.clearBatch();
	}

	public void clearParameters() throws SQLException {
		statement.clearParameters();
	}

	public void clearWarnings() throws SQLException {
		statement.clearWarnings();
	}

	public void close() throws SQLException {
		statement.close();
	}

	public boolean execute() throws SQLException {
		return statement.execute();
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		return statement.execute(sql, autoGeneratedKeys);
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		return statement.execute(sql, columnIndexes);
	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		return statement.execute(sql, columnNames);
	}

	public boolean execute(String sql) throws SQLException {
		return statement.execute(sql);
	}

	public int[] executeBatch() throws SQLException {
		return statement.executeBatch();
	}

	public ResultSet executeQuery() throws SQLException {
		return statement.executeQuery();
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		return statement.executeQuery(sql);
	}

	public int executeUpdate() throws SQLException {
		return statement.executeUpdate();
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		return statement.executeUpdate(sql, autoGeneratedKeys);
	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		return statement.executeUpdate(sql, columnIndexes);
	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		return statement.executeUpdate(sql, columnNames);
	}

	public int executeUpdate(String sql) throws SQLException {
		return statement.executeUpdate(sql);
	}

	public Connection getConnection() throws SQLException {
		return statement.getConnection();
	}

	public int getFetchDirection() throws SQLException {
		return statement.getFetchDirection();
	}

	public int getFetchSize() throws SQLException {
		return statement.getFetchSize();
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		return statement.getGeneratedKeys();
	}

	public int getMaxFieldSize() throws SQLException {
		return statement.getMaxFieldSize();
	}

	public int getMaxRows() throws SQLException {
		return statement.getMaxRows();
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return statement.getMetaData();
	}

	public boolean getMoreResults() throws SQLException {
		return statement.getMoreResults();
	}

	public boolean getMoreResults(int current) throws SQLException {
		return statement.getMoreResults(current);
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		return statement.getParameterMetaData();
	}

	public int getQueryTimeout() throws SQLException {
		return statement.getQueryTimeout();
	}

	public ResultSet getResultSet() throws SQLException {
		return statement.getResultSet();
	}

	public int getResultSetConcurrency() throws SQLException {
		return statement.getResultSetConcurrency();
	}

	public int getResultSetHoldability() throws SQLException {
		return statement.getResultSetHoldability();
	}

	public int getResultSetType() throws SQLException {
		return statement.getResultSetType();
	}

	public int getUpdateCount() throws SQLException {
		return statement.getUpdateCount();
	}

	public SQLWarning getWarnings() throws SQLException {
		return statement.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		return statement.isClosed();
	}

	public boolean isPoolable() throws SQLException {
		return statement.isPoolable();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return statement.isWrapperFor(iface);
	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		params.add(x);
		statement.setArray(parameterIndex, x);
	}
	
	private void unimplemented() {
		Errors.bomb("unimplemented");
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		unimplemented(); 
		statement.setAsciiStream(parameterIndex, x, length);
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		unimplemented();
		statement.setAsciiStream(parameterIndex, x, length);
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		unimplemented();
		statement.setAsciiStream(parameterIndex, x);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		params.add(x);
		statement.setBigDecimal(parameterIndex, x);
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		unimplemented();
		statement.setBinaryStream(parameterIndex, x, length);
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		unimplemented();
		statement.setBinaryStream(parameterIndex, x, length);
	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		unimplemented();
		statement.setBinaryStream(parameterIndex, x);
	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		unimplemented();
		statement.setBlob(parameterIndex, x);
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		unimplemented();
		statement.setBlob(parameterIndex, inputStream, length);
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		unimplemented();
		statement.setBlob(parameterIndex, inputStream);
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		params.add(x);
		statement.setBoolean(parameterIndex, x);
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		params.add(x);
		statement.setByte(parameterIndex, x);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		params.add(x);
		statement.setBytes(parameterIndex, x);
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		unimplemented();
		statement.setCharacterStream(parameterIndex, reader, length);
	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		unimplemented();
		statement.setCharacterStream(parameterIndex, reader, length);
	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		unimplemented();
		statement.setCharacterStream(parameterIndex, reader);
	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		unimplemented();
		statement.setClob(parameterIndex, x);
	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		unimplemented();
		statement.setClob(parameterIndex, reader, length);
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		unimplemented();
		statement.setClob(parameterIndex, reader);
	}

	public void setCursorName(String name) throws SQLException {
		statement.setCursorName(name);
	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		unimplemented();
		statement.setDate(parameterIndex, x, cal);
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		params.add(x);
		statement.setDate(parameterIndex, x);
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		params.add(x);
		statement.setDouble(parameterIndex, x);
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		statement.setEscapeProcessing(enable);
	}

	public void setFetchDirection(int direction) throws SQLException {
		statement.setFetchDirection(direction);
	}

	public void setFetchSize(int rows) throws SQLException {
		statement.setFetchSize(rows);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		params.add(x);
		statement.setFloat(parameterIndex, x);
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		params.add(x);
		statement.setInt(parameterIndex, x);
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		params.add(x);
		statement.setLong(parameterIndex, x);
	}

	public void setMaxFieldSize(int max) throws SQLException {
		statement.setMaxFieldSize(max);
	}

	public void setMaxRows(int max) throws SQLException {
		statement.setMaxRows(max);
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		unimplemented();
		statement.setNCharacterStream(parameterIndex, value, length);
	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		unimplemented();
		statement.setNCharacterStream(parameterIndex, value);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		unimplemented();
		statement.setNClob(parameterIndex, value);
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		unimplemented();
		statement.setNClob(parameterIndex, reader, length);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		unimplemented();
		statement.setNClob(parameterIndex, reader);
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		params.add(value);
		statement.setNString(parameterIndex, value);
	}

	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		params.add(null);
		statement.setNull(parameterIndex, sqlType, typeName);
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		params.add(null);
		statement.setNull(parameterIndex, sqlType);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		params.add(x);
		statement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		params.add(x);
		statement.setObject(parameterIndex, x, targetSqlType);
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		params.add(x);
		statement.setObject(parameterIndex, x);
	}

	public void setPoolable(boolean poolable) throws SQLException {
		statement.setPoolable(poolable);
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		statement.setQueryTimeout(seconds);
	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		params.add(x);
		statement.setRef(parameterIndex, x);
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		params.add(x);
		statement.setRowId(parameterIndex, x);
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		params.add(x);
		statement.setShort(parameterIndex, x);
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		unimplemented();
		statement.setSQLXML(parameterIndex, xmlObject);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		params.add(x);
		statement.setString(parameterIndex, x);
	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		unimplemented();
		statement.setTime(parameterIndex, x, cal);
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		params.add(x);
		statement.setTime(parameterIndex, x);
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		unimplemented();
		statement.setTimestamp(parameterIndex, x, cal);
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		params.add(x);
		statement.setTimestamp(parameterIndex, x);
	}

	@Deprecated	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		statement.setUnicodeStream(parameterIndex, x, length);
	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		params.add(x);
		statement.setURL(parameterIndex, x);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return statement.unwrap(iface);
	}

	public String replaceBinds(String sql) {
		for (Object o : params) {
			String s;
			if (o instanceof java.sql.Timestamp) {
				Timestamp time = (Timestamp) o;
				s = Dates.yyyyMmDdHhMmSs(time);
			} else {
				s = String.valueOf(o);
			}
			String quoted = quote(s);
			quoted = quoted.replaceAll("\\$", "\\\\\\$");
			sql = sql.replaceFirst("\\?", quoted);
		}
		return sql;
	}


}
