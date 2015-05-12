package activemq.ui.actions;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;

import util.RemoteService;
import activemq.ActiveMQMonitor;

public class ServerActions {

    @Action
    public void restartActiveMQServer() {
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        final JFrame mainFrame = application.getMainFrame();

        String serverName = application.getServerName();
        int userChoice = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to restart the ActiveMQ server '" + serverName + "'",
                "Confirm Restart " + serverName, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (userChoice == JOptionPane.YES_OPTION) {
            int userChoice2 = JOptionPane.showConfirmDialog(mainFrame,
                    "This will restart the the ActiveMQ server '" + serverName
                            + "'\nAll processes will be disconnected and last 'image' reset",
                    "Confirm Restart " + serverName, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (userChoice2 == JOptionPane.YES_OPTION) {
                boolean success = false;
                RemoteService rs = new RemoteService(serverName, application.getServiceName());
                try {
                    success = rs.restart();
                } catch (Exception e) {
                    JOptionPane.showConfirmDialog(mainFrame, "Failed to connect to server '" + serverName
                            + "'\nTry again or restart manually", "Restarted " + serverName,
                            JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (success) {
                    JOptionPane.showConfirmDialog(mainFrame, "Restarted server '" + serverName + "'",
                            "Restarted " + serverName, JOptionPane.PLAIN_MESSAGE,
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showConfirmDialog(mainFrame, "Failed to restart server '" + serverName
                            + "'\nTry again or restart manually", "Restarted " + serverName,
                            JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
