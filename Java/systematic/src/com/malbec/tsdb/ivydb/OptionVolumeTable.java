package com.malbec.tsdb.ivydb;

import static util.Objects.*;

import java.util.*;

import tsdb.*;
import db.*;
import db.clause.*;
import db.tables.IvyDB.*;

public class OptionVolumeTable extends OPTIONVOLUMEBase {
    private static final long serialVersionUID = 1L;
	public static final OptionVolumeTable VOLUMES = new OptionVolumeTable();
	
	public class OptionVolumeRow extends Row {
	    private static final long serialVersionUID = 1L;
		public OptionVolumeRow(Row r) {
			super(r);
		}

		public int id() {
			return value(C_SECURITYID);
		}

		public Double volume() {
			return Double.valueOf(value(C_VOLUME));
		}
		public Double openInterest() {
			return Double.valueOf(value(C_OPENINTEREST));
		}

		public OptionType optionType() {
			return OptionType.fromFlag(value(C_CALLPUT));
		}
	}
	
	public OptionVolumeTable() {
		super("vol");
	}

	public OptionVolumeRow row(int id, Date date, OptionType type) {
		Clause securityIdMatches = C_SECURITYID.is(id);
		Clause dateMatches = C_DATE.is(date);
		Clause typeMatches = type.is(C_CALLPUT);
		return new OptionVolumeRow(row(securityIdMatches.and(dateMatches).and(typeMatches)));
	}

	public List<OptionVolumeRow> rows(Date date) {
		List<Row> rows = rows(C_DATE.is(date).and(C_CALLPUT.hasContent()));
		List<OptionVolumeRow> result = empty();
		for (Row row : rows)
			result.add(new OptionVolumeRow(row));
		return result;
	}

}
