package malbec.fer.com;

import malbec.fer.Order;

public interface ComClient {

    boolean sendOrder(Order order);

    String getName();
}
