package malbec.fer.rediplus;

import java.util.ArrayList;
import java.util.List;

import malbec.fer.ITransportableOrder;
import malbec.jacob.rediplus.RediPlusOrder;

public class RediPlusTransportableOrder implements ITransportableOrder {

    private RediPlusOrder rediOrder;

    private RediPlusServer rediServer;

    private List<String> currentErrors = new ArrayList<String>();

    public RediPlusTransportableOrder(RediPlusServer rediPlusServer, RediPlusOrder rediOrder,
            List<String> errors) {
        rediServer = rediPlusServer;
        this.rediOrder = rediOrder;
        this.currentErrors = errors;
    }

    @Override
    public List<String> errors() {
        return currentErrors;
    }

    @Override
    public boolean transport() {

        if (currentErrors.size() > 0) {
            return false;
        }
        
        return rediServer.sendOrder(rediOrder, currentErrors);
    }

}
