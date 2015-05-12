package com.fftw.bloomberg.cmfp.mina;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.mina.common.IoFilterChainBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.SessionFactory;

import com.fftw.bloomberg.cmfp.CmfApplication;
import com.fftw.bloomberg.cmfp.CmfSession;
import com.fftw.bloomberg.cmfp.CmfSessionID;
import com.fftw.bloomberg.cmfp.CmfSessionSettings;
import com.fftw.bloomberg.cmfp.mina.initiator.IoSessionInitiator;

/**
 * Manage a session connection.
 * 
 * 
 */
public class CmfSessionConnector
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public final static String CMF_SESSION = "CMF_SESSION";

    private CmfSessionSettings cmfSettings;

    private CmfApplication cmfApplication;

    protected final static ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor();

    private IoFilterChainBuilder ioFilterChainBuilder;
    
    private Map<CmfSessionID, CmfSession> cmfSessions;
    
    private ScheduledFuture<?> sessionTimerFuture;
    
    protected final Set<IoSessionInitiator> initiators = new HashSet<IoSessionInitiator>();

    // private CmfSessionFactory cmfFactory;

    public CmfSessionConnector (CmfApplication application, CmfSessionSettings settings)
    {
        this.cmfApplication = application;
        this.cmfSettings = settings;
    }

    protected CmfApplication getApplication()
    {
        return cmfApplication;
    }
    
    protected CmfSession createSession (CmfSessionID sessionID) throws ConfigError,
        FieldConvertError
    {
        CmfSessionSettings cmfSettings = getCmfSettings();
        String pricingNumer = cmfSettings.getString(sessionID, "PricingNumber");
        String specVersion = cmfSettings.getString(sessionID, "SpecVersion");

        CmfSession cmfSession = new CmfSession(getCmfSettings(), sessionID, cmfApplication,
            pricingNumer, specVersion);

        return cmfSession;
    }

    protected boolean isInitiatorSession (Object sectionKey) throws ConfigError, FieldConvertError
    {
        CmfSessionSettings settings = getCmfSettings();
        return !cmfSettings.isSetting((CmfSessionID)sectionKey,
            SessionFactory.SETTING_CONNECTION_TYPE)
            || settings.getString((CmfSessionID)sectionKey, SessionFactory.SETTING_CONNECTION_TYPE)
                .equals("initiator");
    }

    protected ScheduledExecutorService getScheduledExecutorService ()
    {
        return scheduledExecutorService;
    }

    protected void setSessions (Map<CmfSessionID, CmfSession> sessions)
    {
        cmfSessions = sessions;
    }

    public CmfSessionSettings getCmfSettings ()
    {
        return cmfSettings;
    }

    protected Map<CmfSessionID, CmfSession> getCmfSessions ()
    {
        return Collections.unmodifiableMap(cmfSessions);
    }
    
    protected IoFilterChainBuilder getIoFilterChainBuilder() {
        return ioFilterChainBuilder;
    }
    
    protected void startSessionTimer() {
        sessionTimerFuture = scheduledExecutorService.scheduleAtFixedRate(new SessionTimerTask(),
                0, 1000L, TimeUnit.MILLISECONDS);
    }
    protected void stopSessionTimer() {
        if (sessionTimerFuture != null) {
            sessionTimerFuture.cancel(false);
        }
    }

    
    private class SessionTimerTask implements Runnable {
        public void run() {
            try {
                Iterator<CmfSession> sessionItr = getCmfSessions().values().iterator();
                while (sessionItr.hasNext()) {
                    sessionItr.next();
//                    CmfSession session = sessionItr.next();
                    // TODO this handles the heartbeats and other things
//                    try {
//                        session.next();
//                    } catch (IOException e) {
//                        logError(session.getCmfSessionID(), null, "Error in session timer processing",
//                                e);
//                    }
                }
            } catch (Throwable e) {
                log.error("Error during timer processing", e);
            }
        }
    }    
}
