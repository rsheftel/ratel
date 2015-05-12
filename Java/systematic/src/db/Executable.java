package db;

import java.sql.*;

interface Executable {

	void execute();
	String toSql();
	int bindInto(int n, LoggingPreparedStatement st) throws SQLException;
}