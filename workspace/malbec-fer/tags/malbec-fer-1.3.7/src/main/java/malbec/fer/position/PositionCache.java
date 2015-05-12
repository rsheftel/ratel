package malbec.fer.position;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import malbec.jms.DefaultTextMessageProcessor;
import malbec.jms.DestinationHandler;

public class PositionCache {

    
    private Map<String, BigDecimal> cache = new HashMap<String, BigDecimal>();

    private Connection brokerConnection;

    private String primeBroker;

    private DestinationHandler dh;

    private static PositionCache _instance = new PositionCache();
    
    public static PositionCache getInstance() {
        return _instance;
    }

    private PositionCache() {
        // we are a singleton - do not allow creation
    }
    
    public synchronized BigDecimal positionFor(String primeBroker, String ticker) {

        String key = createCacheKey(primeBroker, ticker);

        BigDecimal position = cache.get(key);

        if (position == null) {
            return BigDecimal.ZERO;
        }

        return position;
    }

    public synchronized BigDecimal updatePosition(String primeBroker, String ticker, BigDecimal newPosition) {
        String key = createCacheKey(primeBroker, ticker);

        return cache.put(key, newPosition);
    }

    public synchronized BigDecimal updatePosition(String primeBroker, String ticker, long newPosition) {
        return updatePosition(primeBroker, ticker, BigDecimal.valueOf(newPosition));
    }

    private String createCacheKey(String primeBroker, String ticker) {
        return primeBroker.toUpperCase() + "-" + ticker.toUpperCase();
    }

    public synchronized int count() {
        return cache.size();
    }

    public synchronized BigDecimal purgePosition(String primeBroker, String ticker) {
        String key = createCacheKey(primeBroker, ticker);

        return cache.remove(key);
    }

    public synchronized Collection<BigDecimal> positions() {
        return cache.values();
    }

    public synchronized void purge() {
        cache.clear();
    }

    public void setBrokerConnection(Connection connection) {
        brokerConnection = connection;
    }

    public void start() throws JMSException {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Positions.PrimeBroker.QMF.");
        sb.append(primeBroker).append(".EQUITY.>");

        dh = new DestinationHandler(brokerConnection, sb.toString());
        dh.setTextMessageProcessor(new DefaultTextMessageProcessor() {

            @Override
            protected void onTextMessage(TextMessage textMessage, Map<String, String> mapMessage) {
                String primeBroker = mapMessage.get("PRIMEBROKER");
                String ticker = mapMessage.get("TICKER");
                BigDecimal position = new BigDecimal(mapMessage.get("CURRENTPOSITION"));

                updatePosition(primeBroker, ticker, position);
            }
        });
        dh.start();
    }

    public void setPrimeBroker(String primeBroker) {
        this.primeBroker = primeBroker;
    }

    public Connection getBrokerConnection() {
        return brokerConnection;
    }

    public void stop() throws JMSException {
        if (dh != null) {
            dh.stop();
        }
    }

}
