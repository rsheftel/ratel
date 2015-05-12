package activemq.ui.util;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

public class FormCreationHelper {

    public static JFormattedTextField createNumericField(int fractions) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(fractions);
        numberFormat.setGroupingUsed(false);
        
        NumberFormatter formatter = new NumberFormatter(numberFormat);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        
        return new JFormattedTextField(formatter);
    }
}
