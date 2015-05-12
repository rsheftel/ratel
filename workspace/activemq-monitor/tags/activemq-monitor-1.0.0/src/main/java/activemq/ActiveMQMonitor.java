package activemq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.Executors;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;

import util.TopicViewNode;
import activemq.jms.TopicMonitor;
import activemq.jmx.JmxMonitor;
import activemq.ui.JmsMessageTableModel;
import activemq.ui.TopicNodeRenderer;
import activemq.ui.TopicTreeModel;
import activemq.ui.TopicTreeNode;

public class ActiveMQMonitor extends Application {
    // TODO put this into the configuration
    private static final int TCP_PORT = 63636;
    private static final int JMX_PORT = 9393;

    private static final String HOST_NAME = "nysrv31";
    protected static final String BROKER_URL = "tcp://" + HOST_NAME + ":" + TCP_PORT;
    protected static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT + "/jmxrmi";

    private JmxMonitor jmxMonitor;
    private TopicMonitor topicMonitor;
    private TopicViewNode selectedViewNode;
    private JDesktopPane desktop = new JDesktopPane();

    // private JFrame appFrame = null;
    private JLabel messageLabel = null;
    private TopicTreeModel treeModel;


    @Override
    protected void startup() {
        // start the ActiveMQ stuff
        jmxMonitor = new JmxMonitor(JMX_URL);
        jmxMonitor.startup();

        topicMonitor = new TopicMonitor(BROKER_URL);
        topicMonitor.startup();

        treeModel = new TopicTreeModel(jmxMonitor.listTopics());

        // Outline outline = new
        // Outline(DefaultOutlineModel.createOutlineModel(listModel, new
        // FileRowModel()));
        JTree outline = new JTree(treeModel);
        outline.setCellRenderer(new TopicNodeRenderer());
        // The selection event seems to be called first, handle it, then the
        // double-click
        outline.addMouseListener(new NodeMouseListener());
        outline.addTreeSelectionListener(new NodeSelectionListener());

        Border border = new EmptyBorder(2, 4, 2, 4);

        JScrollPane scrollPane = new JScrollPane(outline);
//        scrollPane.setBorder(border);

        JPanel panel = new JPanel();
        messageLabel = new JLabel(" ");
        messageLabel.setBorder(border);

        final JFrame appFrame = new JFrame("ActiveMQ Monitor");
        // put the two panels into a split pane
        desktop = new JDesktopPane();
        //desktop.setLayout(new FlowLayout());
        desktop.setMinimumSize(new Dimension(200,200));

        // The split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, scrollPane, desktop);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(10);
        outline.setMinimumSize(new Dimension(100, 300));
        
        appFrame.add(splitPane, BorderLayout.CENTER);
        appFrame.add(panel, BorderLayout.NORTH);
        appFrame.add(messageLabel, BorderLayout.SOUTH);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.pack();
        appFrame.setSize(new Dimension(900, 800));
        appFrame.setLocationRelativeTo(null);
        appFrame.setVisible(true);
        
        TaskService taskService = new TaskService("Timer", Executors.newScheduledThreadPool(1));
        Application.getInstance().getContext().addTaskService(taskService);
        taskService.execute(new Task<Void, Boolean>(Application.getInstance()) {

            @Override
            protected Void doInBackground() throws Exception {
                while(!isCancelled()) {
                    Thread.sleep(500);
                    publish(topicMonitor.isConnected());
                }
                
                return (Void)null;
            }

            @Override
            protected void process(List<Boolean> values) {
                super.process(values);
                boolean connected = values.get(0);
                if (connected) {
                    messageLabel.setForeground(Color.GREEN.darker());
                    messageLabel.setText("Connected to " + BROKER_URL);
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Disconnected!  Restart!");
                    cancel(true);
                    JOptionPane.showMessageDialog(appFrame,
                            "Disconnected from broker.",
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        
        
    }

    private final class NodeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                TopicTreeNode selectedNode = (TopicTreeNode) path.getLastPathComponent();

                selectedViewNode = (TopicViewNode) selectedNode.getUserObject();
            }
        }
    }

    @SuppressWarnings("serial")

    private final class NodeMouseListener extends MouseAdapter {
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && selectedViewNode != null && selectedViewNode.isLeaf()) {
                JInternalFrame jif = new JInternalFrame(selectedViewNode.getHierarchicalName(), true, true, true, true);
                JmsMessageTableModel newTable = new JmsMessageTableModel(selectedViewNode.getHierarchicalName());
                topicMonitor.addObserver(newTable);
                topicMonitor.listenTo(selectedViewNode.getHierarchicalName(), true);

                JTable recordTable = new JTable(newTable);
                // recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                recordTable.setDefaultRenderer(Object.class, new JmsMessageTableModel.ChangeRowTableCellRenderer());
                JScrollPane scrollPane = new JScrollPane(recordTable);
                jif.getContentPane().add(scrollPane);
                desktop.add(jif);
                int componentCount = desktop.getComponents().length - 1;
                jif.setLocation(componentCount * 50, componentCount * 50);
                jif.pack();

                jif.addInternalFrameListener(new InternalFrameAdapter() {
                    @Override
                    public void internalFrameClosing(InternalFrameEvent e) {
                        topicMonitor.stopListeningTo(e.getInternalFrame().getTitle(), true);
                    }
                });
                jif.setVisible(true);
            }
        }
    }

    public static void main(String[] args) {
        Application.launch(ActiveMQMonitor.class, args);
    }
}