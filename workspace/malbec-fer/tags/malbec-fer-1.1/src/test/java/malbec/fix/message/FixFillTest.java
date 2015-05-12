package malbec.fix.message;

import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import malbec.fer.mapping.DatabaseMapper;

import org.apache.mina.filter.codec.ProtocolCodecException;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import quickfix.DefaultMessageFactory;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Message.Header;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.BeginString;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecBroker;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityType;
import quickfix.field.SenderCompID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix42.ExecutionReport;
import quickfix.mina.message.FIXMessageDecoder;

public class FixFillTest {

    @BeforeClass(groups = { "unittest", "EMSXDropTest" })
    public void setup() {
        DatabaseMapper dbm = new DatabaseMapper(true);
        FixFillFactory.initialize(dbm);
    }

    @Test(groups = { "unittest" })
    public void convertFix42Equity() {

        ExecutionReport er = new ExecutionReport();

        Header header = er.getHeader();
        long nanoTime = System.nanoTime() / 10000;
        long orderQuantity = 1000;
        long cumulatedQuantity = 100;
        Date testTime = new Date();

        // required
        header.setString(BeginString.FIELD, "FIX.4.2");
        header.setString(SenderCompID.FIELD, "UT_SENDER");
        header.setString(TargetCompID.FIELD, "UT_TARGET");
        header.setString(MsgType.FIELD, "8");
        // do the posdupflag later

        // populate order related fields
        er.set(new Account("TestAccount"));
        er.set(new ClOrdID("CO-" + nanoTime));
        er.set(new OrderID("OI-" + nanoTime));
        er.set(new OrderQty(orderQuantity));
        er.set(new OrdStatus(OrdStatus.NEW));
        er.set(new OrdType(OrdType.LIMIT));
        er.set(new Side(Side.BUY));
        er.set(new Symbol("ZVZZT"));
        er.set(new TimeInForce(TimeInForce.DAY));
        // er.set(new SecurityType(SecurityType.EQUITY)); // There is no EQUITY, if tag is missing == equity
        // er.set(new TradeDate()); // if missing implies today

        // populate execution related fields
        er.set(new AvgPx(9.89));
        er.set(new CumQty(cumulatedQuantity));
        // er.set(new Currency()); default to USD if missing
        er.set(new ExecID("EI-" + nanoTime));
        er.set(new ExecTransType(ExecTransType.NEW)); // CANCEL = '1'; CORRECT = '2'; STATUS = '3'
        er.set(new LastPx(9.89)); // work with the average price
        er.set(new LastShares(100)); // in 4.2 this is LastShares, 4.3 it is LastQty
        er.set(new Price(9.89));
        er.set(new TransactTime(testTime)); // default to now
        er.set(new ExecBroker("MVF"));
        er.set(new ExecType(ExecType.FILL)); // PARTIAL_FILL = '1';FILL = '2';TRADE = 'F';
        er.set(new LeavesQty(orderQuantity - cumulatedQuantity));
        er.set(new SecurityExchange("2"));
        // er.set(new SettlType()); // if missing implies regular
        // er.set(new SettlDate()); // not required for all securities, implied from settlement type

        FixFill fill = FixFillFactory.valueOf(er);

        // Header fields
        assertNotNull(fill, "Failed to convert execution report to FixFill");
        assertEquals(fill.getBeginString(), "FIX.4.2", "Failed to convert protocol version string");
        assertEquals(fill.getSenderCompId(), "UT_SENDER", "Failed to convert SenderCompID");
        assertEquals(fill.getTargetCompId(), "UT_TARGET", "Failed to convert TargetCompID");

        // Order related fields
        assertEquals(fill.getAccount(), "TestAccount", "Failed to convert Account");
        assertEquals(fill.getClientOrderId(), "CO-" + nanoTime, "Failed to convert ClientOrderId");
        assertEquals(fill.getOrderId(), "OI-" + nanoTime, "Failed to convert OrderId");
        assertEquals(fill.getOrderQuantity(), orderQuantity, "Failed to convert OrderQuantity");
        assertEquals(fill.getOrderStatus(), OrdStatus.NEW, "Failed to convert OrderStatus");
        assertEquals(fill.getOrderType(), OrdType.LIMIT, "Failed to convert OrderType");
        assertEquals(fill.getSide(), Side.BUY, "Failed to convert Side");
        assertEquals(fill.getSymbol(), "ZVZZT", "Failed to convert Symbol");
        assertEquals(fill.getTimeInForce(), TimeInForce.DAY, "Failed to convert TIF");

        // Fill related fields
        assertEquals(fill.getAveragePrice(), 9.89, "Failed to convert AveragePrice");
        assertEquals(fill.getCumulatedQuantity(), cumulatedQuantity, "Failed to convert CumulatedQuantity");
        assertEquals(fill.getExecutionId(), "EI-" + nanoTime, "Failed to convert ExecID");
        assertEquals(fill.getExecutionTransactionType(), ExecTransType.NEW,
                "Failed to convert ExecutionTransactionType");
        assertEquals(fill.getLastPrice(), 9.89, "Failed to convert LastPrice");
        assertEquals(fill.getLastShares(), 100, "Failed to convert LastShares");
        assertEquals(fill.getPrice(), 9.89, "Failed to convert Price");
        assertEquals(fill.getTransactionTime(), testTime, "Failed to convert TransactionTime");
        assertEquals(fill.getExecutingBroker(), "MVF", "Failed to convert ExecutionBroker");
        assertEquals(fill.getExecutionType(), ExecType.FILL, "Failed to convert ExecutionType");
        assertEquals(fill.getLeavesQuantity(), (orderQuantity - cumulatedQuantity),
                "Failed to convert LeavesQuantity");
        assertEquals(fill.getSecurityExchange(), "2", "Failed to convert SecurityExchange");

        // Optional fields - all levels
        assertEquals(fill.getPossibleDuplicate(), 'N', "Failed to convert/set PossibleDuplicate");
        assertEquals(fill.getPossibleResend(), 'N', "Failed to convert/set PossibleResend");
        assertEquals(fill.getSecurityType(), "EQUITY", "Failed to convert/set SecurityType");
        assertEquals(fill.getTradeDate(), new LocalDate().toDateMidnight().toDate(),
                "Failed to convet/set TradeDate");
    }

