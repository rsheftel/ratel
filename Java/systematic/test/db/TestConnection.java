package db;
import static db.clause.Clause.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static util.Objects.*;
import static util.Times.*;

import java.util.*;

import tsdb.*;
import db.columns.*;

public class TestConnection extends DbTestCase {
	public void testCanConnectToDb() throws Exception {
		assertEquals("1", Db.string("select 1"));
	}
	
	public void testCanGetTableNameList() throws Exception {
		nonEmpty(Db.tableNames("TSDB"));
	}
	
	public void testRollback() throws Exception {
		TimeSeries series = createTestTimeSeries();
		Db.reallyRollback();
		try {
			series.id();
			fail();
		} catch (RuntimeException e) {
			assertMatches("time series does not exist", e);
		}
	}

	private TimeSeries createTestTimeSeries() {
		TimeSeries series = new TimeSeries("javaTestTs");
		series.create(values(
			TICKER.value("test-quantys"),
			QUOTE_TYPE.value("close")
		));
		series.id();
		return series;
	}
	
	public void testCommit() throws Exception {
		TimeSeries series = createTestTimeSeries();
		Db.commit();
		series.id();
		series.delete();
		Db.commit();
	}
	
	static class TestTable extends Table {
	    private static final long serialVersionUID = 1L;
	    public IntColumn C_A = new IntColumn("a", "int", this, NOT_NULL);
	    public IntColumn C_B = new IntColumn("b", "int", this, NOT_NULL);

        protected TestTable() {
            super("test_table");
        }
	    
	}
	
	public void functestRetryLogic() throws Exception {
	    Db.getOutOfNoCommitTestMode();
	    try {
    	    Db.execute("create table test_table (a int, b int)");
    	    Db.commit();
    	    Db.execute("insert into test_table values(1,2)");
    	    Db.setQueryTimeout(1);
    	    final List<Boolean> success = list(false);
    	    Thread select = new Thread() {
    	        @Override public void run() {
    	            new TestTable().rows();
    	            success.set(0, true);
    	        }
    	    };
            select.start();
            sleepSeconds(10);
            Db.commit();
            select.join();
            assertTrue(first(success));
            Db.execute("insert into test_table values(1,2)");
            Thread update = new Thread() {
                @Override public void run() {
                    TestTable t = new TestTable();
                    try {
                        t.C_A.updateOne(TRUE, 0);
                        success.set(0, false);
                    } catch (Exception e) {
                        if(!e.getCause().getMessage().matches(".*query has timed out.*"))
                            success.set(0, false);
                    } finally {
                        Db.rollback();
                        
                    }
                }
            };
            update.start();
            sleepSeconds(3);
            Db.commit();
            select.join();
            assertTrue(first(success));
	    } finally {
	        Db.execute("drop table test_table");
	    }
	    
    }
	
	@Override
	protected void tearDown() throws Exception {
		TimeSeries series = new TimeSeries("javaTestTs");
		if(series.exists())
			series.delete();
		Db.commit();
		super.tearDown();
	}
	
}
