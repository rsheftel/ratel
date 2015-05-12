package com.fftw.fix.ui.swing;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;

import quickfix.Message;

import com.fftw.task.AbstractObservableTask;

public class FixLogViewerFrame extends JFrame implements Observer
{

    private static FixLogViewerFrame instance = new FixLogViewerFrame();

    private ProgressMonitor pm;

    private JTable mainTable;

    private JScrollPane scrollPane = new JScrollPane();

    public static FixLogViewerFrame getInstance ()
    {
        return instance;
    }

    String[] fileItems = new String[]
    {
        "New", "Open", "Save", "Exit"
    };

    String[] editItems = new String[]
    {
        "Undo", "Cut", "Copy", "Paste", "Test"
    };

    char[] fileShortcuts =
    {
        'N', 'O', 'S', 'X'
    };

    char[] editShortcuts =
    {
        'Z', 'X', 'C', 'V', 'T'
    };

    private FixLogViewerFrame () throws HeadlessException
    {
        // This is a simple Frame to display the FIX logs from QuickFix/J
        buildMenu();
        getContentPane().add(scrollPane);
    }

    public void setTableData (List<Message> rawData)
    {

        if (mainTable != null)
        {
            scrollPane.getViewport().remove(mainTable);
        }
        mainTable = new JTable(new FixMessageTableModel(rawData));

        // getContentPane().remove(0);
        // getContentPane().add(mainTable);
        scrollPane.getViewport().add(mainTable);
        validate();
    }

    public Message getSelectedRow ()
    {
        FixMessageTableModel tableModel = (FixMessageTableModel)mainTable.getModel();
        int selectedRow = mainTable.getSelectedRow();
        if (selectedRow != -1)
        {
            return tableModel.getRowItem(selectedRow);
        }
        else
        {
            return null;
        }
    }

    private void buildMenu ()
    {
        JMenuBar menuBar = new JMenuBar();

        ActionListener printListener = new ActionListener()
        {
            public void actionPerformed (ActionEvent event)
            {
                System.out.println("Menu item [" + event.getActionCommand() + "] was pressed.");
            }
        };

        JMenu fileMenu = buildFileMenu(printListener, this);
        JMenu editMenu = buildEditMenu(printListener, this);

        JMenu otherMenu = new JMenu("Other");
        JMenu subMenu = new JMenu("SubMenu");
        JMenu subMenu2 = new JMenu("SubMenu2");

        // Assemble the submenus of the Other menu.
        JMenuItem item;
        subMenu2.add(item = new JMenuItem("Extra 2"));
        item.addActionListener(printListener);
        subMenu.add(item = new JMenuItem("Extra 1"));
        item.addActionListener(printListener);
        subMenu.add(subMenu2);

        // Assemble the Other menu itself.
        otherMenu.add(subMenu);
        otherMenu.add(item = new JCheckBoxMenuItem("Check Me"));
        item.addActionListener(printListener);
        otherMenu.addSeparator();
        ButtonGroup buttonGroup = new ButtonGroup();
        otherMenu.add(item = new JRadioButtonMenuItem("Radio 1"));
        item.addActionListener(printListener);
        buttonGroup.add(item);
        otherMenu.add(item = new JRadioButtonMenuItem("Radio 2"));
        item.addActionListener(printListener);
        buttonGroup.add(item);
        otherMenu.addSeparator();
        otherMenu.add(item = new JMenuItem("Potted Plant", new ImageIcon("image.gif")));
        item.addActionListener(printListener);

        // Finally, add all the menus to the menu bar.
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(otherMenu);
        setJMenuBar(menuBar);
    }

    private JMenu buildFileMenu (ActionListener defaultListener, Component parent)
    {
        JMenu fileMenu = new JMenu("File");

        // Assemble the File menus with mnemonics.
        for (int i = 0; i < fileItems.length; i++)
        {
            JMenuItem item = new JMenuItem(fileItems[i], fileShortcuts[i]);
            switch (fileShortcuts[i])
            {
                case 'O':
                    item.addActionListener(new FileOpenAction(parent));
                    break;
                default:
                    item.addActionListener(defaultListener);
                    break;
            }

            fileMenu.add(item);
        }

        return fileMenu;
    }

    private JMenu buildEditMenu (ActionListener defaultListener, Component parent)
    {
        JMenu editMenu = new JMenu("Edit");

        // Assemble the File menus with mnemonics.
        for (int i = 0; i < editItems.length; i++)
        {
            JMenuItem item = new JMenuItem(editItems[i], editShortcuts[i]);
            switch (editShortcuts[i])
            {
                case 'T':
                    item.addActionListener(new TestMessageAction(parent));
                    break;
                default:
                    item.addActionListener(defaultListener);
                    break;
            }

            editMenu.add(item);
        }
        // Insert a separator in the Edit menu in Position 1 after "Undo".
        editMenu.insertSeparator(1);

        return editMenu;
    }

    public void update (Observable o, Object arg)
    {
        if (o instanceof AbstractObservableTask)
        {
            update((AbstractObservableTask)o);
        }
    }

    private void update (AbstractObservableTask task)
    {
        if ((task.isDone() || task.isCancelled()) && pm != null)
        {
            // clean up the dialog
            pm.close();
            pm = null;

        }
        else
        {
            if (pm == null)
            {

                pm = new ProgressMonitor(this, task.getDescription(), "", 0, 100);
                pm.setMillisToPopup(500); // half second
            }

            String note = String.format("Completed %d%%.\n", task.getProgress());
            pm.setNote(note);
            pm.setProgress(task.getProgress());

            if (pm.isCanceled())
            {
                task.requestCancel();
            }
        }
    }
}