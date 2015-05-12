package activemq.ui.actions;

import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;

import activemq.ActiveMQMonitor;

public class WindowActions {
    
    @Action
    public void closeAll() {
        
        final ActiveMQMonitor application = (ActiveMQMonitor) Application.getInstance();
        
        JDesktopPane desktop = application.getDesktop();
        
        JInternalFrame[] frames = desktop.getAllFrames();
        
        for (JInternalFrame frame : frames) {
            try {
                frame.setClosed(true); // this will fire the CLOSING_EVENT
            } catch (PropertyVetoException e) {
             // ignore this
            }
        }
    }
    
}
