package malbec.fer.fix;

import java.util.ArrayList;
import java.util.List;

import quickfix.Message;

import malbec.fer.ITransportableOrder;
import malbec.fix.FixClient;

public class FixTransportableOrder implements ITransportableOrder {

    
    final private FixClient fixClient;
    
    final private List<String> currentErrors;
    
    final private Message fixMessage;
    
    
    public FixTransportableOrder(FixClient fixClient, List<String> errors, Message fixMessage) {
        this.fixClient = fixClient;
        this.currentErrors = new ArrayList<String>();
        this.fixMessage = fixMessage;
        
        this.currentErrors.addAll(errors);
    }
    
    @Override
    public List<String> errors() {
        return currentErrors;
    }

    @Override
    public boolean transport() {
        // do not transport an order that contains an error
        if (currentErrors.size() > 0) {
            return false;
        }
        return fixClient.sendOrder(fixMessage);
    }

    public Message getFixMessage() {
        return fixMessage;
    }
}
