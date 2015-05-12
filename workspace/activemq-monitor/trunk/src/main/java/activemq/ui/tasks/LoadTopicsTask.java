/**
 * 
 */
package activemq.ui.tasks;

import java.awt.Color;
import java.util.Date;
import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.jdesktop.application.Task;

import util.IObserver;
import activemq.ActiveMQMonitor;
import activemq.broker.BrokerMonitor;
import activemq.ui.tree.BrokerTreeNode;
import activemq.ui.tree.MultiBrokerTreeModel;

public class LoadTopicsTask extends Task<Boolean, Void> {
    
    private final class ProgressObserver implements IObserver<Integer> {
        private int base = 0;
        private int max;
        
        private float spread;
        
        ProgressObserver(int base, int max) {
            if (max > 100) {
                throw new IllegalArgumentException("Cannot be greater than 100%");
            }
            this.base = base;
            this.max = max;
            spread = this.max - this.base;
        }
        
        @Override
        public void onUpdate(Integer progress) {
            final float additional = (spread * progress / 100.0f) + base;
            updateProgress((int)additional);
        }
    }

    private final ProgressMonitor progressMonitor;
    private final BrokerMonitor brokerMonitor;
    private final ActiveMQMonitor application;
    //private TopicTreeModel treeModel;
    private MultiBrokerTreeModel treeModel;
    
    private BrokerTreeNode brokerNode;
    

    public LoadTopicsTask(ActiveMQMonitor application, BrokerMonitor brokerMonitor, ProgressMonitor progressMonitor) {
        super(application);
        this.progressMonitor = progressMonitor;
        this.application = application;
        this.brokerMonitor = brokerMonitor;
    }

    @Override
    protected void finished() {
        application.getStatusBar().setMessage("");
        System.out.println("Finished:" + new Date());
    }

    /* (non-Javadoc)
     * @see org.jdesktop.application.Task#cancelled()
     */
    @Override
    protected void cancelled() {
        System.out.println("Task was cancelled");
    }


    /* (non-Javadoc)
     * @see org.jdesktop.application.Task#failed(java.lang.Throwable)
     */
    @Override
    protected void failed(Throwable cause) {
        // TODO find out if we can display this as broken
        brokerNode = new BrokerTreeNode(brokerMonitor.getConfig());
        treeModel = application.getTreeModel();
        treeModel.addBroker(brokerNode);
        progressMonitor.setProgress(100);

        // error message
        application.getStatusBar().setMessageForeground(Color.RED);
        StringBuilder sb = new StringBuilder(512);
        sb.append("Not able to connect to broker: ").append(brokerMonitor.getConfig().getJmsUrl());
        sb.append(cause.getMessage());
        application.getStatusBar().setMessage(sb.toString());
        cause.printStackTrace();
    }


    /* (non-Javadoc)
     * @see org.jdesktop.application.Task#succeeded(java.lang.Object)
     */
    @Override
    protected void succeeded(Boolean result) {
        treeModel = application.getTreeModel();
        //treeModel.removeBrokerNode(brokerConfig.getServerName());
        treeModel.addBroker(brokerNode);

        progressMonitor.setProgress(100);
        brokerMonitor.removeAdvisoryObserver(application);
        brokerMonitor.addAdvisoryObserver(application);
    }


    private void updateProgress(final int progress) {
        
        if (SwingUtilities.isEventDispatchThread()) {
            progressMonitor.setProgress(progress);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressMonitor.setProgress(progress);
                }
            });
        }
    }
    
    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            // remove any topics that are associated with this broker
            application.setTopicCount(application.getTopicCount() - brokerMonitor.getTopicCount());
            application.getStatusBar().setMessageForeground(Color.BLUE);
            application.getStatusBar().setMessage("Connecting to " + brokerMonitor.getServerName());
            // Ensure we are the correct size
            progressMonitor.setNote("");
            
            if (!brokerMonitor.isStarted()) {
                brokerMonitor.startAll();
            }
            
            if (brokerMonitor.isConnectedToJmx() ) {
                updateProgress(1);
                progressMonitor.setNote("Connected JMX");
            }

            if (progressMonitor.isCanceled()) {
                return Boolean.FALSE;
            }
            
            if (brokerMonitor.isConnectedToTopics()) {
                updateProgress(2);
                progressMonitor.setNote("Connected JMS");
            }

            if (progressMonitor.isCanceled()) {
                return Boolean.FALSE;
            }

            if (brokerMonitor.isConnectedToAdvisory()) {
                updateProgress(3);
                progressMonitor.setNote("Connected Advisory");
            }

            if (progressMonitor.isCanceled()) {
                return Boolean.FALSE;
            }

            progressMonitor.setNote("Collecting topics");
            List<DestinationViewMBean> destList = brokerMonitor.listTopics(new ProgressObserver(4, 55));

            if (progressMonitor.isCanceled()) {
                return Boolean.FALSE;
            }
            
            progressMonitor.setNote("Creating tree...");
            brokerMonitor.setTopicCount(destList.size());
            application.setTopicCount(application.getTopicCount() + destList.size());


            brokerNode = new BrokerTreeNode(brokerMonitor.getConfig());
            float count = 0;
            float max = destList.size();
            ProgressObserver observer = new ProgressObserver(56, 98);
            for (DestinationViewMBean destNode : destList) {
                brokerNode.add(destNode);
                if (observer != null) {
                    count++;
                    observer.onUpdate( (int)(count / max * 100f));
                }
            }
            
            progressMonitor.setNote("Added topics to tree");


            return Boolean.TRUE;
        } catch (Exception e) {
       /*     application.getStatusBar().setMessageForeground(Color.RED);
            ActiveMQMonitor app = (ActiveMQMonitor) getApplication();
            StringBuilder sb = new StringBuilder(512);
            sb.append("Not able to connect to broker: ").append(brokerMonitor.getConfig().getJmsUrl());
            sb.append(e.getMessage());
            application.getStatusBar().setMessage(sb.toString());

            return Boolean.FALSE;
            */
            throw e;
        }
    }
}