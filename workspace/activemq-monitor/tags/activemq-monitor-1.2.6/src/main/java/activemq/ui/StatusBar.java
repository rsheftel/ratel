package activemq.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.jdesktop.application.TaskMonitor;

/**
 * A StatusBar panel that tracks a TaskMonitor. Although one could certainly create a more elaborate StatusBar
 * class, this one is sufficient for the examples that need one.
 * <p>
 * This class loads resources from the ResourceBundle called {@code resources.StatusBar}.
 * 
 */
@SuppressWarnings("serial")
public class StatusBar extends JPanel implements PropertyChangeListener {
    private final Insets zeroInsets = new Insets(0, 0, 0, 0);
    private final JLabel messageLabel;
    private final JLabel topicCountLabel;
    private int topicCount = -1;
    private final JLabel selectedCountLabel;
    

    /**
     * Constructs a panel that displays messages/progress/state properties of the {@code taskMonitor's}
     * foreground task.
     * 
     * @param taskMonitor
     *                the {@code TaskMonitor} whose {@code PropertyChangeEvents} {@code this StatusBar} will
     *                track.
     */
    public StatusBar(TaskMonitor taskMonitor) {
        super(new GridBagLayout());
        setBorder(new EmptyBorder(2, 0, 6, 0)); // top, left, bottom, right
        messageLabel = new JLabel(" ");
        topicCountLabel = new JLabel(" ");
        selectedCountLabel = new JLabel(" ");

        GridBagConstraints c = new GridBagConstraints();
        initGridBagConstraints(c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        add(new JSeparator(), c);

        initGridBagConstraints(c);
        c.insets = new Insets(6, 6, 0, 3); // top, left, bottom, right;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(messageLabel, c);

        initGridBagConstraints(c);
        c.insets = new Insets(6, 6, 0, 3); // top, left, bottom, right;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(selectedCountLabel, c);

        initGridBagConstraints(c);
        c.insets = new Insets(6, 3, 0, 3); // top, left, bottom, right;
        add(topicCountLabel, c);

        taskMonitor.addPropertyChangeListener(this);
    }

    public void setMessage(String s) {
        messageLabel.setText((s == null) ? "" : s);
    }

    private void initGridBagConstraints(GridBagConstraints c) {
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.insets = zeroInsets;
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 0.0;
        c.weighty = 0.0;
    }

    /**
     * The TaskMonitor (constructor arg) tracks a "foreground" task; this method is called each time a
     * foreground task property changes.
     */
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();

        if ("message".equals(propertyName)) {
            String text = (String) (e.getNewValue());
            setMessage(text);
        }
    }

    public void setTopicCount(int size) {
        topicCount = size;
        topicCountLabel.setText("Topics: " + String.valueOf(size));
    }

    public int removeTopic() {
        setTopicCount(--topicCount);
        return topicCount;
    }
    
    public void setMessageForeground(Color fgColor) {
        messageLabel.setForeground(fgColor);
    }

    public void setSelectedCount(final int selectedItemCount) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (selectedItemCount == -1) {
                    selectedCountLabel.setText(" ");
                } else {
                    selectedCountLabel.setText(String.valueOf(selectedItemCount));
                }
            }
            
        });
        
    }
}
