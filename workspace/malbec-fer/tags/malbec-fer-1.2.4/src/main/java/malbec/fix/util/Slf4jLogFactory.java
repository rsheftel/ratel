package malbec.fix.util;

import quickfix.Log;
import quickfix.SLF4JLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

public class Slf4jLogFactory extends SLF4JLogFactory {

    private SessionSettings settings;

    public Slf4jLogFactory(SessionSettings settings) {
        super(settings);
        this.settings = settings;
    }

    @Override
    public Log create(SessionID sessionID, String callerFQCN) {
        String eventCategory = null;
        String incomingMsgCategory = null;
        String outgoingMsgCategory = null;
        boolean prependSessionID = true;
        boolean logHeartbeats = true;
        try {
            if (settings.isSetting(sessionID, SETTING_EVENT_CATEGORY)) {
                eventCategory = settings.getString(sessionID, SETTING_EVENT_CATEGORY);
            }
            if (settings.isSetting(sessionID, SETTING_INMSG_CATEGORY)) {
                incomingMsgCategory = settings.getString(sessionID, SETTING_INMSG_CATEGORY);
            }
            if (settings.isSetting(sessionID, SETTING_OUTMSG_CATEGORY)) {
                outgoingMsgCategory = settings.getString(sessionID, SETTING_OUTMSG_CATEGORY);
            }
            if (settings.isSetting(sessionID, SETTING_PREPEND_SESSION_ID)) {
                prependSessionID = settings.getBool(sessionID, SETTING_PREPEND_SESSION_ID);
            }
            if (settings.isSetting(sessionID, SETTING_LOG_HEARTBEATS)) {
                logHeartbeats = settings.getBool(sessionID, SETTING_LOG_HEARTBEATS);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Slf4jLog(sessionID, eventCategory, incomingMsgCategory, outgoingMsgCategory,
                prependSessionID, logHeartbeats, callerFQCN);
    }

}
