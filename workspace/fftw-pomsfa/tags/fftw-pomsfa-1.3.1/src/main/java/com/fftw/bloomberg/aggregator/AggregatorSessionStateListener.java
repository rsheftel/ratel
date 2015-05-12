package com.fftw.bloomberg.aggregator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionStateListener;

import com.fftw.util.Emailer;

public class AggregatorSessionStateListener implements SessionStateListener
{

    private final static Logger log = LoggerFactory.getLogger(AggregatorSessionStateListener.class);

    private SessionID sessionId;

    private Emailer mailer;

    private boolean connected = false;

    private boolean threadChecking = false;

    private DateTime disconnectTime;

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

        if (isThreadChecking())
        {
            return;
        }
        disconnectTime = new DateTime();

        setThreadChecking(true);

        final Session mySession = Session.lookupSession(sessionId);

        // If we were asked to logout, we don't need to start testing for
        // reconnect
        Thread timerThread = new Thread(new Runnable()
        {
            public void run ()
            {
                boolean emailSent = false;
                while (!mySession.isLogoutReceived() && mySession.isSessionTime() && !isConnected()
                    && !isWeekend(new DateTime()))
                {
                    long freeMem = Runtime.getRuntime().freeMemory();
                    long totalMem = Runtime.getRuntime().totalMemory();
                    if (!emailSent)
                    {
                        StringBuilder sb = new StringBuilder(1000);
                        sb.append(sessionId.toString());
                        sb.append(" disconnected at ").append(disconnectTime);
                        sb.append(", attempting to reconnect.");

                        sb.append("\nFree memory=").append(freeMem).append(" bytes.");
                        sb.append("\nTotal memory=").append(totalMem).append(" bytes.");
                        sb.append("\n\nSending email when reconnect occurs.");

                        mailer.emailErrorMessage(sessionId + " disconnected", sb.toString(), true);
                        emailSent = true;
                    }
                    // If we should be connected, sleep for 30 seconds and
                    // check again
                    StringBuilder sb = new StringBuilder(1000);
                    sb.append("Still waiting for connection to be re-established ");
                    sb.append(sessionId);
                    sb.append(" Free memory=").append(freeMem).append(" bytes.");
                    sb.append(" Total memory=").append(totalMem).append(" bytes.");
                    log.warn(sb.toString());
                    sleep(30000);
                    
                }
                // We are here because the session has ended/we connected/it
                // is the weekend!
                setThreadChecking(false);
                if (emailSent)
                {
                    StringBuilder sb = new StringBuilder(1000);
                    sb.append(sessionId.toString());
                    sb.append(" disconnected at ").append(disconnectTime);
                    sb.append("\n");

                    if (!mySession.isSessionTime())
                    {
                        sb.append("Disconnected session has ended.  ");
                        sb.append("We should reconnect when session starts");

                        mailer.emailErrorMessage(sessionId + "session has ended", sb.toString(),
                            true);
                    }
                    else if (isWeekend(new DateTime()))
                    {
                        sb.append("Disconnected session has entered weekend.  ");
                        sb.append("We should reconnect when the week starts");

                        mailer.emailErrorMessage(sessionId + " entered weekend", sb.toString(),
                            true);
                    }
                    else if (isConnected())
                    {
                        sb.append("Disconnected session has reconnected.");
                        mailer.emailErrorMessage(sessionId + " CONNECTED", sb.toString(), true);
                    }
                }
            }
        }, "ConnectedThread " + sessionId);
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
            // We don't care if we get interrupted
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

    private synchronized void setThreadChecking (boolean checking)
    {
        threadChecking = checking;
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

    /**
     * Check if the time is included in the weekend.
     * 
     * The weekend is: Friday at 21:00 to Monday at 05:00 exclusive.
     * 
     * @param timeToCheck
     * @return
     */
    public boolean isWeekend (DateTime timeToCheck)
    {
        // Set the start of the weekend to Friday at 9:00 PM
        DateTime.Property dp = timeToCheck.property(DateTimeFieldType.dayOfWeek());
        DateTime weekendStart = dp.setCopy("Friday").withHourOfDay(21);
        // Set the end of the weekend to Monday at 5:00 AM
        DateTime weekendEnd = weekendStart.plusDays(3).withHourOfDay(5);
        // Calculate the previous weekend to handle Monday mornings
        DateTime previousWeekendStart = weekendStart.minusDays(7);
        DateTime previousWeekendEnd = weekendEnd.minusDays(7);

        return ((timeToCheck.isAfter(weekendStart) && timeToCheck.isBefore(weekendEnd)) || (timeToCheck
            .isAfter(previousWeekendStart) && timeToCheck.isBefore(previousWeekendEnd)));

    }

}
