package com.malbec.tsdb.markit;

import static util.Errors.*;
import static util.Strings.*;

import java.util.*;

import db.*;

public class ConvertColumn<NEW, ORIG> extends SyntheticColumn<NEW> {
    private static final long serialVersionUID = 1L;

	private final String newType;
	private final Column<ORIG> original;

	public ConvertColumn(String newType, Column<ORIG> original, String alias) {
		super(alias, newType.replaceFirst("\\(.*", ""), Table.NOT_NULL, original.identity() + "_" + alias);
		this.newType = newType;
		this.original = original;
	}

	
	@Override public String asSql() {
		return "convert" + paren(newType + ", " + original.asSql());
	}

	@Override public String asSelect() {
		return asSql() + " as " + name();
	}


	@Override public void collectTables(Set<Table> tables) {
		original.collectTables(tables);
	}
	
	@Override public String string(NEW t) {
	    throw bomb("don't know how to string " + t);
	}

}
