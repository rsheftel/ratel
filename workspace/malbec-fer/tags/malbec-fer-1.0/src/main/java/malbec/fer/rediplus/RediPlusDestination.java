package malbec.fer.rediplus;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.jacob.rediplus.RediPlusOrder;
import malbec.util.EmailSettings;

public class RediPlusDestination implements IOrderDestination {

    private String name;

    private RediPlusServer rediPlusServer;

    public RediPlusDestination(String name, Properties config, EmailSettings emailSettings) {
        this.name = name;
        rediPlusServer = new RediPlusServer(name, config, emailSettings);
    }

    @Override
    public ITransportableOrder createOrder(Order order) {
        List<String> errors = new ArrayList<String>();

        RediPlusOrder rpo = rediPlusServer.createOrder(order, errors);

        return new RediPlusTransportableOrder(rediPlusServer, rpo, errors);
    }

    @Override
    public String getDestinationName() {
        return name;
    }

    @Override
    public void start() {
    // no-op
    }

    @Override
    public void stop() {
    // no-op
    }

    @Override
    public List<String> validateOrder(Order order) {
        List<String> errors = new ArrayList<String>();
        rediPlusServer.createOrder(order, errors);

        return errors;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // no-op
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // no-op
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // no-op
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // no-op
    }

}
