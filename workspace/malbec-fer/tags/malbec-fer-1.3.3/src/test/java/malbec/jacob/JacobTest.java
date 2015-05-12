package malbec.jacob;

import static malbec.fer.rediplus.RediExchange.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import static malbec.jacob.JacobUtil.createRefVariant;

import java.io.File;
import java.math.BigDecimal;

import malbec.fer.rediplus.RediExchange;
import malbec.jacob.rediplus.RediPlusCacheControl;
import malbec.jacob.rediplus.RediPlusOrder;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.jacob.com.ComThread;
import com.jacob.com.Variant;

public class JacobTest {

    public static void main(String[] args) {
        JacobTest test = new JacobTest();
        test.testRediOrderPropertiesDemo();
    }

    @BeforeTest(groups = { "redi" })
    public void setIntitalizeJacob() {
        File dir = new File("./");
        File path = new File(dir, "target/lib");
        System.setProperty("java.library.path", path.getAbsolutePath());
        System.out.println("java.library.path=" + System.getProperty("java.library.path"));
        ComThread.InitSTA();
    }

    @Test(groups = { "unittest-disabled" })
    public void testComLoad() {
    /*
     * AbstractBaseCom abc = new AbstractBaseCom() {
     * 
     * @Override protected String getProgramID() { // This needs to be the ProgID for any valid COM Server
     * return "REDI.ORDER"; } };
     * 
     * abc.quit();
     */
    }

    @Test(groups = { "redi" })
    public void testRediOrderPropertiesDemo() {
        //ComThread.InitSTA();
        // Create the COM client
        RediPlusOrder order = new RediPlusOrder();

        // set the exchange first as this drives the possible values
        order.setExchange(TICKET);
        RediExchange ret = order.getExchange();
        assertEquals(ret, TICKET, "Failed to retrieve exchange");
        
        order.setExchange(SIGMA);
        RediExchange res = order.getExchange();
        assertEquals(res, SIGMA, "Failed to retrieve exchange");

        order.setExchange(DEMO);
        RediExchange re = order.getExchange();
        assertEquals(re, DEMO, "Failed to retrieve exchange");
        
        testPropertySide(order);

        order.setSymbol("ZVZZT");
        String symbol = order.getSymbol();
        assertEquals(symbol, "ZVZZT", "Failed to retrieve symbol");

        testPropertyExchange(order);

        // This only works during market hours
        // order.setTIF("Day");
        // String tif = order.getTIF();
        // assertEquals(tif, "Day", "Failed to retrieve TIF");

        order.setQuantity(new BigDecimal("12"));
        BigDecimal quantity = order.getQuantity();
        assertEquals(quantity, new BigDecimal("12"), "Failed to retrieve quantity");

        // Display quantity is the quantity or less
        order.setDisplayQuantity(new BigDecimal("13"));
        BigDecimal displayQuantity = order.getDisplayQuantity();
        assertEquals(displayQuantity, quantity, "Failed to retrieve displayQuantity");

        order.setDisplayQuantity(new BigDecimal("11"));
        BigDecimal lessThanDisplayQuantity = order.getDisplayQuantity();
        assertEquals(lessThanDisplayQuantity, new BigDecimal("11"), "Failed to retrieve displayQuantity");

        order.setPrice(new BigDecimal("9.45"));
        BigDecimal price = order.getPrice();
        assertEquals(price, new BigDecimal("9.45"), "Failed to retrieve price");

        order.setStopPrice(new BigDecimal("9.40"));
        BigDecimal stopPrice = order.getStopPrice();
        assertEquals(stopPrice, new BigDecimal("9.40"), "Failed to retrieve stopPrice");

        order.setMemo("Unit Test memo");
        String memo = order.getMemo();
        assertEquals(memo, "Unit Test memo", "Failed to retrieve memo");

        order.setAccount("UnitTest");
        String account = order.getAccount();
        assertEquals(account, "UNITTEST", "Failed to retrieve account");

        order.setPassword("UnitTest");
        String password = order.getAccount();
        assertEquals(password, "UNITTEST", "Failed to retrieve password");

    }

    @Test(groups = { "redi" })
    public void testRediOrderSend() {
        // Create the COM client
        RediPlusOrder order = new RediPlusOrder();
        order.setSide("BUY");
        order.setSymbol("ZVZZT");
        order.setExchange(TICKET);
        order.setQuantity(1);
        order.setPrice(new BigDecimal("9.54"));
        order.setMemo("UnitTest: Test order");
        order.setAccount("5CX19915");
        order.setPassword("mal200");
        order.setClientData("UT-" + System.nanoTime());

        StringBuilder sb = new StringBuilder();

        Boolean warning = order.getWarning();
        System.out.println(warning);
        
        order.setWarning(false);

        boolean submittedOrder = order.submit(sb);

        assertTrue(submittedOrder, "Failed to submit order " + sb.toString());

        warning = order.getWarning();
        System.out.println(warning);

    }

    @Test(groups = { "unittest-disabled" })
    public void testRediQuery() {
        // Create the COM client
        RediPlusCacheControl cc = new RediPlusCacheControl();
        cc.setPassword("mal200");

        Object o = cc.submit(new Variant("Message"), new Variant("true"), createRefVariant());
        System.out.println("Returned value is: " + o);
    }

    void testPropertiesThatFail(RediPlusOrder order) {
        // TODO add this back once we figure out what it should be and if it is necessary

        order.setUserID("TestUserID");
        String userID = order.getUserID();
        assertEquals(userID, "TestUserID", "Failed to retrieve userID");

    }

    private void testPropertyExchange(RediPlusOrder order) {
        // exchange defaults to SIGMA
        order.setExchange(DEMO);
        RediExchange demoExchange = order.getExchange();
        assertEquals(demoExchange, DEMO, "Failed to retrieve exchange");

        order.setExchange(SIGMA);
        RediExchange sigmaExchange = order.getExchange();
        assertEquals(sigmaExchange, SIGMA, "Failed to retrieve exchange");
    }

    private void testPropertySide(RediPlusOrder order) {
        // Test the fields that we need
        order.setSide("Buy");
        String buySide = order.getSide();

        assertEquals(buySide, "BUY", "Failed to retrieve side");

        order.setSide("SELL");
        String sellSide = order.getSide();
        assertEquals(sellSide, "SELL", "Failed to retrieve side");

        order.setSide("SELL SHORT");
        String sellShortSide = order.getSide();
        assertEquals(sellShortSide, "SELL SHORT", "Failed to retrieve side");
    }
}
