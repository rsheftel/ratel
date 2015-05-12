package activemq.ui.actions;

import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.TaskService;

import activemq.ActiveMQMonitor;
import activemq.jmx.JmxMonitor;
import activemq.ui.JmsMessageTableModel;
import activemq.ui.TableUtils;
import activemq.ui.TopicTreeNode;
import activemq.ui.tasks.LoadTopicsTask;

/**
 * Contain the actions that can be applied to a Tree.
 * 
 * The <i>tree</i> in this case is the left tree that lists the ActiveMQ
 * topics.
 */
public class TreeActions {

    @Action
    public void monitorTopic() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        TopicTreeNode selectedTreeNode = application.getSelectedTreeNode();

        // TODO if this topic starts with 'ActiveMQ.Advisory' we need to create a different type of 
        // table listener
        JInternalFrame jif = new JInternalFrame(selectedTreeNode.getHierarchicalName(), true, true, true, true);
        jif.setPreferredSize(new Dimension(430, 180));
        JmsMessageTableModel newTable = new JmsMessageTableModel(selectedTreeNode.getHierarchicalName());
        application.getTopicMonitor().addObserver(newTable);
        application.getTopicMonitor().listenTo(selectedTreeNode.getHierarchicalName(), true);

        JTable recordTable = new JTable(newTable);
        recordTable.setDefaultRenderer(Object.class, new JmsMessageTableModel.ChangeRowTableCellRenderer());
        recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableUtils.setColumnPreferredWidths(recordTable, new int[] { 0, 1, 2 }, new int[] { 70, 170, 180 });

        JScrollPane scrollPane = new JScrollPane(recordTable);
        jif.getContentPane().add(scrollPane);
        application.getDesktop().add(jif);
        int componentCount = application.getDesktop().getComponents().length - 1;
        jif.setLocation(componentCount * 50, componentCount * 50);
        jif.pack();

        jif.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                application.getTopicMonitor().stopListeningTo(e.getInternalFrame().getTitle(), true);
            }
        });
        jif.setVisible(true);
    }
    
    @Action
    public void removeTopic() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        TopicTreeNode selectedTreeNode = application.getSelectedTreeNode();
        JmxMonitor monitor = application.getJmxMonitor();
        int userChoice = JOptionPane.showConfirmDialog(application.getMainFrame(), "Are you sure you want to remove topic '"
                + selectedTreeNode.getHierarchicalName()+"'", "Confirm Topic Removal",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if (userChoice == JOptionPane.YES_OPTION) {
            monitor.removeTopic(selectedTreeNode.getHierarchicalName());
            // remove from the tree itself
            application.getTreeModel().removeNodeFromParent(selectedTreeNode);
            application.consumeSelectedTreeNode();
        }
    }
    
    @Action
    public void refreshTopics() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        
        final ProgressMonitor progressMonitor = new ProgressMonitor(application.getMainFrame(),
                "Refreshing Topics", "", 0, 100);
        
        TaskService defaultService = Application.getInstance().getContext().getTaskService();
        defaultService.execute(new LoadTopicsTask(application, progressMonitor));
    }
}
