package malbec.fer.rediplus;

public interface RediPlusDestinationMBean {

    void start();
    
    void stop();
    
    String getName();
    
    boolean isConnected();
}
