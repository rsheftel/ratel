package malbec.fer;

import quickfix.Message;

public interface IOrderStatusObserver {

    String orderAccepted(Message fixMessage);
}
