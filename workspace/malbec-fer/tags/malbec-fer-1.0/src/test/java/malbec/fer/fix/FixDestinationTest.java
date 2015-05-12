package malbec.fer.fix;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import malbec.fer.ITransportableOrder;
import malbec.fer.InvalidConfigurationException;
import malbec.fer.OrderTest;
import malbec.fix.FixClient;
import malbec.util.EmailSettings;

import org.testng.annotations.Test;

public class FixDestinationTest extends AbstractFixTest {

    private OrderTest ot = new OrderTest();
    
    @Test(groups = { "unittest" })
    public void testFixDestinationStartup() throws InvalidConfigurationException {
        System.out.println("Starting 'testFixDestinationStartup'");

        final FixDestination fd = new FixDestination("Test Session", createInitiatorSession(), new EmailSettings());
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

}
