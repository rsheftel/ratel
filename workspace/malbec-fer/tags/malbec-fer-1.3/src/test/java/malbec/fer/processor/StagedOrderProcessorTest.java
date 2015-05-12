package malbec.fer.processor;


import static malbec.fer.FerretState.*;

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import malbec.fer.IOrderDestination;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fer.fix.AbstractFixTest;
import malbec.fer.fix.GoldmanSachsDestination;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.DateTimeUtil;
import malbec.util.EmailSettings;
import malbec.util.MessageUtil;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.Test;

public class StagedOrderProcessorTest extends AbstractProcessorTest {

    private static final class StageTransportableOrder implements ITransportableOrder {
        ITransportableOrder ito;
        
        public StageTransportableOrder(ITransportableOrder ito) {
            this.ito = ito;
        }

        @Override
        public List<String> errors() {
            return ito.errors();
        }

        @Override
        public boolean transport() {
            return true;
        }
    }

    @Test(groups = { "unittest"})
    public void testReleaseStagedRediOrder() {
        DateTimeUtil.freezeTime(new LocalTime(10,0,0,0).toDateTimeToday());
        DatabaseMapper dbm = createDatabaseMapper();
        Map<String, IOrderDestination> orderDestinations = createRediDestinations(dbm);
        String userOrderId = createStagedOrder(dbm, orderDestinations);
        
        // Release the order for today
        StagedOrderProcessor sop = new StagedOrderProcessor(dbm);
        Map<String, String> releaseStagedOrderRequest = new HashMap<String, String>();
        MessageUtil.setReleaseStagedOrder(releaseStagedOrderRequest);
        MessageUtil.setOrderDate(releaseStagedOrderRequest, new LocalDate());
        MessageUtil.setUserOrderId(releaseStagedOrderRequest, userOrderId);
        
        Map<String, String> processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Ticket);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");
        String destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
        
    }
    
    
    private Map<String, IOrderDestination> createRediDestinations(DatabaseMapper dbm) {
        Map<String, IOrderDestination> destinations = new HashMap<String, IOrderDestination>();
        Properties props = AbstractFixTest.createInitiatorSession();
        destinations.put("TEST", new GoldmanSachsDestination("RediTest", props, new EmailSettings(), dbm) {

            /* (non-Javadoc)
             * @see malbec.fer.fix.FixDestination#isActiveSession()
             */
            @Override
            public boolean isActiveSession() {
                return true;
            }

            /* (non-Javadoc)
             * @see malbec.fer.fix.FixDestination#determineAccount(malbec.fer.Order)
             */
            @Override
            protected String determineAccount(Order order) {
                return "TESTACCOUNT";
            }

            /* (non-Javadoc)
             * @see malbec.fer.fix.FixDestination#createOrder(malbec.fer.Order)
             */
            @Override
            public ITransportableOrder createOrder(Order order) {
                ITransportableOrder ito = super.createOrder(order);
                
                return new StageTransportableOrder(ito);
            }
            
        });
        
        return destinations;
    }


    @Test(groups = { "unittest"})
    public void testReleaseStagedOrder() {
        DateTimeUtil.freezeTime(new LocalTime(10,0,0,0).toDateTimeToday());
        DatabaseMapper dbm = createDatabaseMapper();
        Map<String, IOrderDestination> orderDestinations = createTestDestinations();
        String userOrderId = createStagedOrder(dbm, orderDestinations);
        
        // Release the order for today
        StagedOrderProcessor sop = new StagedOrderProcessor(dbm);
        Map<String, String> releaseStagedOrderRequest = new HashMap<String, String>();
        MessageUtil.setReleaseStagedOrder(releaseStagedOrderRequest);
        MessageUtil.setOrderDate(releaseStagedOrderRequest, new LocalDate());
        MessageUtil.setUserOrderId(releaseStagedOrderRequest, userOrderId);
        
        Map<String, String> processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Ticket);
        
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");
        String destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
        

        // Try to release it again
        processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Ticket);
        
        error = processResults.get("ERROR_1");
        assertNotNull(error, "There was an un-expected error processing order: " + error);
        status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "Sent");
        destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
        
        processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Stage);
        
        error = processResults.get("ERROR_1");
        assertNotNull(error, "There was an un-expected error processing order: " + error);
        status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "FerretRejected");
        destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");

        
        // try to release a staged order while we are in staged mode
        userOrderId = createStagedOrder(dbm, orderDestinations);
        assertNotNull(userOrderId);
        processResults = sop.process(releaseStagedOrderRequest, orderDestinations, Stage);
        
        error = processResults.get("ERROR_1");
        assertNotNull(error, "There was an un-expected error processing order: " + error);
        status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "FerretRejected");
        destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
    }

    private String createStagedOrder(DatabaseMapper dbm, Map<String, IOrderDestination> orderDestinations) {
        OrderProcessor op = new OrderProcessor(dbm);

        // Process a valid limit order
        Map<String, String> sourceMessage = OrderTest.createFuturesOrder("TEST").toMap();

        // Stage state
        Map<String, String> processResults = op.process(sourceMessage, orderDestinations, Stage);
        String error = processResults.get("ERROR_1");
        assertNull(error, "There was an un-expected error processing order: " + error);
        String status = MessageUtil.getStatus(processResults);
        assertNotNull(status, "Failed to populate status in return message");
        assertEquals(status, "New");
        String destination = MessageUtil.getDestination(processResults);
        assertEquals(destination, "TICKET");
        
        return MessageUtil.getUserOrderId(sourceMessage);
    }
}
