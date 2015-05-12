package db;

import java.sql.*;
import java.util.*;

public class SelectInto implements Executable {

	private final Select select;
	private final String name;

	public SelectInto(String name, Select select) {
		this.name = "#" + name;
		this.select = select;
	}

	@Override
	public int bindInto(int n, LoggingPreparedStatement st) throws SQLException {
		return select.bindInto(n, st);
	}

	@Override
	public void execute() {
		try { Db.execute("drop table " + name); }
		catch (Exception tableDidNotExistIgnored) { }
		Db.executeUnprepared(this, false);
	}

	@Override
	public String toSql() {
		return select.toSql("\n", new HashSet<Table>(), "\ninto " + name);
	}

	public Table table() {
		Table temp = new Table(name); // side effect registers table
		for (Column<?> c : select.columns()) {
			if (!c.isConcrete() || ((ConcreteColumn<?>)c).exists()) c.copyOnto(temp);
		}
		return temp;
	}

}
