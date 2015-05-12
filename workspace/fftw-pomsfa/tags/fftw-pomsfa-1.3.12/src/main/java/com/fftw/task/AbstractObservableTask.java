package com.fftw.task;

import java.util.Observable;

public abstract class AbstractObservableTask extends Observable implements Task
{

    private boolean cancelled;

    private boolean done;

    private boolean cancelRequested;

    private int progress; // Progress as a percent

    public boolean isCancelled ()
    {
        return cancelled;
    }

    public boolean isDone ()
    {
        return done;
    }

    public void requestCancel ()
    {
        cancelRequested = true;
    }

    public boolean isCancelRequested ()
    {
        return cancelRequested;
    }

    protected void setCancelled (boolean cancel)
    {
        cancelled = cancel;
    }

    protected void setDone (boolean done)
    {
        this.done = done;
    }

    public int getProgress ()
    {
        return progress;
    }

    protected void setProgress (int p)
    {
        progress = p;
    }

}
