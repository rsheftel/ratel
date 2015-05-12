package activemq.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Provide a base class for implementing custom <code>TableCellRenderer</code>s. 
 *
 * This handles the necessary performance details of 'no-op'ing methods as documented in
 * {@link javax.swing.DefaultTableCellRenderer}
 * 
 */
public abstract class AbstractTableCellRenderer extends JLabel implements TableCellRenderer, PropertyChangeListener {

    /**
     * 
     */
    private static final long serialVersionUID = 7273804012901897926L;

    protected Color dropCellForeground;
    protected Color dropCellBackground;

    protected Color focusCellForeground;
    protected Color focusCellBackground;

    protected Color unselectedForeground;
    protected Color unselectedBackground;

    protected Color selectedForeground;
    protected Color selectedBavground;

    protected Border focusSelectedCellHighlightBorder;
    protected Border focusCellHighlightBorder;
    
    protected final static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1); 

    protected AbstractTableCellRenderer() {
        UIManager.addPropertyChangeListener(this);
        initialize();
    }

    protected void initialize() {
        dropCellForeground = UIManager.getColor("Table.dropCellForeground");
        dropCellBackground = UIManager.getColor("Table.dropCellBackground");

        focusCellForeground = UIManager.getColor("Table.focusCellForeground");
        focusCellBackground = UIManager.getColor("Table.focusCellBackground");

        focusSelectedCellHighlightBorder = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
        focusCellHighlightBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
    }
    
    protected abstract void overrideCellColor(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column);

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        // determine the color to use
        // color depends on being selected/unselected/DnD destination/cell is being edited
        // border is determined by focus/selection

        // set the colors
        boolean isDropLocation = isCellDropTarget(table.getDropLocation(), row, column);
        if (isDropLocation) {
            super.setForeground(dropCellForeground);
            super.setBackground(dropCellBackground);
        } else if (isSelected) {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else if (hasFocus && !isSelected && table.isCellEditable(row, column)) {
            super.setForeground(focusCellForeground);
            super.setBackground(focusCellBackground);
        } else {
            super.setForeground(table.getForeground());
            super.setBackground(table.getBackground());
        }

        // give subclasses the chance to change the color
        overrideCellColor(table, value, isSelected, hasFocus, row, column);
        // set the border
        if (hasFocus) {
            if (isSelected && focusSelectedCellHighlightBorder != null) {
                setBorder(focusSelectedCellHighlightBorder);
            } else {
                setBorder(focusCellHighlightBorder);
            }
        } else {
            setBorder(noFocusBorder);
        }

        // set font and value
        setFont(table.getFont());
        setValue(value);

        return this;
    }

    /**
     * Determine if this cell is the target of a DnD action.
     * 
     * @param dropLocation
     * @param row
     * @param column
     * @return
     */
    protected boolean isCellDropTarget(JTable.DropLocation dropLocation, int row, int column) {
        return (dropLocation != null
                && !dropLocation.isInsertRow()
                && !dropLocation.isInsertColumn()
                && dropLocation.getRow() == row
                && dropLocation.getColumn() == column);
    }

    protected void setValue(Object value) {
        if (value != null) {
            setText(value.toString());
        } else {
            setText("");
        }
    }

    public boolean isOpaque() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if ("lookAndFeel".equals(propertyName)) {
            initialize();
        }
    }

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        // No-op as per JavaDoc for performance
    }

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        // No-op as per JavaDoc for performance    
    }

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        // No-op as per JavaDoc for performance
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // No-op as per JavaDoc for performance
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        // No-op as per JavaDoc for performance
    }

    @Override
    public void repaint(Rectangle r) {
        // No-op as per JavaDoc for performance
    }

    @Override
    public void repaint() {
        // No-op as per JavaDoc for performance
    }

    @Override
    public void revalidate() {
        // No-op as per JavaDoc for performance
    }

    @Override
    public void validate() {
        // No-op as per JavaDoc for performance
    }

    protected Color blend(Color base, Color other) {
        // calculate the difference between the RGBs
        int r = (other.getRed() - base.getRed()) / 2;
        int g = (other.getGreen() - base.getGreen()) / 2;
        int b = (other.getBlue() - base.getBlue()) / 2;

        Color blendedColor =  new Color(ensureValidComponent(base.getRed() + r), 
                ensureValidComponent(base.getGreen()+ g),
                ensureValidComponent(base.getBlue() + b));

        return blendedColor;
    }

    private int ensureValidComponent(double componentValue) {
        return (int)Math.min(Math.max(componentValue, 0), 255);
    }

    protected Color darker(Color base, double factor) {
        return new Color(ensureValidComponent(base.getRed() * factor),
                ensureValidComponent(base.getGreen() * factor),
                ensureValidComponent(base.getBlue() * factor));
    }

    protected Color brighter(Color base, double factor) {
        int r = base.getRed();
        int g = base.getGreen();
        int b = base.getBlue();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int i = (int)(1.0/(1.0-factor));
        if ( r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i);
        }
        if ( r > 0 && r < i ) {
            r = i;
        }
        if ( g > 0 && g < i ) {
            g = i;
        }
        if ( b > 0 && b < i ) {
            b = i;
        }

        return new Color(ensureValidComponent(r/factor),
                ensureValidComponent(g/factor),
                ensureValidComponent(b/factor));
    }

}


      
