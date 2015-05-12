package systemdb.metadata;

import db.tables.SystemDB.*;

public class SystemTable extends SystemBase {
    private static final long serialVersionUID = 1L;
    public static final SystemTable SYSTEM = new SystemTable();
    
    public SystemTable() {
        super("systems");
    }

    public void insert(String system, String qClass) {
        insert(C_NAME.with(system), C_QCLASSNAME.with(qClass));
    }

    public String qClass(String system) {
        return C_QCLASSNAME.value(C_NAME.is(system));
    }
}
