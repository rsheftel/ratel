package com.fftw.task;

public interface Task
{
    boolean isDone ();

    void requestCancel ();

    boolean isCancelRequested ();

    boolean isCancelled ();

    String getDescription ();

    int getProgress ();

}
