package malbec.fer.processor;

import static org.testng.Assert.*;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.CancelRequest;
import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.MessageUtil;

import org.testng.annotations.Test;

public class OrderProcessorTest {

    private static final class TestTransportableOrder implements ITransportableOrder {
        @Override
        public List<String> errors() {
            return Collections.<String> emptyList();
        }

        @Override
        public boolean transport() {
            return true;
        }
    }

    protected OrderTest ot = new OrderTest();

    @Test(groups = { "unittest", "unittest-op" })
    public void testCreateNewOrder() {
        DatabaseMapper dbm = new DatabaseMapper(true);
        dbm.addAccountMapping("TEST", "TEST.EQUITY", "EQUITY", "TEST-ACCOUNT");
        OrderProcessor op = new OrderProcessor(dbm);
        Map<String, IOrderDestination> orderDestinations = createTestDestinations();

        // Process a valid limit order
        Map<String, String> sourceMessage = ot.createLimitOrder("TEST").toMap();

        Map<String, String> processResults = op.process(sourceMessage, orderDestinations);
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = processResults.get("STATUS");
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status.toUpperCase(), "SENT", "Status not set to 'SENT'");
        assertNotNull(MessageUtil.getUserOrderId(processResults), "Failed to set UserOrderId on result");

        // Process a duplicate order
        processResults = op.process(sourceMessage, orderDestinations);
        String error1 = processResults.get("ERROR_1");
        assertNotNull(error1, "Failed to generate duplicate order entry");
        String status1 = processResults.get("STATUS");
        assertNotNull(status1, "Failed to populate status in return message");
        assertEquals(status1.toUpperCase(), "DUPLICATE", "Status not set to 'DUPLICATE'");
        String userOrderId = MessageUtil.getUserOrderId(processResults);
        assertNotNull(userOrderId, "Failed to set UserOrderId on result");
        assertEquals(userOrderId, MessageUtil.getUserOrderId(sourceMessage),
                "Returned UserOrderId != sent UserOrderId");

        // try to process an order with SQL error
        Order sqlErrorOrder = ot.createLimitOrder("TEST");
        System.err.println("Trying to save order with null SIDE");
        sqlErrorOrder.setSide(null);
        sourceMessage = sqlErrorOrder.toMap();
        try {
            processResults = null;
            processResults = op.process(sourceMessage, orderDestinations);
            assertNull(processResults, "No exceptions thrown");
        } catch (PersistenceException e) {

        }
        assertNull(processResults, "Failed to blowup on processing order");
    }

    protected Map<String, IOrderDestination> createTestDestinations() {
        Map<String, IOrderDestination> destinations = new HashMap<String, IOrderDestination>();
        destinations.put("TEST", new IOrderDestination() {

            @Override
            public ITransportableOrder createCancelOrder(CancelRequest cancelRequest) {
                return new TestTransportableOrder();
            }

            @Override
            public ITransportableOrder createOrder(Order order) {
                order.setAccount("TEST");
                return new TestTransportableOrder();
            }

            @Override
            public ITransportableOrder createReplaceOrder(CancelReplaceRequest crr) {
                return new TestTransportableOrder();
            }

            @Override
            public String getDestinationName() {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
                return null;
            }

            @Override
            public boolean isActiveSession() {
                return true;
            }

            @Override
            public void start() {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }

            }

            @Override
            public void stop() {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }

            }

            @Override
            public List<String> validateOrder(Order order) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }
                return null;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }

            }

            @Override
            public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }

            }

            @Override
            public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
                if (true) {
                    throw new UnsupportedOperationException("Implement me!");
                }

            }

        });

        return destinations;
    }
}
