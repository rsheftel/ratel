package com.fftw.bloomberg.cmfp;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.mina.common.TransportType;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.Initiator;
import quickfix.mina.NetworkingOptions;
import quickfix.mina.ProtocolFactory;

import com.fftw.bloomberg.aggregator.CmfSenderApplication;
import com.fftw.bloomberg.cmfp.mina.CmfSessionConnector;
import com.fftw.bloomberg.cmfp.mina.initiator.IoSessionInitiator;

public class CmfSocketInitiator extends CmfSessionConnector
{

    private boolean isInitialized;

    public CmfSocketInitiator (CmfApplication application, CmfSessionSettings cmfSettings)
    {
        super(application, cmfSettings);

        try
        {
            createSessions();
        }
        catch (ConfigError e)
        {
            e.printStackTrace();
        }
        catch (FieldConvertError e)
        {
            e.printStackTrace();
        }
    }

    private void createSessions () throws ConfigError, FieldConvertError
    {
        Map<CmfSessionID, CmfSession> initiatorSessions = new HashMap<CmfSessionID, CmfSession>();
        for (Iterator<CmfSessionID> i = getCmfSettings().cmfSectionIterator(); i.hasNext();)
        {
            CmfSessionID sessionID = i.next();
            if (isInitiatorSession(sessionID))
            {
                try
                {
                    CmfSession cmfSession = createSession(sessionID);
                    initiatorSessions.put(sessionID, cmfSession);
                }
                catch (Throwable e)
                {
                    throw e instanceof ConfigError ? (ConfigError)e : new ConfigError(
                        "error during session initialization", e);
                }
            }
        }
        if (initiatorSessions.isEmpty())
        {
            throw new ConfigError("no initiators in settings");
        }
        setSessions(initiatorSessions);
    }

    public void stop ()
    {
        stop(false);
    }

    public void stop (boolean forceDisconnect)
    {
        stopSessionTimer();
        CmfSession.unregisterSessions(getCmfSessions().keySet());
    }

    public void stopSessionTimer ()
    {
        stopInitiators();
        super.stopSessionTimer();
    }

    private void stopInitiators ()
    {
        Iterator<IoSessionInitiator> i = initiators.iterator();
        while (i.hasNext())
        {
            i.next().stop();
        }
    }

    public void start () throws ConfigError
    {
        initialize();
    }

    private void startTimers ()
    {
        startSessionTimer();
        startInitiators();
    }

    private void startInitiators ()
    {
        Iterator<IoSessionInitiator> i = initiators.iterator();
        while (i.hasNext())
        {
            i.next().start();
        }
    }

    private int getReconnectIntervalInSeconds (CmfSessionID sessionID) throws ConfigError
    {
        int reconnectInterval = 30;
        CmfSessionSettings settings = getCmfSettings();
        if (settings.isSetting(sessionID, Initiator.SETTING_RECONNECT_INTERVAL))
        {
            try
            {
                reconnectInterval = (int)settings.getLong(sessionID,
                    Initiator.SETTING_RECONNECT_INTERVAL);
            }
            catch (FieldConvertError e)
            {
                throw new ConfigError(e);
            }
        }
        return reconnectInterval;
    }

    private SocketAddress[] getSocketAddresses (CmfSessionID sessionID) throws ConfigError
    {
        CmfSessionSettings settings = getCmfSettings();
        ArrayList<SocketAddress> addresses = new ArrayList<SocketAddress>();
        for (int index = 0;; index++)
        {
            try
            {
                String protocolKey = Initiator.SETTING_SOCKET_CONNECT_PROTOCOL
                    + (index == 0 ? "" : Integer.toString(index));
                String hostKey = Initiator.SETTING_SOCKET_CONNECT_HOST
                    + (index == 0 ? "" : Integer.toString(index));
                String portKey = Initiator.SETTING_SOCKET_CONNECT_PORT
                    + (index == 0 ? "" : Integer.toString(index));
                TransportType transportType = TransportType.SOCKET;
                if (settings.isSetting(sessionID, protocolKey))
                {
                    try
                    {
                        transportType = TransportType.getInstance(settings.getString(sessionID,
                            protocolKey));
                    }
                    catch (IllegalArgumentException e)
                    {
                        // Unknown transport type
                        throw new ConfigError(e);
                    }
                }
                if (settings.isSetting(sessionID, portKey))
                {
                    String host;
                    host = settings.getString(sessionID, hostKey);

                    int port = (int)settings.getLong(sessionID, portKey);
                    addresses.add(ProtocolFactory.createSocketAddress(transportType, host, port));
                }
                else
                {
                    break;
                }
            }
            catch (FieldConvertError e)
            {
                throw (ConfigError)new ConfigError(e.getMessage()).initCause(e);
            }
        }

        return addresses.toArray(new SocketAddress[addresses.size()]);
    }

    private synchronized void initialize () throws ConfigError
    {
        if (!isInitialized)
        {
            try
            {
                Iterator<CmfSession> sessionItr = getCmfSessions().values().iterator();

                while (sessionItr.hasNext())
                {
                    CmfSession cmfSession = sessionItr.next();

                    CmfSessionID cmfSessionID = cmfSession.getCmfSessionID();
                    ((CmfSenderApplication)getApplication()).setPomsSessionID(cmfSessionID);
                    
                    int reconnectingInterval = getReconnectIntervalInSeconds(cmfSessionID);

                    SocketAddress[] socketAddresses = getSocketAddresses(cmfSession
                        .getCmfSessionID());
                    if (socketAddresses.length == 0)
                    {
                        throw new ConfigError("Must specify at least one socket address");
                    }

                    NetworkingOptions networkingOptions = new NetworkingOptions(getCmfSettings()
                        .getSessionProperties(cmfSessionID));

                    IoSessionInitiator ioSessionInitiator = new IoSessionInitiator(cmfSession,
                        socketAddresses, reconnectingInterval, getScheduledExecutorService(),
                        networkingOptions, getIoFilterChainBuilder());

                    initiators.add(ioSessionInitiator);
                }
                startTimers();
            }
            catch (FieldConvertError e)
            {
                throw new ConfigError(e);
            }
        }
        isInitialized = true;
    }

}
