package systemdb.metadata;

import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;

import java.sql.*;
import java.util.*;
import java.util.Date;

import db.*;
import db.clause.*;
import db.tables.SystemDB.*;

public class ParameterValuesTable extends ParameterValuesBase {
    private static final long serialVersionUID = 1L;
    public static final ParameterValuesTable VALUES = new ParameterValuesTable();
	public ParameterValuesTable() { super("paramValues"); }
	public ParameterValuesTable(String alias) { super(alias); }

	private List<Row> rows(String system, Pv pv) {
        Clause matches = matches(system, pv);
        return rows(matches.and(C_ASOFDATE.is(new ParameterValuesTable("maxDate").maxDateSelect(this))));
	}
	
	public Map<String, String> params(String system, Pv pv) {
	    Map<String, String> result = emptyMap();
	    List<Row> rows = rows(system, pv);
	    for (Row row : rows)
            result.put(row.value(C_PARAMETERNAME), row.value(C_PARAMETERVALUE));
        return result;
	}

	private SelectOne<Timestamp> maxDateSelect(ParameterValuesTable outer) {
	    return C_ASOFDATE.max().select(
			C_SYSTEM.is(outer.C_SYSTEM)
			.and(C_NAME.is(outer.C_NAME))
			.and(C_STRATEGY.is(outer.C_STRATEGY))
			.and(C_PARAMETERNAME.is(outer.C_PARAMETERNAME))
			.and(C_ASOFDATE.notInFuture())
		);
	}

	private Clause matches(String system, Pv pv) {
		return C_SYSTEM.is(system).and(pv.matches(C_NAME));
	}
	
	public void insert(String system, String pvName, String paramName, String paramValue) {
	    insert(system, pvName, paramName, paramValue, now());
	}
	
    public void insert(String system, String pvName, String paramName, String paramValue, Date asOf) {
        StrategyBase S = StrategyBase.T_STRATEGY;
        if(!S.rowExists(S.C_NAME.is(system)))
            S.insert(S.C_NAME.with(system));
        StrategyParameterNamesBase N = StrategyParameterNamesBase.T_STRATEGYPARAMETERNAMES;
        if(!N.rowExists(N.C_STRATEGY.is(system).and(N.C_PARAMETERNAME.is(paramName))))
            N.insert(N.C_STRATEGY.with(system), N.C_PARAMETERNAME.with(paramName));
        insert(
            C_SYSTEM.with(system),
            C_STRATEGY.with(system),
            C_ASOFDATE.with(asOf),
            C_NAME.with(pvName),
            C_PARAMETERNAME.with(paramName),
            C_PARAMETERVALUE.with(paramValue)
        );
    }
    
    public String param(String system, Pv pv, String parameterName) {
        Map<String, String> params = params(system, pv);
        return bombNull(params.get(parameterName), "could not find " + parameterName + " in " + params);
    }
    
    public String param(String system, String pv, String parameterName) {
        return param(system, new Pv(pv), parameterName);
    }
}