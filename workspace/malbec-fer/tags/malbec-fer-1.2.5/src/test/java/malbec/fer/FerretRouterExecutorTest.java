package malbec.fer;

import static malbec.fer.CancelRequestTest.createCancelReplaceRequestMap;
import static malbec.fer.CancelRequestTest.createCancelRequestMap;
import static malbec.fer.FerretRouterTestHelper.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import malbec.AbstractBaseTest;
import malbec.fer.dao.OrderDao;
import malbec.fer.fix.AbstractFixTest;
import malbec.fer.fix.FixDestination;
import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.EmailSettings;
import malbec.util.IWaitFor;
import malbec.util.MessageUtil;
import malbec.util.TaskService;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import quickfix.examples.executor.Executor;

/**
 * This tests with the QFJ Executor sample application.
 */
public class FerretRouterExecutorTest extends AbstractBaseTest {

    static {
        TaskService.getInstance().createAndAddSingleThreadScheduled("QuickFIXJ");
    }

    IDatabaseMapper dbm = createDatabaseMapper();
    FerretRouter fr = createFerRouterDma(dbm);

    TestMessageProcessorListener mpl = new TestMessageProcessorListener();

    AbstractFixTest fixHarness = new AbstractFixTest() {};
    OrderTest ot = new OrderTest();

    @BeforeMethod(groups = { "executor", "unittest" })
    public void resetMpl() {
        mpl.reset();
    }

    @BeforeClass(groups = { "executor", "unittest" })
    public void startQuickFix() throws Exception {
        // startup the executor in a different thread so we can continue
        ScheduledExecutorService executor = TaskService.getInstance().getExecutor("QuickFIXJ");
        // Send a 'CR' to the stdin to kill this
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Starting executor");
                    Executor.main(new String[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        DatabaseMapper dbm = createDatabaseMapper();

        final FixDestination fix44Dest = new FixDestination(EXECUTOR44, AbstractFixTest
            .createInitiatorSession("FIX.4.4"), new EmailSettings(), dbm);
        fr.addOrderDestination(fix44Dest);

        final FixDestination fix42Dest = new FixDestination(EXECUTOR42, AbstractFixTest
            .createInitiatorSession("FIX.4.2"), new EmailSettings(), dbm);
        fr.addOrderDestination(fix42Dest);

        fr.start();
        fixHarness.waitForLogon(fix42Dest.getFixClient());
        fixHarness.waitForLogon(fix44Dest.getFixClient());
    }

    @AfterClass(groups = { "executor", "unittest" })
    public void stopQuickFix() throws Exception {
        System.out.println("Stopping FIX Server");
        // System.in.read("\n".getBytes());
        fr.stop();
        System.out.println("FIX Server stopped");
    }

    /**
     * If these fail, make sure that the executor is configured to have ResetOnLogon=Y
     * 
     * Do not use JMS for this, as we want to limit the tests to the FIX logic
     * 
     * @throws Exception
     */
    @Test(groups = { "executor", "unittest" })
    public void testCreateOrder44() throws Exception {
        // Send an order that should make it to the execution engine
        // Limit Order
        Order limitOrder = OrderTest.createLimitOrder(EXECUTOR44);
        System.out.println("Sending limit order: " + limitOrder.getUserOrderId());

        Map<String, String> limitOrderMap = limitOrder.toMap();
        MessageUtil.setNewOrder(limitOrderMap);

        fr.processMessage(mpl, limitOrderMap);
        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Order not processes "
            + mpl.getErrorMessageFor(limitOrder.getUserOrderId()));

        waitForOrderAccepted(limitOrder);

        // the execution engine needs to return a pending and then accept message
        OrderDao dao = OrderDao.getInstance();
        Order limit = dao.findOrderByUserOrderId(limitOrder.getUserOrderId(), limitOrder.getOrderDate());
        assertNotNull(limit, "Did not select order by 'UserOrderId' " + limitOrder.getUserOrderId());
        assertNotNull(limit.getOrderID(), "Limit Order not accepted by engine");
    }

    @Test(groups = { "executor", "unittest" })
    public void testCreateOrder42() throws Exception {
        // Send an order that should make it to the execution engine
        // Limit Order
        Order limitOrder = OrderTest.createLimitOrder(EXECUTOR42);
        System.out.println("Sending limit order: " + limitOrder.getUserOrderId());

        Map<String, String> limitOrderMap = limitOrder.toMap();
        MessageUtil.setNewOrder(limitOrderMap);

        fr.processMessage(mpl, limitOrderMap);
        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Order not processes "
            + mpl.getErrorMessageFor(limitOrder.getUserOrderId()));

        waitForOrderAccepted(limitOrder);

        // the execution engine needs to return a pending and then accept message
        OrderDao dao = OrderDao.getInstance();
        Order limit = dao.findOrderByUserOrderId(limitOrder.getUserOrderId(), limitOrder.getOrderDate());
        assertNotNull(limit, "Did not select order by 'UserOrderID' " + limitOrder.getUserOrderId());
        assertNotNull(limit.getOrderID(), "Limit Order not accepted by engine");
    }

