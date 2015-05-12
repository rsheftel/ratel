package activemq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.Executors;

import javax.swing.ActionMap;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.DestinationInfo;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;

import util.Monitoring;

import activemq.advisory.AdvisoryMessageMonitor;
import activemq.jms.TopicMonitor;
import activemq.jmx.JmxMonitor;
import activemq.ui.StatusBar;
import activemq.ui.TopicNodeRenderer;
import activemq.ui.TopicTreeModel;
import activemq.ui.TopicTreeNode;
import activemq.ui.actions.ServerActions;
import activemq.ui.actions.TreeActions;
import activemq.ui.actions.WindowActions;

public class ActiveMQMonitor extends Application implements IJmsObserver<ActiveMQMessage> {
    // TODO put this into the configuration
    private static final int TCP_PORT = 63636;
    // private static final int TCP_PORT = 64616;
    // private static final int TCP_PORT = 61616;
    private static final int JMX_PORT = 9393;
    // private static final int JMX_PORT = 1099;

    private static final String HOST_NAME = "amqmktdata";
    // private static final String HOST_NAME = "nysrv31";
    // private static final String HOST_NAME = "nysrv61";
    // private static final String HOST_NAME = "nyws802";
    // private static final String HOST_NAME = "nyux51";
    // private static final String HOST_NAME = "nyws751";

    // private static final String FAILOVER_CONFIG = "?initialReconnectDelay=100&maxReconnectAttempts=5";
    private static final String FAILOVER_CONFIG = "";

    protected static final String BROKER_URL = "tcp://" + HOST_NAME + ":" + TCP_PORT + FAILOVER_CONFIG;
    // protected static final String BROKER_URL = "tcp://" + HOST_NAME + ":" + TCP_PORT;
    protected static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT
            + "/jmxrmi";

    private JmxMonitor jmxMonitor;
    private TopicMonitor topicMonitor;
    private AdvisoryMessageMonitor advisoryMonitor;
    private TopicTreeNode selectedTreeNode;
    private JDesktopPane desktop = new JDesktopPane();

    private final Object lockObject = new Object();
    // private JLabel messageLabel = null;
    private StatusBar statusBar;
    private TopicTreeModel treeModel;
    final JFrame appFrame = new JFrame("ActiveMQ Monitor");

    private JTree topicTree;

    private int topicCount;

    public JFrame getMainFrame() {
        return appFrame;
    }

    @Override
    protected void startup() {
        // Outline outline = new
        // Outline(DefaultOutlineModel.createOutlineModel(listModel, new
        // FileRowModel()));
        // JTree outline = new JTree(treeModel);
        topicTree = new JTree(treeModel);

        topicTree.setCellRenderer(new TopicNodeRenderer());
        // The selection event seems to be called first, handle it, then the
        // double-click

        ActionMap actionMap = getContext().getActionMap(new TreeActions());
        JPopupMenu menu = new JPopupMenu();
        menu.add(actionMap.get("monitorTopic"));
        menu.add(actionMap.get("removeTopic"));

        topicTree.addMouseListener(new NodeMouseListener(menu));
        topicTree.addTreeSelectionListener(new NodeSelectionListener());

        JScrollPane scrollPane = new JScrollPane(topicTree);
        // scrollPane.setBorder(border);

        JPanel panel = new JPanel();

        statusBar = new StatusBar(Application.getInstance().getContext().getTaskMonitor());
        // put the two panels into a split pane
        desktop = new JDesktopPane();
        // desktop.setLayout(new FlowLayout());
        desktop.setMinimumSize(new Dimension(200, 200));

        // The split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, scrollPane, desktop);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(10);
        topicTree.setMinimumSize(new Dimension(100, 300));

        appFrame.add(splitPane, BorderLayout.CENTER);
        appFrame.add(panel, BorderLayout.NORTH);
        appFrame.add(statusBar, BorderLayout.SOUTH);

        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.pack();
        appFrame.setSize(new Dimension(900, 800));
        appFrame.setLocationRelativeTo(null);
        appFrame.setJMenuBar(createMenuBar());
        appFrame.setVisible(true);

    }

