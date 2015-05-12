package malbec.hawkeye;

import static malbec.jms.DestinationHandlerTest.*;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import malbec.fer.Order;
import malbec.hawkeye.HawkEyeConfiguration;
import malbec.hawkeye.Main;
import malbec.jms.AbstractJmsBaseTest;
import malbec.util.DateTimeUtil;
import malbec.util.MessageUtil;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import systemdb.metadata.LiveOrders.LiveOrder;
import util.Dates;

public class MainTest extends AbstractJmsBaseTest {

    @Test(groups = { "unittest" })
    public void testReadNewOrderFromSources() {
        // We must freeze both frameworks
        freezeTime("2009/03/31 10:00:00");

        Main main = new Main();

        main.dailyInit();
        assertTrue(main.getTomahawkMaxId() > 0);
        assertTrue(main.getFerretMaxId() > 0);

        // Tomahawk orders
        List<LiveOrder> tomahawkOrders = main.queryTomahawk();
        assertTrue(tomahawkOrders.size() > 0);

        // Ferret Orders
        List<Order> ferretOrders = main.queryFerret();
        assertTrue(ferretOrders.size() > 0);

        // TODO This will probably have to be changes, as the orders will grow over time
        limitTomahawkToDate(new DateTime(), tomahawkOrders);
        limitFerretToDate(new DateTime(), ferretOrders);
        Order removedFerretOrder = ferretOrders.remove(0);
        LiveOrder removedTomahawkOrder = removeMatchingOrderFromTomahawk(tomahawkOrders, removedFerretOrder
            .getUserOrderId());
        assertTrue(main.ordersMatch(tomahawkOrders, ferretOrders));

        // Make them different
        tomahawkOrders.add(removedTomahawkOrder);
        assertFalse(main.ordersMatch(tomahawkOrders, ferretOrders));
        assertEquals(main.getExtraFerretOrders().size(), 0);
        assertEquals(main.getExtraTomahawkOrders().size(), 1);

        // Add the missing order
        ferretOrders.add(removedFerretOrder);
        assertTrue(main.ordersMatch(tomahawkOrders, ferretOrders));
        assertEquals(main.getExtraFerretOrders().size(), 0);
        assertEquals(main.getExtraTomahawkOrders().size(), 0);
    }

    @Test(groups = { "unittest" })
    public void testReceivedStatus() {
        Main main = new Main();
        Map<String, String> mapMessage = new HashMap<String, String>();
        MessageUtil.setStatus(mapMessage, "Sent");

        main.setFerretState("Stage");
        assertFalse(main.shouldFerretBeSending());
        assertFalse(main.validateStatusMessageMain(mapMessage));

        main.setFerretState("Ticket");
        assertTrue(main.shouldFerretBeSending());
        assertTrue(main.validateStatusMessageMain(mapMessage));
        
    }
    
    private void limitFerretToDate(DateTime dateTime, List<Order> ferretOrders) {
        List<Order> orderToRemove = new ArrayList<Order>();

        Date local = dateTime.toDate();
        for (Order order : ferretOrders) {
            if (order.getCreatedAt().after(local)) {
                orderToRemove.add(order);
            }
        }

        ferretOrders.removeAll(orderToRemove);
    }

    private void limitTomahawkToDate(DateTime dateTime, List<LiveOrder> tomahawkOrders) {
        List<LiveOrder> orderToRemove = new ArrayList<LiveOrder>();

        Date local = dateTime.toDate();
        for (LiveOrder order : tomahawkOrders) {
            if (order.isFerret() && order.submittedTime().after(local)) {
                orderToRemove.add(order);
            }
        }

        tomahawkOrders.removeAll(orderToRemove);
    }

    @Test(groups = { "unittest" })
    public void testReadOrderStatusAndFerretState() throws JMSException {
        HawkEyeConfiguration config = new HawkEyeConfiguration(BROKER_URL, "Test.FER.Order.Response", "Test.FER.State");
        
        Main main = new Main(config);
        
        main.startTopicHandlers();

        assertTrue(main.isListeningToFerret());
        publishFerretStateMessage(main.getBrokerConnection(), config.getFerretStateTopic(), "Stage");
        sleep(100);
        assertEquals(main.getFerretState(), "Stage");
    }

    private LiveOrder removeMatchingOrderFromTomahawk(List<LiveOrder> tomahawkOrders, String userOrderId) {
        for (LiveOrder order : tomahawkOrders) {
            if (order.isFerret() && order.ferretOrderId().equals(userOrderId)) {
                tomahawkOrders.remove(order);
                return order;
            }
        }
        return null;
    }

    private void freezeTime(String freezeTime) {
        DateTimeUtil.freezeTime(freezeTime);
        Dates.freezeNow(freezeTime);

        // check that we have the same time
        Date myDate = new DateTime().toDate();
        Date theirDate = Dates.now();

        if (myDate.getTime() != theirDate.getTime()) {
            System.out.println("Different");
        }
    }
}
