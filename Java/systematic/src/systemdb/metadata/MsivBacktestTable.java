package systemdb.metadata;

import static systemdb.metadata.MsivTable.*;
import static systemdb.metadata.SystemDetailsTable.*;

import java.util.*;

import systemdb.metadata.MsivTable.*;
import systemdb.metadata.SystemDetailsTable.*;
import util.*;
import db.*;
import db.clause.*;
import db.tables.SystemDB.*;

public class MsivBacktestTable extends MSIVBacktestBase {
    private static final long serialVersionUID = 1L;
    public static MsivBacktestTable BACKTEST = new MsivBacktestTable();
    
    public MsivBacktestTable() {
        super("backtest");
    }

    public void insert(String msiv, String stoId, Date start, Date end) {
        insert(
            C_STOID.with(stoId), 
            C_MSIV_NAME.with(msiv), 
            C_STARTDATE.withMaybe(start), 
            C_ENDDATE.withMaybe(end)
        );
    }

    public void insert(String msiv, String stoId) {
        insert(msiv, stoId, null, null);
    }

    public List<String> stoIds(Siv siv) {
        return C_STOID.distinct(matches(siv));
    }

    private Clause matches(Siv siv) {
        return C_MSIV_NAME.is(MSIVS.C_NAME).and(MSIVS.matches(siv));
    }

    public List<String> markets(Siv siv, String stoId) {
        return MSIVS.C_MARKET.distinct(C_STOID.is(stoId).and(matches(siv)));
    }

    public Range range(int systemId, String market) {
        SystemDetails system = DETAILS.details(systemId);
        MsivRow msiv = system.msiv(market);
        Row backtest = row(msiv.matches(C_MSIV_NAME).and(C_STOID.is(system.stoId())));
        return new Range(backtest.value(C_STARTDATE), backtest.value(C_ENDDATE));
    }

}
