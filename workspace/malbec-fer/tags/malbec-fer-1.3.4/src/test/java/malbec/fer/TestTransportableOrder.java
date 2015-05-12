/**
 * 
 */
package malbec.fer;

import java.util.ArrayList;
import java.util.List;

final class TestTransportableOrder implements ITransportableOrder {
    private boolean didTransport;
    private List<String> myErrors = new ArrayList<String>();

    public void addError(String errorMessage) {
        myErrors.add(errorMessage);
    }
    
    @Override
    public List<String> errors() {
        return myErrors;
    }

    public boolean didTransport() {
        return didTransport;
    }

    @Override
    public boolean transport() {
        didTransport = true;
        return true;
    }
}