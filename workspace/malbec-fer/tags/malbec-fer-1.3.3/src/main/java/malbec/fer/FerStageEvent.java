package malbec.fer;

import java.beans.PropertyChangeEvent;

@SuppressWarnings("serial")
public class FerStageEvent extends PropertyChangeEvent {

//    public FerStageEvent(Object source, String propertyName, Object oldValue, Object newValue) {
//        super(source, propertyName, oldValue, newValue);
//       
//    }

    public FerStageEvent(Object source, String propertyName, Object oldValue) {
        super(source, propertyName, oldValue, null);
       
    }
    
    public String getClientOrderId() {
        return (String) getOldValue();
    }
    
}
