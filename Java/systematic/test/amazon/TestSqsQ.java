package amazon;

import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Systematic.*;
import util.*;
import db.*;

public class TestSqsQ extends DbTestCase {
    private SqsQ q = null;
    private static int counter = 0;
    
    @Override protected void setUp() throws Exception {
        super.setUp();
        q = new SqsQ(hostname() + "-imaq-" + yyyyMmDdHhMmSsNoSeparator(now()) + "-" + (counter++));
    }

    public void testQ() throws Exception {
        try { 
            q.message(); // no messages to get
            fail();
        } catch (Exception e) {
            assertMatches("no messages to receive", e);
        }
        q.send("imamessage");
        assertEquals("imamessage", q.message().object());
    }
    
    public void testBlockingReceive() throws Exception {
        new Thread() {
            @Override public void run() {
                Times.sleep(1500);
                q.send("another message");
            }
        }.start();
        assertEquals("another message", the(q.messagesBlocking()).object());
    }
    
    public void slowTestDeleteQ() throws Exception {
        new SqsQ("quantys-272126").drain();
    }
    
    public void slowTestQueueSize() throws Exception {
        String qName = "179024-20090413134158";
        info("request: " + new SqsQ(qName).size());
        info("response: " + new SqsQ("response-" + qName).size());
//        String qName2 = "62391-20090125020905";
//        info("request: " + new SqsQ(qName2).size());
//        info("response: " + new SqsQ("response-" + qName2).size());
    }
    
    public void slowTestListQueues() throws Exception {
        info(SqsQ.listQueues("356297") + "");
    }
}
