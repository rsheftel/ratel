package malbec.fer.fix;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import quickfix.Message;

import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.util.OrderValidation;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;

public class FixDestination extends FerFixClientApplication implements IOrderDestination {

    private FixClient fixClient;

    private String name;

    @SuppressWarnings("unused")
    private FixDestination() {
        super(null);
    }

    public FixDestination(String name, Properties config, EmailSettings emailSettings) {
        super(emailSettings);
        fixClient = new FixClient(name, this, config);
        this.name = name;
    }

    
    @Override
    public ITransportableOrder createOrder(Order order) {
        List<String> errors = new ArrayList<String>();
        Message fixMessage = OrderValidation.createFixMessage(order, errors);

        return new FixTransportableOrder(fixClient, errors, fixMessage);
    }

    @Override
    public String getDestinationName() {
        return name;
    }

    @Override
    public List<String> validateOrder(Order order) {
        List<String> errors = new ArrayList<String>();
        OrderValidation.createFixMessage(order, errors);

        return errors;
    }

    public FixClient getFixClient() {
        return fixClient;
    }
    
    @Override
    public void start() {
        fixClient.start();
    }

    @Override
    public void stop() {
        fixClient.stop();
    }

}
