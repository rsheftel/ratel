package com.fftw.fix.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import quickfix.Message;

import com.fftw.bloomberg.aggregator.AggregatorApplication;

public class TestMessageAction implements ActionListener
{

    private Component parent = null;

//    private Cursor prevParentCursor;

    public TestMessageAction (Component parent)
    {
        this.parent = parent;
    }

    public void actionPerformed (ActionEvent event)
    {
        try
        {
            Message message = FixLogViewerFrame.getInstance().getSelectedRow();

            AggregatorApplication app = new AggregatorApplication();
            app.crack(message, null);
            
            if (parent != null) {
                System.out.println("This is form PMD");
            } else {
                System.out.println("This is form PMD");
            }

            // get selected item and run through the logic
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
