package db;

import static db.TestLocksTable.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Strings.*;
import mail.*;
import util.*;
import db.clause.*;
public abstract class DbTestCase extends Asserts {

	private long start;
	protected MockEmailer emailer;
	private boolean setUpCalled;
	
	@Override protected void setUp() throws Exception {
        thawNow();
	    setUpCalled = true;
        String name = getName();
        name = isEmpty(name) ? getClass().getName() : name;
        TEST_LOCK.acquireLock("test", name);
		start = System.currentTimeMillis();
		setContext(name);
		emailer = new MockEmailer();
		Db.beInNoCommitTestMode();
	}

	@Override protected void tearDown() throws Exception {
	    bombUnless(setUpCalled, "calling dbtestcase setup is required. and you might have missed it in tearDown too?");
		System.err.println("test " + getName() + " took " + (reallyNow().getTime() - start) + " millis.");
		Db.reallyRollback();
		emailer.reset();
		thawNow();
	}
	
	@Override public void runBare() throws Throwable {
	    try {
	        super.runBare();
	    } finally {
            releaseLock();
        }
	}

    public void releaseLock() {
        TEST_LOCK.releaseLock("test", false);
    }
	
	public void assertCount(int expected, Clause matches) {
		assertEquals(expected, matches.count());
	}
	
	public void assertExists(Clause matches) {
	    assertTrue("no rows match " + matches, matches.exists());
	}
	
	public void assertEmpty(Clause matches) {
	    assertFalse("existing rows match " + matches, matches.exists());
	}
	
	public void assertCommitted() {
		assertTrue(Db.explicitlyCommitted());
	}
	
	public void assertNotCommitted() {
		assertFalse(Db.explicitlyCommitted());
	}
}
