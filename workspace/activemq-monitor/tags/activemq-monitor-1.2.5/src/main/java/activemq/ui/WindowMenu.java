package activemq.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Menu component that handles the functionality expected of a standard "Windows" menu for MDI applications.
 */
@SuppressWarnings("serial")
public class WindowMenu extends JMenu {
    private MdiDesktopPane desktop;
    private ActionMap constantActions;

    public WindowMenu(final MdiDesktopPane desktop, final ActionMap windowActions) {
        this.desktop = desktop;
        this.constantActions = windowActions;
        setText("Window");
        
        addMenuListener(new MenuListener() {
            public void menuCanceled(MenuEvent e) {}

            public void menuDeselected(MenuEvent e) {
                removeAll();
            }

            /**
             * Build the menu dynamically when we are selected
             */
            public void menuSelected(MenuEvent e) {
                buildChildMenus();
            }
        });
    }

    /* Sets up the children menus depending on the current desktop state */
    private void buildChildMenus() {
        JInternalFrame[] array = desktop.getAllFrames();

        // Add back our constant actions
        // if we have windows, enable the functions
        boolean haveWindows = array.length > 0; 
        // TODO if we need to change the order, we need to figure it out here
        for (Object actionName : constantActions.keys()) {
            Action action = constantActions.get(actionName);
            action.setEnabled(haveWindows);
            JMenuItem menuItem = new JMenuItem();
            menuItem.setAction(action);
            add(menuItem);
        }
        
        if (haveWindows) {
            addSeparator();
        }

        for (JInternalFrame internalFrame : array) {
            ChildMenuItem menu = new ChildMenuItem(internalFrame);
            menu.setState(internalFrame.isSelected());
            menu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JInternalFrame frame = ((ChildMenuItem) ae.getSource()).getFrame();
                    frame.moveToFront();
                    try {
                        frame.setSelected(true);
                    } catch (PropertyVetoException e) {
                        e.printStackTrace();
                    }
                }
            });
            menu.setIcon(internalFrame.getFrameIcon());
            add(menu);
        }
    }

    /*
     * This JCheckBoxMenuItem descendant is used to track the child frame that corresponds to a give menu.
     */
    static class ChildMenuItem extends JCheckBoxMenuItem {
        private JInternalFrame frame;

        public ChildMenuItem(JInternalFrame frame) {
            super(frame.getTitle());
            this.frame = frame;
        }

        public JInternalFrame getFrame() {
            return frame;
        }
    }
}