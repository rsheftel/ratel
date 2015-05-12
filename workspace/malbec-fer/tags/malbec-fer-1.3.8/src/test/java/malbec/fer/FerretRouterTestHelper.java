package malbec.fer;

import org.joda.time.LocalTime;

import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.DateTimeUtil;
import malbec.util.EmailSettings;

public class FerretRouterTestHelper {
    
    public static final String EXECUTOR42 = "Executor42";
    public static final String EXECUTOR44 = "Executor44";
    
    public static DatabaseMapper createDatabaseMapper() {
        DatabaseMapper dbm = new DatabaseMapper(true);
        dbm.addAccountMapping("FixServer", "TEST.EQUITY", "Equity", "FIX-TEST");
        dbm.addAccountMapping("FixServer", "TESTE", "Equity", "FIX-TEST");
        dbm.addAccountMapping("FixServer", "TESTF", "Futures", "FIX-TEST");

        dbm.addAccountMapping("RediServer", "TESTF", "Futures", "5CX19915");
        dbm.addAccountMapping("RediServer", "TESTE", "Equity", "5CX19915");
        dbm.addAccountMapping("RediServer", "TEST.EQUITY", "Equity", "5CX19915");

        dbm.addAccountMapping(EXECUTOR44, "TEST.EQUITY", "Equity", "EXEC-TEST");
        dbm.addAccountMapping(EXECUTOR44, "TESTE", "Equity", "EXEC-TEST");
        dbm.addAccountMapping(EXECUTOR44, "TESTF", "Futures", "EXEC-TEST");

        dbm.addAccountMapping(EXECUTOR42, "TEST.EQUITY", "Equity", "EXEC-TEST");
        dbm.addAccountMapping(EXECUTOR42, "TESTE", "Equity", "EXEC-TEST");
        dbm.addAccountMapping(EXECUTOR42, "TESTF", "Futures", "EXEC-TEST");

        dbm.addAccountMapping("TEST", "TEST.EQUITY", "Equity", "EXEC-TEST");
        dbm.addAccountMapping("TEST", "TESTE", "Equity", "EXEC-TEST");
        dbm.addAccountMapping("TEST", "TESTF", "Futures", "EXEC-TEST");

        dbm.addAccountMapping("DATABASE", "TEST.EQUITY", "Equity", "DB-TEST");

        return dbm;
    }
    
    public static FerretRouter createFerRouterDma(IDatabaseMapper dbm) {
        LocalTime lt = new LocalTime(10,0,0,0);
        return createFerRouterDma(lt.toDateTimeToday().toString("yyyy/MM/dd HH:mm:ss"), dbm);
    }
    
    /**
     * Create a Ferret instance that is in DMA mode for today.
     * @param dbm
     * @return
     */
    public static FerretRouter createFerRouterDma(String freezeTime, IDatabaseMapper dbm) {
        DateTimeUtil.freezeTime(freezeTime);

        FerretRouter fr = new FerretRouter(new EmailSettings(), dbm);
        FerretSchedule fs = FerretScheduleTest.createTestSchedule();

        fs.setStateToTicket();
        fs.setStateToDma();
        fr.setSchedule(fs);

        return fr;
    }
    
    public static FerretRouter createFerRouter(String freezeTime, IDatabaseMapper dbm) {
        DateTimeUtil.freezeTime(freezeTime);

        FerretRouter fr = new FerretRouter(new EmailSettings(), dbm);
        FerretSchedule fs = FerretScheduleTest.createTestSchedule();

        fr.setSchedule(fs);

        return fr;
    }

}