    @Test(groups = { "executor", "unittest" })
    public void testFillOrder44() throws Exception {
        // Send an order that should make it to the execution engine
        // Limit Order
        Order limitOrder = OrderTest.createLimitOrder(EXECUTOR44);
        limitOrder.setLimitPrice(BigDecimal.valueOf(20));
        System.out.println("Sending limit order: " + limitOrder.getUserOrderId());

        Map<String, String> limitOrderMap = limitOrder.toMap();
        MessageUtil.setNewOrder(limitOrderMap);

        fr.processMessage(mpl, limitOrderMap);
        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Order not processes "
            + mpl.getErrorMessageFor(limitOrder.getUserOrderId()));

        waitForOrderAccepted(limitOrder);

        // the execution engine needs to return a pending and then accept message
        OrderDao dao = OrderDao.getInstance();
        Order limit = dao.findOrderByUserOrderId(limitOrder.getUserOrderId(), limitOrder.getOrderDate());
        assertNotNull(limit, "Did not select order by 'UserOrderID' " + limitOrder.getUserOrderId());
        assertNotNull(limit.getOrderID(), "Limit Order not accepted by engine");

        waitForOrderFilled(limitOrder);

        Order filledOrder = dao
            .findOrderByUserOrderId(limitOrder.getUserOrderId(), limitOrder.getOrderDate());
        assertEquals(filledOrder.getStatus(), OrderStatus.Filled, "Limit Order not filled by engine");
    }

    @Test(groups = { "executor", "unittest" })
    public void testFillOrder42() throws Exception {
        // Send an order that should make it to the execution engine
        // Limit Order
        Order limitOrder = OrderTest.createLimitOrder(EXECUTOR42);
        // Make sure the generated ClientOrderId worked
        assertNotNull(limitOrder.getClientOrderId(), "ClientOrderId not set");
        limitOrder.setLimitPrice(BigDecimal.valueOf(20));
        System.out.println("Sending limit order: " + limitOrder.getUserOrderId());

        Map<String, String> limitOrderMap = limitOrder.toMap();
        MessageUtil.setNewOrder(limitOrderMap);

        fr.processMessage(mpl, limitOrderMap);
        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Order not processes "
            + mpl.getErrorMessageFor(limitOrder.getUserOrderId()));

        waitForOrderAccepted(limitOrder);

        // the execution engine needs to return a pending and then accept message
        OrderDao dao = OrderDao.getInstance();
        Order limit = dao.findOrderByUserOrderId(limitOrder.getUserOrderId(), limitOrder.getOrderDate());
        assertNotNull(limit, "Did not select order by 'UserOrderID' " + limitOrder.getUserOrderId());
        assertNotNull(limit.getOrderID(), "Limit Order not accepted by engine");

        waitForOrderFilled(limitOrder);

        Order filledOrder = dao
            .findOrderByUserOrderId(limitOrder.getUserOrderId(), limitOrder.getOrderDate());
        assertEquals(filledOrder.getStatus(), OrderStatus.Filled, "Limit Order not filled by engine");
    }

    @Test(groups = { "executor", "unittest" })
    public void testCancelOrder44() throws Exception {
        // Create an order to cancel
        Order orderToCancel = OrderTest.createLimitOrder(EXECUTOR44);

        Map<String, String> orderToCancelMap = orderToCancel.toMap();
        MessageUtil.setNewOrder(orderToCancelMap);

        System.out.println("Sending limit order for cancel: " + orderToCancel.getUserOrderId());
        fr.processMessage(mpl, orderToCancelMap);
        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Order not processes "
            + mpl.getErrorMessageFor(orderToCancel.getUserOrderId()));

        waitForOrderAccepted(orderToCancel);

        CancelRequest cr = new CancelRequest(createCancelRequestMap());
        cr.setOriginalUserOrderId(orderToCancel.getUserOrderId());
        Map<String, String> crMap = cr.toMap();
        MessageUtil.setCancelOrder(crMap);

        System.out.println("Sending cancel request: " + cr.getUserOrderId());
        fr.processMessage(mpl, crMap);

        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Cancel not processes "
            + mpl.getErrorMessageFor(cr.getUserOrderId()));

        waitForOrderAccepted(cr);

        // the execution engine needs to return a pending and then accept message
        OrderDao dao = OrderDao.getInstance();
        Order cancelRequested = dao.findOrderByUserOrderId(cr.getUserOrderId(), cr.getOrderDate());
        assertNotNull(cancelRequested, "Did not select order by 'UserOrderID' " + cr.getUserOrderId());
        assertEquals(cancelRequested.getStatus(), OrderStatus.Accepted, "Cancel Request not accepted");

        Order canceledOrder = dao.findOrderByUserOrderId(cr.getOriginalUserOrderId(), cr.getOrderDate());
        assertNotNull(canceledOrder, "Did not select order by 'ClientOrderID' " + cr.getOriginalUserOrderId());
        assertNotNull(canceledOrder.getOrderID(), "Limit Order not accepted by engine");
        assertEquals(canceledOrder.getStatus(), OrderStatus.Cancelled, "Order not cancelled by engine");
    }

    @Test(groups = { "executor", "unittest" })
    public void testCancelOrder42() throws Exception {
        // Create an order to cancel
        Order orderToCancel = OrderTest.createLimitOrder(EXECUTOR42);

        Map<String, String> orderToCancelMap = orderToCancel.toMap();
        MessageUtil.setNewOrder(orderToCancelMap);

        System.out.println("Sending limit order for cancel: " + orderToCancel.getUserOrderId());
        fr.processMessage(mpl, orderToCancelMap);
        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Order not processes "
            + mpl.getErrorMessageFor(orderToCancel.getUserOrderId()));

        waitForOrderAccepted(orderToCancel);

        CancelRequest cr = new CancelRequest(createCancelRequestMap());
        cr.setOriginalUserOrderId(orderToCancel.getUserOrderId());
        Map<String, String> crMap = cr.toMap();
        MessageUtil.setCancelOrder(crMap);

        System.out.println("Sending cancel request: " + cr.getUserOrderId());
        fr.processMessage(mpl, crMap);

        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Cancel not processes "
            + mpl.getErrorMessageFor(cr.getUserOrderId()));

        waitForOrderAccepted(cr);

        // the execution engine needs to return a pending and then accept message
        OrderDao dao = OrderDao.getInstance();
        Order cancelRequested = dao.findOrderByUserOrderId(cr.getUserOrderId(), cr.getOrderDate());
        assertNotNull(cancelRequested, "Did not select order by 'UserOrderID' " + cr.getUserOrderId());
        assertEquals(cancelRequested.getStatus(), OrderStatus.Accepted, "Cancel Request not accepted");

        Order canceledOrder = dao.findOrderByUserOrderId(cr.getOriginalUserOrderId(), cr.getOrderDate());
        assertNotNull(canceledOrder, "Did not select order by 'UserOrderID' " + cr.getOriginalUserOrderId());
        assertNotNull(canceledOrder.getOrderID(), "Limit Order not accepted by engine");
        assertEquals(canceledOrder.getStatus(), OrderStatus.Cancelled, "Order not cancelled by engine");
    }

    @Test(groups = { "executor", "unittest" })
    public void testCancelReplaceOrder44() throws Exception {
        // Create an order to cancel
        Order orderToCancel = OrderTest.createLimitOrder(EXECUTOR44);

        Map<String, String> orderToCancelMap = orderToCancel.toMap();
        MessageUtil.setNewOrder(orderToCancelMap);

        System.out.println("Sending limit order for cancel replace: " + orderToCancel.getUserOrderId());
        fr.processMessage(mpl, orderToCancelMap);
        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Order not processes "
            + mpl.getErrorMessageFor(orderToCancel.getUserOrderId()));

        waitForOrderAccepted(orderToCancel);

        CancelReplaceRequest crr = new CancelReplaceRequest(createCancelReplaceRequestMap());
        crr.setOriginalUserOrderId(orderToCancel.getUserOrderId());
        Map<String, String> crrMap = crr.toMap();
        MessageUtil.setCancelReplaceOrder(crrMap);

        System.out.println("Sending cancel replace request: " + crr.getUserOrderId());
        fr.processMessage(mpl, crrMap);

        assertEquals(mpl.getMessagesWithErrorCount(), 0, "CancelReplace not processes "
            + mpl.getErrorMessageFor(crr.getUserOrderId()));

        waitForOrderAccepted(crr);

        // the execution engine needs to return a pending and then accept message
        OrderDao dao = OrderDao.getInstance();
        Order cancelRequested = dao.findOrderByUserOrderId(crr.getUserOrderId(), crr.getOrderDate());
        assertNotNull(cancelRequested, "Did not select order by 'UserOrderID' " + crr.getUserOrderId());
        assertEquals(cancelRequested.getStatus(), OrderStatus.Accepted, "CancelReplace Request not accepted");

        // check for the cancel
        Order canceledOrder = dao.findOrderByUserOrderId(crr.getOriginalUserOrderId(), crr.getOrderDate());
        assertNotNull(canceledOrder, "Did not select order by 'UserOrderID' " + crr.getOriginalUserOrderId());
        assertNotNull(canceledOrder.getOrderID(), "Limit Order not accepted by engine");
        assertEquals(canceledOrder.getStatus(), OrderStatus.Cancelled, "Order not cancelled by engine");
    }

    @Test(groups = { "executor", "unittest" })
    public void testCancelReplaceOrder42() throws Exception {
        // Create an order to cancel
        Order orderToCancel = OrderTest.createLimitOrder(EXECUTOR42);

        Map<String, String> orderToCancelMap = orderToCancel.toMap();
        MessageUtil.setNewOrder(orderToCancelMap);

        System.out.println("Sending limit order for cancel replace: " + orderToCancel.getUserOrderId());
        fr.processMessage(mpl, orderToCancelMap);
        assertEquals(mpl.getMessagesWithErrorCount(), 0, "Order not processes "
            + mpl.getErrorMessageFor(orderToCancel.getUserOrderId()));

        waitForOrderAccepted(orderToCancel);

        CancelReplaceRequest crr = new CancelReplaceRequest(createCancelReplaceRequestMap());
        crr.setOriginalUserOrderId(orderToCancel.getUserOrderId());
        Map<String, String> crrMap = crr.toMap();
        MessageUtil.setCancelReplaceOrder(crrMap);

        System.out.println("Sending cancel replace request: " + crr.getUserOrderId());
        fr.processMessage(mpl, crrMap);

        assertEquals(mpl.getMessagesWithErrorCount(), 0, "CancelReplace not processes "
            + mpl.getErrorMessageFor(crr.getUserOrderId()));

        waitForOrderAccepted(crr);

        // the execution engine needs to return a pending and then accept message
        OrderDao dao = OrderDao.getInstance();
        Order cancelRequested = dao.findOrderByUserOrderId(crr.getUserOrderId(), crr.getOrderDate());
        assertNotNull(cancelRequested, "Did not select order by 'UserOrderID' " + crr.getUserOrderId());
        assertEquals(cancelRequested.getStatus(), OrderStatus.Accepted, "CancelReplace Request not accepted");

        // check for the cancel
        Order canceledOrder = dao.findOrderByUserOrderId(crr.getOriginalUserOrderId(), crr.getOrderDate());
        assertNotNull(canceledOrder, "Did not select order by 'UserOrderID' " + crr.getOriginalUserOrderId());
        assertNotNull(canceledOrder.getOrderID(), "Limit Order not accepted by engine");
        assertEquals(canceledOrder.getStatus(), OrderStatus.Cancelled, "Order not cancelled by engine");
    }

    private void waitForOrderAccepted(final Order order) {

        waitForValue(new IWaitFor<Boolean>() {
            OrderDao dao = OrderDao.getInstance();

            @Override
            public Boolean waitFor() {
                Order foundOrder = dao.findOrderByUserOrderId(order.getUserOrderId(), order.getOrderDate());
                return (foundOrder != null && foundOrder.getOrderID() != null);

            }
        }, true, 10000);
    }

    private void waitForOrderFilled(final Order order) {

        waitForValue(new IWaitFor<Boolean>() {
            OrderDao dao = OrderDao.getInstance();

            @Override
            public Boolean waitFor() {
                Order foundOrder = dao.findOrderByUserOrderId(order.getUserOrderId(), order.getOrderDate());
                return (foundOrder != null && foundOrder.getStatus() == OrderStatus.Filled);
            }
        }, true, 10000);
    }
}
