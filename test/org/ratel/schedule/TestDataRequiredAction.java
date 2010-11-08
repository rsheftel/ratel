package org.ratel.schedule;

import static org.ratel.mail.EmailAddress.*;
import static org.ratel.schedule.JobTable.*;
import static org.ratel.schedule.Schedulable.*;
import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.AttributeValues.*;
import static org.ratel.tsdb.DataSource.*;
import static org.ratel.util.Dates.*;
import static org.ratel.util.Objects.*;
import org.ratel.mail.MockEmailer.*;
import org.ratel.schedule.JobTable.*;
import org.ratel.schedule.dependency.*;

public class TestDataRequiredAction extends AbstractJobTest {


    private Job test;

    @Override public void setUp() throws Exception {
        super.setUp();
        JOBS.setAllSuccess("2008/02/15");
        test = JOBS.insert("test group", RUN_COMMAND, THREE_PM);
        makeJobDoNothing(test);
        AllDataReady.create(test, TEST_SOURCE, AAPL);
    }

    private void makeJobDoNothing(Job job) {
        job.setParameters(map("command", "echo echo echo"));
    }
    
    public void testShouldNotRecheckIfRerun() throws Exception {
        AAPL_TEST.write("2008/02/15", 17.0);
        freezeNow("2008/02/15 14:00:00");
        new Scheduler().run();
        AAPL_TEST.purge();
        new Scheduler().run(); // an email here would mean the recheck ran, error
    }
    
    public void testShouldOnlySendOneEmailPerGroup() throws Exception {
        Job second = JOBS.insert("test group 2", RUN_COMMAND, THREE_PM);
        makeJobDoNothing(second);
        AllDataReady.create(second, TEST_SOURCE, AAPL, AAPL_HIGH);
        freezeNow("2008/02/15 15:00:00");
        emailer.allowMessages();
        new Scheduler().run();
        emailer.requireSent(2);
    }
    
    public void testSendsEmailWhenDeadlinePassesWithoutData() throws Exception {
        freezeNow("2008/02/15 14:59:59");
        new Scheduler().run();
        freezeNow("2008/02/15 15:00:00");
        emailer.allowMessages();
        new Scheduler().run();
        emailer.requireSent(1);
        Sent sent = emailer.sent();
        sent.hasContent("aapl close:test");
        sent.hasReceiver(NOBODY);
        emailer.disallowMessages();
        new Scheduler().run();
    }
    
    public void testCrossJoinDoesNotHappenInAllDataDeadline() throws Exception {
        test.delete();
        Job second = JOBS.insert("test group 2", RUN_COMMAND, THREE_PM);
        makeJobDoNothing(second);
        AllDataReady.create(second, TEST_SOURCE, values(
            TICKER.value("aapl"),
            QUOTE_TYPE.value("close", "open")
        ));
        freezeNow("2008/02/15 15:00:00");
        emailer.allowMessages();
        new Scheduler().run();
        assertMatches("aapl close:test", emailer.sent().message);
    }

    public void testDoesNotSendEmailWhenDeadlinePassesWithData() throws Exception {
        freezeNow("2008/02/15 14:59:58");
        new Scheduler().run();
        AAPL_TEST.write("2008/02/15", 17.0);
        freezeNow("2008/02/15 14:59:59");
        new Scheduler().run();
        freezeNow("2008/02/15 15:00:00");
        new Scheduler().run();
        
    }
}
