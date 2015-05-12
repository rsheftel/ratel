package systemdb.metadata;

import static systemdb.metadata.MsivTable.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import sto.*;
import systemdb.data.*;
import systemdb.metadata.MsivTable.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.SystemDB.*;

public class SystemDetailsTable extends SystemDetailsBase {
    private static final long serialVersionUID = 1L;
    public static final SystemDetailsTable DETAILS = new SystemDetailsTable();
	private Map<Integer, SystemDetails> cache = emptyMap();
    
    public SystemDetailsTable() {
        super("sys_details");
    }

    public int insert(Siv siv, Pv pv, String stoDir, String stoId, boolean runInNativeCurrency) {
        insert(
            siv.systemCell(C_SYSTEM_NAME), 
            pv.cell(C_PV_NAME),
            siv.versionCell(C_VERSION),
            siv.intervalCell(C_INTERVAL), 
            C_STO_DIR.with(stoDir), 
            C_STO_ID.with(stoId),
            C_RUN_IN_NATIVE_CURRENCY.with(runInNativeCurrency)
        );
        return Db.identity();
    }

    //"Q.Systems.ExampleSymbolSystem", "Test", "1", "daily", "asdf", Env.svn(@"dotNET\Q"), "testSTO"
    public int insert(String system, String version, String interval, String pvName, String stoDir, String stoId) {
        return insert(new Siv(system, interval, version), new Pv(pvName), stoDir, stoId, false);
    }
    
    public class SystemDetails extends Row {
        private static final long serialVersionUID = 1L;
        public SystemDetails(Row r) {
            super(r);
        }
        
        public String qClass() { 
            return siv().qClass();
        }

        public Siv siv() {
            return new Siv(value(C_SYSTEM_NAME), value(C_INTERVAL), value(C_VERSION));
        }

        public String stoDir() {
            return value(C_STO_DIR);
        }

        public String stoId() {
            return value(C_STO_ID);
        }

        public int id() {
            return value(C_ID);
        }
        
        public LiveSystem liveSystem() {
            return new LiveSystem(siv(), pv());
        }

        Pv pv() {
            return new Pv(pvName());
        }

        private String pvName() {
            return value(C_PV_NAME);
        }

        public MsivRow msiv(String market) {
            return MSIVS.msiv(market, liveSystem().siv());
        }

        public Clause matches(NvarcharColumn stoDirCol, NvarcharColumn stoIdCol) {
            return stoDirCol.is(stoDir()).and(stoIdCol.is(stoId()));
        }
        
        public int lastRunNumber() {
        	return new STO(this).lastRunNumber();
        }
        
        public Interval interval() {
            return Interval.lookup(value(C_INTERVAL));
        }
        
        public boolean hasPv() { 
            String pvName = pvName();
            return hasContent(pvName)  && !pvName.equals("NA");
        }
        
        public boolean hasValidStoDir() { 
            try { new STO(this); return true; } 
            catch (Exception e) { return false; }
        }
        
        public boolean runInNativeCurrency() {
            Boolean value = value(C_RUN_IN_NATIVE_CURRENCY);
            return value == null ? false : value;
        }
        
    }
    
    public static LiveSystem liveSystem(int systemId) {
        return DETAILS.details(systemId).liveSystem();
    }
    
    public static List<Integer> allAvailableStoIds() {
        Clause hasContentSto = DETAILS.C_STO_ID.hasContent().and(DETAILS.C_STO_DIR.hasContent());
        return DETAILS.C_ID.distinct(
            hasContentSto.and(DETAILS.C_PV_NAME.is("NA"))
        );
    }
    
    public static Siv siv(int systemId) {
        return DETAILS.details(systemId).siv();
    }
    
    public SystemDetails details(int systemId) {
    	if(!cache .containsKey(systemId)) cache.put(systemId, new SystemDetails(row(idMatches(systemId))));
    	return cache.get(systemId);
    }

    private Clause idMatches(int systemId) {
        return C_ID.is(systemId);
    }

    public boolean liveExists(Siv siv, Pv pv) {
        return liveMatches(siv, pv).exists();
    }

    private Clause matches(Siv siv, Pv pv) {
        return sivMatches(siv).and(pvMatches(pv));
    }

    public Clause pvMatches(Pv pv) {
        return pv.matches(C_PV_NAME);
    }

    public Clause sivMatches(Siv siv) {
        return siv.matches(C_SYSTEM_NAME, C_INTERVAL, C_VERSION);
    }

    public int insert(Siv siv, Pv pv, boolean runInNativeCurrency) {
        return insert(siv, pv, "NA", "NA", runInNativeCurrency);
    }

    public SystemDetails details(Clause detailMatches) {
        return details(C_ID.value(detailMatches));
    }

    public SystemDetails liveDetails(Siv siv, Pv pv) {
        return details(liveMatches(siv, pv));
    }

    private Clause liveMatches(Siv siv, Pv pv) {
        return matches(siv, pv).and(C_STO_DIR.is("NA").and(C_STO_ID.is("NA")));
    }

    public boolean stoExists(Siv siv, String stoId) {
        return sivStoMatch(siv, stoId).exists();
    }
    
    public boolean isValid(int id) {
        return rowExists(idMatches(id));
    }

    private Clause sivStoMatch(Siv siv, String stoId) {
        return sivMatches(siv).and(C_STO_ID.is(stoId));
    }

    public int id(Siv siv, String stoId) {
        return C_ID.value(sivStoMatch(siv, stoId));
    }

    public SystemDetails details(String stoDir, String stoId) {
        return details(C_STO_DIR.is(stoDir).and(C_STO_ID.is(stoId)).and(C_PV_NAME.is("NA")));
    }

    public void delete(Siv testSiv, Pv testPv) {
        deleteAll(liveMatches(testSiv, testPv));
    }


}
