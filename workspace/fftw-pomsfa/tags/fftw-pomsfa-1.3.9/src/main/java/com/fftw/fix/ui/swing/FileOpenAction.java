package com.fftw.fix.ui.swing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Message;

import com.fftw.fix.quickfixj.LogReader;

public class FileOpenAction implements ActionListener
{
    private final static Logger log = LoggerFactory.getLogger(FileOpenAction.class);

    private String lastDirectory = null;

    private Component parent = null;

    private Cursor prevParentCursor;

    public FileOpenAction (Component parent)
    {
        this.parent = parent;
    }

    public void actionPerformed (ActionEvent e)
    {

        System.out.println("actionPerfromed:" + e.getSource());

        prevParentCursor = parent.getCursor();
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Thread thread = new Thread(new SwingWorker<List<Message>, Object>()
        {
            @Override
            protected List<Message> doInBackground () throws Exception
            {
                List<Message> fileContents = null;
                JFileChooser fileChooser = new JFileChooser(lastDirectory);

                int returnVal = fileChooser.showOpenDialog(parent);

                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    File file = fileChooser.getSelectedFile();
                    lastDirectory = file.getPath();
                    LogReader lr = new LogReader();
                    if (parent instanceof Observer)
                    {
                        lr.addObserver((Observer)parent);

                    }

                    fileContents = lr.readFixLog(file);

                    // This is where a real application would open the file.
                    log.info("Opening: " + file.getName() + ".");
                }
                else
                {
                    log.info("Open command cancelled by user.");
                }

                return fileContents;
            }

            @Override
            protected void done ()
            {
                try
                {
                    FixLogViewerFrame.getInstance().setTableData(get());

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    parent.setCursor(prevParentCursor);
                }
            }
        }, "FileOpenThread");

        thread.start();

    }

}
