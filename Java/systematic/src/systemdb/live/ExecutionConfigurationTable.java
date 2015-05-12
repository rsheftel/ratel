package systemdb.live;

import static util.Systematic.*;

import java.sql.*;

import db.*;
import db.clause.*;
import db.tables.SystemDB.*;

public class ExecutionConfigurationTable extends ExecutionConfigurationBase {

    private static final long serialVersionUID = 1L;

    public static ExecutionConfigurationTable CONFIG = new ExecutionConfigurationTable("executionconfig");
    
    public ExecutionConfigurationTable(String alias) { super(alias); }

    public static Configuration currentConfiguration(String type) {
        return CONFIG.config(type);
    }
    
    private SelectOne<Timestamp> maxDateSelect(String type) {
        return C_ASOF.max().select(C_TYPE.is(type).and(C_ASOF.notInFuture()));
    }

    private Configuration config(String type) {
        Clause matches = C_TYPE.is(type);
        return new Configuration(row(matches.and(C_ASOF.is(new ExecutionConfigurationTable("maxDate").maxDateSelect(type)))));
    }
    
    public void insert(String type, String platform, String route) {
        insert(C_ASOF.now(), C_PLATFORM.with(platform), C_ROUTE.with(route), C_SETBY.with(username()), C_TYPE.with(type));
    }

    public class Configuration extends Row {
        private static final long serialVersionUID = 1L;
        public Configuration(Row data) { super(data); }
        public String platform() { return value(C_PLATFORM); }
        public String route() { return value(C_ROUTE); }
        public String type() { return value(C_TYPE); }
    }
}
