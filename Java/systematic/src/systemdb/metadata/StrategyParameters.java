package systemdb.metadata;

import java.util.*;

import db.clause.*;
import db.tables.SystemDB.*;

public class StrategyParameters extends StrategyParameterNamesBase {

    private static final long serialVersionUID = 1L;

    public static final StrategyParameters NAMES = new StrategyParameters();
    
    public StrategyParameters() {
        super("params");
    }

    public List<String> names(String system) {
        return C_PARAMETERNAME.values(matches(system));
    }

    private Clause matches(String system) {
        return C_STRATEGY.is(system);
    }
    
    public String sizingParameter(String system) {
        return C_PARAMETERNAME.value(matches(system).and(C_ISSIZING.is(true)));
    }
    
}
