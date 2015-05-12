package malbec.fer;

import malbec.fer.mapping.DatabaseMapper;

public class FerRouterTestHelper {
    
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
}
