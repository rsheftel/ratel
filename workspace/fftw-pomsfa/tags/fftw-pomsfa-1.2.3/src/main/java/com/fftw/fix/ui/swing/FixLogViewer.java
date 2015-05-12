package com.fftw.fix.ui.swing;

import java.awt.Dimension;

import javax.swing.JFrame;

public class FixLogViewer
{

    /**
     * @param args
     */
    public static void main (String[] args)
    {
        JFrame mainFrame = FixLogViewerFrame.getInstance();
        mainFrame.setTitle("QuickFix/J Log Viewer");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setMinimumSize(new Dimension(300,300));
        mainFrame.setVisible(true);

    }

}
