package systemdb.live;

import static util.Systematic.*;
import db.tables.SystemDB.*;

public class WatcherExclusionsTable extends WatcherExclusionsBase {

    public static WatcherExclusionsTable EXCLUSIONS = new WatcherExclusionsTable();
    
    private static final long serialVersionUID = 1L;

    public WatcherExclusionsTable() {
        super("wex");
    }

    public void insert(String tag) {
        if (isExcluded(tag)) return;
        insert(C_TAG.with(tag), C_WHO.with(username()), C_DATEEXCLUDED.now());
    }
    
    public boolean isExcluded(String tag) {
        return C_TAG.exists(tag);
    }
    
    public void remove(String tag) {
        deleteOne(C_TAG.is(tag));
    }
    
}
