package com.fftw.bloomberg.aggregator;

import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionStateListener;

import com.fftw.util.Emailer;

public class AggregatorSessionStateListener implements SessionStateListener
{

    private SessionID sessionId;

    private Emailer mailer;

    private boolean connected = false;
    
    private boolean threadChecking = false;
    

    public AggregatorSessionStateListener (SessionID sessionID, Emailer mailer)
    {
        this.sessionId = sessionID;
        this.mailer = mailer;
    }

    @Override
    public void onConnect ()
    {
        synchronized (this)
        {
            connected = true;
        }
    }

    /**
     * This will check to ensure that we are not disconnected by error. If we
     * should be connected, wait ~20 seconds and send an email.
     * 
     * This is called in the same thread that is trying to re-connect, we need
     * to spin our own thread so that the re-connect can happen.
     * 
     */
    @Override
    public void onDisconnect ()
    {
        synchronized (this)
        {
            connected = false;
        }

        if (isThreadChecking()) {
            return;
        }
        setThreadChecking(true);
        
        
        Thread timerThread = new Thread(new Runnable()
        {
            public void run ()
            {

                Session mySession = Session.lookupSession(sessionId);

                int checkCount = 0;
                while (mySession.isSessionTime() && !isConnected())
                {
                    checkCount++;
                    // After ~5 minutes and
                    if (checkCount > 10)
                    {
                        StringBuilder sb = new StringBuilder(1000);
                        sb.append("Fix Session: ").append(sessionId.toString());
                        sb.append(" disconnected, attempts to re-connect failed.");
                        sb.append("\n\nStill trying.");

                        mailer.emailErrorMessage("Fix Session disconnected - unable to reconnect",
                            sb.toString(), true);
                        // reset the counter
                        checkCount = 0;
                    }
                    // If we should be connected, sleep for 30 seconds and check
                    // again
                    sleep(30000);
                }
                setThreadChecking(false);
            }
        }, "ConnectedThread");
        timerThread.setDaemon(true);
        timerThread.start();

    }

    private void sleep (long millsToSleep)
    {
        try
        {
            Thread.sleep(millsToSleep);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private synchronized boolean isConnected ()
    {
        return connected;
    }
    
    private synchronized boolean isThreadChecking ()
    {
        return threadChecking;
    }
    
    private synchronized void setThreadChecking (boolean b)
    {
        threadChecking = b;
    }
    
    @Override
    public void onHeartBeatTimeout ()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLogon ()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLogout ()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMissedHeartBeat ()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRefresh ()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReset ()
    {
        // TODO Auto-generated method stub

    }

}
