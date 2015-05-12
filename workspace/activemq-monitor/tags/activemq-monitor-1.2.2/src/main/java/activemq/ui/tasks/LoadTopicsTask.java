/**
 * 
 */
package activemq.ui.tasks;

import java.awt.Color;
import java.util.Date;
import java.util.List;

import javax.swing.ProgressMonitor;

import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.jdesktop.application.Task;

import util.IObserver;

import activemq.ActiveMQMonitor;
import activemq.ui.TopicTreeModel;

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
            float additional = (spread * progress / 100.0f) + base;
            
            progressMonitor.setProgress((int)additional);
        }
    }

    /**
     * 
     */

    private final ProgressMonitor progressMonitor;
    private final ActiveMQMonitor application;
    private TopicTreeModel treeModel;
    

    public LoadTopicsTask(ActiveMQMonitor application, ProgressMonitor progressMonitor) {
        super(application);
        this.progressMonitor = progressMonitor;
        this.application = application;
    }

    @Override
    protected void finished() {
        progressMonitor.setProgress(100);
        application.getAdvisoryMessageMonitor().removeObserver(application);
        application.getAdvisoryMessageMonitor().addObserver(application);
        System.out.println("Finished:" + new Date());
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            if (!application.getJmxMonitor().isConnected()) {
                application.getJmxMonitor().startup();
                progressMonitor.setProgress(1);
                progressMonitor.setNote("Connected JMX");
            }

            if (!application.getTopicMonitor().isConnected()) {
                application.getTopicMonitor().startup();
                progressMonitor.setProgress(2);
                progressMonitor.setNote("Connected JMS");
            }

            if (!application.getAdvisoryMessageMonitor().isConnected()) {
                application.getAdvisoryMessageMonitor().startup();
                progressMonitor.setProgress(3);
                progressMonitor.setNote("Connected Advisory");
            }

            progressMonitor.setNote("Collecting topics");
            List<TopicViewMBean> model = application.getJmxMonitor().listTopics(new ProgressObserver(4, 55));
            progressMonitor.setNote("Created topics");

            treeModel = new TopicTreeModel(model, new ProgressObserver(56, 98));
            application.setTopicTreeModel(treeModel);
            progressMonitor.setNote("Added topics to tree");

            application.setTopicCount(model.size());

            return Boolean.TRUE;
        } catch (Exception e) {
            application.getStatusBar().setMessageForeground(Color.RED);
            application.getStatusBar().setMessage(
                    "Not able to connect to broker: " + ActiveMQMonitor.BROKER_URL);

            return Boolean.FALSE;
        }
    }
}