    protected void ready() {
        // Create all of the monitors and connect them later
        jmxMonitor = new JmxMonitor(JMX_URL);
        topicMonitor = new TopicMonitor(BROKER_URL);
        advisoryMonitor = new AdvisoryMessageMonitor(topicMonitor.getConnectionTask());
        advisoryMonitor.setMonitorDestinations(true);

        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();

        final ProgressMonitor progressMonitor = new ProgressMonitor(application.getMainFrame(),
                "Loading Topics", "", 0, 100);

        TaskService defaultService = Application.getInstance().getContext().getTaskService();
        defaultService.execute(new LoadTopicsTask(application, progressMonitor, application));

        TaskService taskService = new TaskService("Timer", Executors.newScheduledThreadPool(1));
        Application.getInstance().getContext().addTaskService(taskService);
        taskService.execute(new ConnectionMonitorTask(Application.getInstance()));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // Put in the default File->Exit
        ActionMap defaultActionMap = getContext().getActionMap();
        JMenu fileMenu = new JMenu("File");

        // ActionMap configActionMap = getContext().getActionMap(new ConfigurationActions());

        // JMenuItem configItem = new JMenuItem(configActionMap.get("editConfiguration"));
        JMenuItem configItem = new JMenuItem("Configuration");
        configItem.setEnabled(false);
        fileMenu.add(configItem);

        fileMenu.add(new JSeparator());

        JMenuItem exitItem = new JMenuItem(defaultActionMap.get("quit"));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        ActionMap serverActionMap = getContext().getActionMap(new ServerActions());
        JMenu serverMenu = new JMenu("Servers");
        JMenuItem restartItem = new JMenuItem();
        restartItem.setAction(serverActionMap.get("restartActiveMQServer"));
        restartItem.setIcon(null);
        serverMenu.add(restartItem);

        menuBar.add(serverMenu);

        ActionMap windowActionMap = getContext().getActionMap(new WindowActions());
        JMenu windowMenu = new JMenu("Window");
        JMenuItem closeAllItem = new JMenuItem();
        closeAllItem.setAction(windowActionMap.get("closeAll"));
        closeAllItem.setIcon(null);
        windowMenu.add(closeAllItem);

        menuBar.add(windowMenu);

        return menuBar;
    }

    public static void main(String[] args) {
        Application.launch(ActiveMQMonitor.class, args);
    }

    public TopicTreeNode consumeSelectedTreeNode() {
        synchronized (lockObject) {
            TopicTreeNode current = selectedTreeNode;
            selectedTreeNode = null;
            return current;
        }
    }

    public TopicTreeNode getSelectedTreeNode() {
        synchronized (lockObject) {
            return selectedTreeNode;
        }
    }

    void setSelectedTreeNode(TopicTreeNode selectedNode) {
        synchronized (lockObject) {
            this.selectedTreeNode = selectedNode;
        }
    }

    public JmxMonitor getJmxMonitor() {
        return jmxMonitor;
    }

    public void setJmxMonitor(JmxMonitor jmxMonitor) {
        this.jmxMonitor = jmxMonitor;
    }

    public TopicMonitor getTopicMonitor() {
        return topicMonitor;
    }

    public void setTopicMonitor(TopicMonitor topicMonitor) {
        this.topicMonitor = topicMonitor;
    }

    public JDesktopPane getDesktop() {
        return desktop;
    }

    public TopicTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * TODO This should be from the configuration
     *
     * @return
     */
    public String getServerName() {
        return HOST_NAME;
    }

    /**
     * TODO This should be from the configuration
     *
     * @return
     */
    public String getServiceName() {
        return "ActiveMQ 5.1.0";
    }