    @Test(groups = { "unittest", "EMSXDropTest" })
    public void testFixFillInsert() throws Exception {
//        List<String> fileMessages = readMessagesFromFile("EmsxDrops.txt");
        List<String> fileMessages = readMessagesFromFile("TestExecutions.txt");
//        List<String> fileMessages = readMessagesFromFile("stripped.txt");
        
        DefaultMessageFactory dmf = new DefaultMessageFactory();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        long testTime = System.nanoTime() / 1000000;

        boolean insertedAll = false;
        Map<String, String> previousExecutionIds = new HashMap<String,String>();
        for (String rawMessage : fileMessages) {
            Message fixMessage = MessageUtils.parse(dmf, null, rawMessage);
            FixFill fill = FixFillFactory.valueOf((ExecutionReport) fixMessage);
            if (fill == null || fill.isOrderAck()) {
                System.err.println("Skipping execution report: " + fixMessage);
                continue;
            }
            
            String currentExecutionId = fill.getExecutionId() + testTime;
            if (previousExecutionIds.containsKey(currentExecutionId)) {
                continue;
            }
            fill.setExecutionId(currentExecutionId);

            previousExecutionIds.put(currentExecutionId, currentExecutionId);
            
            // check the transaction date with the trade date -- should really be the same
            LocalDate tradeDate = new LocalDate(fill.getTradeDate());
            LocalDate transactionDate = new LocalDate(fill.getTransactionTime());

            if (!tradeDate.equals(transactionDate)) {
//                System.err.println("Changing TradeDate to date of TransactionTime.  " + tradeDate + " -> "
//                        + transactionDate);
                fill.setTradeDate(transactionDate.toDateMidnight().toDate());
            }
            
            // Some of this is for mass loading of the record
            if (fill.isPossibleDuplicate()) {
                try {
                    tx.begin();
                    em.persist(fill);
                    tx.commit();
                } catch (Exception e) {
                    // ignoring the duplicate exception
                    tx.rollback();
                }
            } else {
                try {
                    tx.begin();
                    em.persist(fill);
                    tx.commit();
                } catch (Exception e) {
                    System.out.println(fixMessage);
                    throw e;
                }
            }

            
            if (fill.getSecurityId() == null) {
                System.out.println(fixMessage);    
            }
            assertNotNull(fill.getSecurityId(), "Failed to map SecurityId");
            assertNotNull(fill.getSecurityIdSource(), "Failed to map SecurityIdSource");
            assertNotNull(fill.getSecurityType(), "Failed to map SecurityType");

            if (OrdStatus.FILLED == fill.getOrderStatus()) {
                assertEquals(fill.getOrderQuantity(), fill.getCumulatedQuantity(),
                        "Order status is filled, but quantities do not match");
                assertEquals(fill.getLeavesQuantity(), 0, "Filled order has leaves quantity");
            } else if (OrdStatus.PARTIALLY_FILLED == fill.getOrderStatus()) {
                assertEquals(fill.getOrderQuantity(), fill.getCumulatedQuantity() + fill.getLeavesQuantity(),
                        "Partially filled order quantities do not match");
                assertTrue(fill.getOrderQuantity() > fill.getCumulatedQuantity(),
                        "Partially filled order has cumulated more than order");
            } else if (OrdStatus.NEW == fill.getOrderStatus()) {
                assertEquals(fill.getOrderQuantity(), fill.getLeavesQuantity(),
                        "New order has mismatched quantity and leaves");
                assertEquals(fill.getCumulatedQuantity(), 0, "New order has cumulated quantity");
            }

            if (SecurityType.FUTURE.equals(fill.getSecurityType())) {
                assertNotNull(fill.getMaturityMonth(), "Failed to set MaturityMonth on fill");
            }
        }
        insertedAll = true;

        assertTrue(insertedAll, "Failed to insert record");

        em.close();
    }

