package malbec.fer.fix;

import static malbec.fer.CancelRequestTest.generateCancelReplaceRequestFromOrder;
import static malbec.fer.CancelRequestTest.generateCancelRequestFromOrder;
import static malbec.fer.FerretRouterTestHelper.createDatabaseMapper;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import malbec.fer.CancelReplaceRequest;
import malbec.fer.CancelRequest;
import malbec.fer.ITransportableOrder;
import malbec.fer.Order;
import malbec.fer.OrderTest;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;
import malbec.util.InvalidConfigurationException;

import org.testng.annotations.Test;

public class FixDestinationTest extends AbstractFixTest {

    private OrderTest ot = new OrderTest();

    @Test(groups = { "unittest" })
    public void testFixDestinationCreateOrder() throws InvalidConfigurationException {
        System.out.println("Starting 'testFixDestinationCreateOrder'");

        final FixDestination fd = new FixDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), createDatabaseMapper());
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        // Send a valid order
        ITransportableOrder to = fd.createOrder(ot.createStopLimitOrder());
        assertTrue(to.errors().size() == 0, "Converted order has errors");
        assertTrue(to.transport(), "Failed to transport order");

        // Send a invalid order
        to = fd.createOrder(ot.createMarketOrder());
        assertTrue(to.errors().size() > 0, "Order should have an error");
        assertFalse(to.transport(), "Transported an order that contained errors");

        fc.stop();
        waitForLogoff(fc);

        assertFalse(fc.isLoggedOn(), "Logout did not occur on session");
        assertFalse(fc.isRunning(), "FixClient is running, after stop requested");
        System.out.println("Finished 'testFixDestinationStartup'");
    }

    @Test(groups = { "unittest" })
    public void testFixDestinationCancelOrder() throws InvalidConfigurationException {
        System.out.println("Starting 'testFixDestinationCancelOrder'");

        final FixDestination fd = new FixDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), createDatabaseMapper());
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        // Send a valid order
        Order order = ot.createStopLimitOrder();
        ITransportableOrder to = fd.createOrder(order);
        assertTrue(to.errors().size() == 0, "Converted order has errors");
        assertTrue(to.transport(), "Failed to transport order");

        // Send a cancel request
        CancelRequest cr = generateCancelRequestFromOrder(order);
        cr.setOriginalUserOrderId(order.getUserOrderId());
        fd.createCancelOrder(cr);
        assertTrue(to.errors().size() == 0, "Converted cancel request has errors");
        assertTrue(to.transport(), "Failed to transport cancel request");

        fc.stop();
        waitForLogoff(fc);

        assertFalse(fc.isLoggedOn(), "Logout did not occur on session");
        assertFalse(fc.isRunning(), "FixClient is running, after stop requested");
        System.out.println("Finished 'testFixDestinationStartup'");
    }

    @Test(groups = { "unittest" })
    public void testFixDestinationCancelReplaceOrder() throws InvalidConfigurationException {
        System.out.println("Starting 'testFixDestinationCancelReplaceOrder'");

        final FixDestination fd = new FixDestination("Test Session", createInitiatorSession(),
            new EmailSettings(), createDatabaseMapper());
        fd.start();

        final FixClient fc = fd.getFixClient();
        waitForLogon(fc);

        assertTrue(fc.isLoggedOn(), "Session failed to logon");

        // Send a valid order
        Order order = ot.createStopLimitOrder();
        ITransportableOrder to = fd.createOrder(order);
        assertTrue(to.errors().size() == 0, "Converted order has errors");
        assertTrue(to.transport(), "Failed to transport order");

        // Send a cancel request
        CancelReplaceRequest cr = generateCancelReplaceRequestFromOrder(order);
        cr.setOriginalUserOrderId(order.getUserOrderId());

        cr.setQuantity(BigDecimal.valueOf(89));
        fd.createReplaceOrder(cr);
        assertTrue(to.errors().size() == 0, "Converted cancel request has errors");
        assertTrue(to.transport(), "Failed to transport cancel request");

        fc.stop();
        waitForLogoff(fc);

        assertFalse(fc.isLoggedOn(), "Logout did not occur on session");
        assertFalse(fc.isRunning(), "FixClient is running, after stop requested");
        System.out.println("Finished 'testFixDestinationCancelReplaceOrder'");
    }
}
