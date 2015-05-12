package malbec.fix.util;

import static malbec.fix.util.Slf4jLog.MSG_SEQ_NUM;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import org.testng.annotations.Test;

import quickfix.Log;
import quickfix.SessionID;
import quickfix.SessionSettings;

public class Slf4jLogTest {

    @Test(groups = { "unittest" })
    public void testOnEvent() {
        Properties config = new Properties();
        
        config.setProperty("BeginString", "FIX.4.2");
        config.setProperty("TargetCompID", "LogTarget");
        config.setProperty("SenderCompID", "LogSender");
        
        SessionID sessionId = QfjHelper.createSessionId(config);
        Log log = new Slf4jLogFactory(new SessionSettings()).create(sessionId);
        assertTrue(log instanceof Slf4jLog, "Failed to create the correct type of log");
        Slf4jLog slf4jLog = (Slf4jLog)log;
        
        slf4jLog.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
               assertEquals(MSG_SEQ_NUM, evt.getPropertyName(), "Failed to receive sequence number event");
               System.out.println("Reveived proper event: " + evt.getPropertyName());
               System.out.println("Event text: "+ evt.getNewValue());
            }
            
        });
        
        // The assert is in the anonymous class
        log.onEvent("FIX.4.2:MALBECRPT->REDIRPT: MsgSeqNum too high, expecting 2621 but received 2802");
        log.onEvent("MsgSeqNum too high, expecting 2621 but received 2802");
        
    }
}