    @Override
    public void onUpdate(ActiveMQMessage message) {
        // We are only interested in new destinations at this time.
        DataStructure dataStruct = message.getDataStructure();

        if (dataStruct instanceof DestinationInfo) {
            // TODO add the new topic to the tree
            // String messageTopicName = message.getDestination().getPhysicalName();
            DestinationInfo destInfo = (DestinationInfo) dataStruct;
            String newTopicName = destInfo.getDestination().getPhysicalName();

            if (newTopicName != null) {

                List<TopicViewMBean> newTopics = jmxMonitor.filterTopics(newTopicName);
                if (newTopics.size() > 0) {
                    getTreeModel().addTopic(newTopics.get(0));
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setTopicCount(topicCount + 1);
                        }

                    });

                }
            }
        }
    }

    private final class LoadTopicsTask extends Task<Boolean, Void> {
        private final ProgressMonitor progressMonitor;
        private final ActiveMQMonitor application;

        private LoadTopicsTask(Application application, ProgressMonitor progressMonitor,
                ActiveMQMonitor application2) {
            super(application);
            this.progressMonitor = progressMonitor;
            this.application = application2;
        }

        @Override
        protected void finished() {
            progressMonitor.setProgress(100);
            advisoryMonitor.addObserver(application);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            try {
                jmxMonitor.startup();
                progressMonitor.setProgress(1);
                progressMonitor.setNote("Connected JMX");
                topicMonitor.startup();
                progressMonitor.setProgress(2);
                progressMonitor.setNote("Connected JMS");
                advisoryMonitor.startup();
                progressMonitor.setProgress(3);
                progressMonitor.setNote("Connected Advisory");

                List<TopicViewMBean> model = jmxMonitor.listTopics();
                progressMonitor.setProgress(55);
                progressMonitor.setNote("Created topics");

                treeModel = new TopicTreeModel(model);
                progressMonitor.setProgress(95);
                progressMonitor.setNote("Added topics to tree");
                topicTree.setModel(treeModel);
                progressMonitor.setProgress(99);

                application.setTopicCount(model.size());

                return Boolean.TRUE;
            } catch (Exception e) {
                statusBar.setMessageForeground(Color.RED);
                statusBar.setMessage("Not able to connect to broker: " + BROKER_URL);

                return Boolean.FALSE;
            }
        }
    }

    private final class ConnectionMonitorTask extends Task<Void, Boolean> {
        private ConnectionMonitorTask(Application application) {
            super(application);
        }

        @Override
        protected Void doInBackground() throws Exception {
            // Sleep for 10 seconds before we start working
            Monitoring.sleep(10000);
            while (!isCancelled()) {
                Thread.sleep(500);
                publish(topicMonitor.isConnected());
            }

            return (Void) null;
        }

        @Override
        protected void process(List<Boolean> values) {
            super.process(values);
            boolean connected = values.get(0);
            if (connected) {
                statusBar.setMessageForeground(Color.GREEN.darker());
                statusBar.setMessage("Connected to " + BROKER_URL);
            } else {
                statusBar.setMessageForeground(Color.RED);
                statusBar.setMessage("Disconnected!  Restart GUI! - " + BROKER_URL);
                cancel(true);
                JOptionPane.showMessageDialog(appFrame, "Disconnected from broker.", "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private final class NodeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                setSelectedTreeNode((TopicTreeNode) path.getLastPathComponent());
            }
        }
    }

    @SuppressWarnings("serial")
    private final class NodeMouseListener extends MouseAdapter {
        private final JPopupMenu menu;

        public NodeMouseListener(JPopupMenu menu) {
            this.menu = menu;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && haveLeafNode()) {
                ActionMap actionMap = getContext().getActionMap(new TreeActions());
                actionMap.get("monitorTopic").actionPerformed(null);
            }
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger() && haveLeafNode()) {
                // TODO we currently only have actions for leaf nodes,
                // if that changes we need to rebuild the menu here
                menu.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.isPopupTrigger()) {
                // TODO select the node and then popup
                System.out.println("TODO Need to handle this");

            }
        }

        private boolean haveLeafNode() {
            return selectedTreeNode != null && selectedTreeNode.isLeaf();
        }
    }

    public void setTopicCount(int size) {
        topicCount = size;
        statusBar.setTopicCount(topicCount);
    }

}