    @Test(groups = { "unittest" })
    public void testFixFillInsertDuplicate() throws Exception {
        List<String> fileMessages = readMessagesFromFile("DuplicateExecutions.txt");

        DefaultMessageFactory dmf = new DefaultMessageFactory();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        long testTime = System.nanoTime() / 1000000;

        boolean insertedAll = false;
        try {
            for (String rawMessage : fileMessages) {
                Message fixMessage = MessageUtils.parse(dmf, null, rawMessage);
                FixFill fill = FixFillFactory.valueOf((ExecutionReport) fixMessage);
                fill.setExecutionId(fill.getExecutionId() + testTime);

                tx.begin();
                em.persist(fill);
                tx.commit();
            }
            insertedAll = true;
        } catch (EntityExistsException e) {
            insertedAll = false; // need to put something here
        } finally {
            em.close();
        }
        assertFalse(insertedAll, "Inserted duplicate order");
    }

    /**
     * Test the fills that were generated by the RediPlus screen scraper.
     * 
     * @throws Exception
     */
    @Test(groups = { "unittest" })
    public void testFixFillRediToFix() throws Exception {
        List<String> fileMessages = readMessagesFromFile("RediToFixExecutions.txt");

        DefaultMessageFactory dmf = new DefaultMessageFactory();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        long testTime = System.nanoTime() / 1000000;

        boolean insertedAll = false;
        try {
            for (String rawMessage : fileMessages) {
                Message fixMessage = MessageUtils.parse(dmf, null, rawMessage);
                FixFill fill = FixFillFactory.valueOf((ExecutionReport) fixMessage);
                fill.setExecutionId(fill.getExecutionId() + testTime);

                tx.begin();
                em.persist(fill);
                tx.commit();
            }
            insertedAll = true;
        } catch (EntityExistsException e) {
            insertedAll = false; // need to put something here
            e.printStackTrace();
        } finally {
            em.close();
        }
        assertTrue(insertedAll, "Inserted duplicate order");
    }

    private List<String> readMessagesFromFile(String fileName) throws UnsupportedEncodingException,
            IOException, ProtocolCodecException, URISyntaxException {
        FIXMessageDecoder fmd = new FIXMessageDecoder();

        URL url = fmd.getClass().getClassLoader().getResource(fileName);
        List<String> fileMessages = fmd.extractMessages(new File(url.toURI()));
        return fileMessages;
    }
}
