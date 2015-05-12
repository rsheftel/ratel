package activemq.ui.actions;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;

import activemq.ActiveMQMonitor;
import activemq.ui.ConfigurationPanelForm;

public class ConfigurationActions {

    @Action
    public void editConfiguration() {
        try {
            final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
            final JFrame mainFrame = application.getMainFrame();

            JDialog dialog = new JDialog(mainFrame);

            ConfigurationPanelForm panel = new ConfigurationPanelForm(application);
            panel.setDialog(dialog);
            panel.setBorder(new EmptyBorder(2, 4, 2, 4));
            dialog.add(panel);

            dialog.pack();
            dialog.setLocationRelativeTo(mainFrame);
            dialog.setResizable(false);
            dialog.setVisible(true);
        } catch (Exception e) {
            
            // TODO remove this!!
            // This is here for debugging, it should be removed once everything is working
            e.printStackTrace();
        }
    }
}
