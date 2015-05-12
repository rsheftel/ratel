package systemdb.live;

import java.util.*;

import systemdb.metadata.*;
import db.*;
import db.columns.*;
import db.tables.SystemDB.*;
import static systemdb.metadata.MsivTable.*;

public class Exceptions extends CheckAllSystemsQExceptionsBase {
    private static final long serialVersionUID = 1L;
    public static final Exceptions EXCEPTIONS = new Exceptions(); 
        
    public Exceptions() {
        super("exceptions");
    }

    public void removeAllowedMissingFromLiveHistory(List<MsivPv> extraQ) {
        removeExceptions(extraQ, C_ALLOWMISSINGFROMMSIVLIVEHISTORY);
    }

    public void removeAllowedMissingFromAllSystemsQ(List<MsivPv> extraLiveHistory) {
        removeExceptions(extraLiveHistory, C_ALLOWMISSINGFROMALLSYSTEMSQ);
    }

    private void removeExceptions(List<MsivPv> extra, BitColumn column) {
        List<Row> toRemove = rows(column.is(true));
        for (Row row : toRemove) {
            MsivPv msivPv = new MsivPv(MSIVS.forName(row.value(C_MSIV_NAME)), new Pv(row.value(C_PV_NAME)));
            extra.remove(msivPv);
        }
    }

    public void insert(MsivPv msivPv, boolean allowMissingAllSystemsQ, boolean allowMissingMsivLiveHistory) {
        EXCEPTIONS.insert(
            msivPv.msivCell(C_MSIV_NAME),
            msivPv.pvCell(C_PV_NAME),
            C_ALLOWMISSINGFROMALLSYSTEMSQ.with(allowMissingAllSystemsQ),
            C_ALLOWMISSINGFROMMSIVLIVEHISTORY.with(allowMissingMsivLiveHistory)
        );
    }

}
