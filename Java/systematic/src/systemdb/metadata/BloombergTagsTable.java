package systemdb.metadata;

import static systemdb.metadata.SystemDetailsTable.*;
import static util.Errors.*;
import static util.Strings.*;
import systemdb.metadata.SystemDetailsTable.*;
import db.clause.*;
import db.tables.SystemDB.*;

public class BloombergTagsTable extends BloombergTagsBase {
    private static final long serialVersionUID = 1L;
    public static final BloombergTagsTable TAGS = new BloombergTagsTable();

    public BloombergTagsTable() {
        super("tags");
    }

    public String tag(int systemId) {
        return C_TAG.value(idMatches(systemId));
    }

    private Clause idMatches(int systemId) {
        return C_SYSTEMID.is(systemId);
    }

    public boolean autoExecuteTrades(int id) {
        return rowExists(idMatches(id)) && C_AUTOEXECUTETRADES.value(idMatches(id));
    }
    
    public boolean anyAutoExecute(String system) {
        return rowExists(C_SYSTEM.is(system).and(C_AUTOEXECUTETRADES.is(true)));
    }

    public void insertIfNeeded(int id, String tag, boolean autoExecute) {
        if(rowExists(idMatches(id)))
            bombUnless(rowExists(idMatches(id).and(C_TAG.is(tag)).and(C_AUTOEXECUTETRADES.is(autoExecute))),
                "row exists in " + name() + ", but does not match " + paren(commaSep(tag, autoExecute)));
        SystemDetails details = DETAILS.details(id);
        insert(
            C_SYSTEMID.with(id), 
            C_TAG.with(tag), 
            C_AUTOEXECUTETRADES.with(autoExecute),
            details.siv().systemCell(C_SYSTEM),
            details.siv().intervalCell(C_INTERVAL),
            details.siv().versionCell(C_VERSION),
            details.pv().cell(C_PV_NAME)
        );
    }

    public void setAutoExecuteTrades(int id, boolean autoExecute) {
        C_AUTOEXECUTETRADES.updateOne(idMatches(id), autoExecute);
    }

}
