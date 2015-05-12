package activemq.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.MessageUtil;
import activemq.IJmsObserver;
import activemq.ui.AbstractTableCellRenderer;

/**
 * Listens for JMS messages.
 * 
 * We are really only interested in <code>TextMessage</code>s, but we will
 * receive all types.
 * 
 * The data for this table is key/value pairs.
 * 
 */
@SuppressWarnings("serial")
public class JmsMessageTableModel extends AbstractTableModel implements IJmsObserver<Message> {
    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    //Application.getInstance().getContext().getTaskService("Timer");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final int HIGHLIGHT_DURATION = 1500;

    private List<PublishedField> rows = new ArrayList<PublishedField>();
    private List<String> columns = new ArrayList<String>();

    private String topicToMonitor;

    private Object lockObject = new Object();

    public JmsMessageTableModel(String topicName) {
        topicToMonitor = topicName;
        columns.add("Name");
        columns.add("Value");
        columns.add("Published At");
    }

    @Override
    public void onUpdate(Object source, Message message) {
        if (message instanceof TextMessage) {
            try {
                // Only process messages for us
                Topic topic = (Topic)message.getJMSDestination();
                if (!topicToMonitor.equals(topic.getTopicName())) {
                    return;
                }

                String recordStr = ((TextMessage) message).getText();
                Map<String, String> record = MessageUtil.extractRecord(recordStr, new TreeMap<String, String>());


                //Date timestamp = MessageUtil.getPublishTimestamp(record);
                Date timestamp = new Date(message.getJMSTimestamp());
                for (Entry<String, String> entry : record.entrySet()) {
                    PublishedField newRow = new PublishedField();
                    newRow.name = entry.getKey();
                    newRow.value = entry.getValue();
                    newRow.publishedTimetimestamp = timestamp;

                    // If the row change, update the table and fire an update
                    // event, schedule a refresh event to redraw the row once
                    // it has been hightlighted for the specified period
                    synchronized (lockObject) {
                        final int currentRow = rows.indexOf(newRow);
                        if (currentRow != -1) {
                            if (newRow.changed(rows.get(currentRow))) {
                                rows.set(currentRow, newRow);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        fireTableRowsUpdated(currentRow, currentRow);
                                        scheduler.schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                SwingUtilities.invokeLater(new Runnable() {
                                                    public void run() {
                                                        //fireTableRowsUpdated(currentRow, currentRow);
                                                        fireTableDataChanged();
                                                    }
                                                });
                                            }
                                        }, HIGHLIGHT_DURATION + 500, TimeUnit.MILLISECONDS);
                                    }
                                });
                            }
                        } else {
                            final int lastRow = rows.size();
                            rows.add(newRow);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    fireTableRowsInserted(lastRow + 1, lastRow + 1);
                                    scheduler.schedule(new Runnable() {
                                        @Override
                                        public void run() {
                                            SwingUtilities.invokeLater(new Runnable() {
                                                public void run() {
                                                    //fireTableRowsInserted(lastRow + 1, lastRow + 1);
                                                    fireTableDataChanged();
                                                }
                                            });
                                        }
                                    }, HIGHLIGHT_DURATION + 500, TimeUnit.MILLISECONDS);
                                }
                            });
                        }
                    }
                }
            } catch (JMSException e) {
                log.error("Unable to extract text from JMS message", e);
            }
        } else {
            log.warn("Non-text message received");
        }
    }
    
    @Override
    public String getColumnName(int column) {
        return columns.get(column);
    }

    @Override
    public int getColumnCount() {
        synchronized (lockObject) {
            return columns.size();
        }
    }

    @Override
    public int getRowCount() {
        synchronized (lockObject) {
            return rows.size();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        synchronized (lockObject) {
            PublishedField row = rows.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return row.name;
                case 1:
                    return row.value;
                case 2:
                    return row.publishedTimetimestamp;
                default:
                    return "#Value";
            }
        }
    }

    private static class PublishedField {
        String name;
        String value;
        Date publishedTimetimestamp;
        long createdAt = System.currentTimeMillis();

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PublishedField)) {
                return false;
            }
            PublishedField other = (PublishedField) obj;
            return name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() * (17 + value.hashCode() * (17 + publishedTimetimestamp.hashCode()));
        }

        public boolean changed(PublishedField other) {
            return !(name.equals(other.name) && value.equals(other.value));
        }
    }

    public static final class ChangeRowTableCellRenderer extends AbstractTableCellRenderer {

        private Color highlightColor = new Color(128,220,255);
        private Color nameText = Color.GREEN.darker();
        @Override
        protected void overrideCellColor(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JmsMessageTableModel model = (JmsMessageTableModel) table.getModel();

            int rowIndex = table.convertRowIndexToModel(row);

            PublishedField fieldRow = model.rows.get(rowIndex);

            //long leftSide = fieldRow.publishedTimetimestamp.getTime() + HIGHLIGHT_DURATION - 500;
            long leftSide = fieldRow.createdAt + HIGHLIGHT_DURATION - 500;
            long rightSide = System.currentTimeMillis();
            
            if (column == 0) {
                setForeground(nameText);
            }
            
            if (leftSide > rightSide) {
                this.setBackground(highlightColor);
            } 
        }
    }
    
    public static final class ChangeRowTableCellRenderer2 extends DefaultTableCellRenderer {

        private Color highlightColor = new Color(128, 220, 255);
        private Color nameText = Color.GREEN.darker();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JmsMessageTableModel model = (JmsMessageTableModel) table.getModel();

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int rowIndex = table.convertRowIndexToModel(row);

            PublishedField fieldRow = model.rows.get(rowIndex);

            // long leftSide = fieldRow.publishedTimetimestamp.getTime() +
            // HIGHLIGHT_DURATION - 500;
            long leftSide = fieldRow.createdAt + HIGHLIGHT_DURATION - 500;
            long rightSide = System.currentTimeMillis();

            if (column == 0) {
                setForeground(nameText);
            }

            if (leftSide > rightSide) {
                setBackground(highlightColor);
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }
}
