package com.fftw.bloomberg.aggregator;

import static malbec.util.FerretIntegration.createEmailer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.EmailSettings;

import org.testng.annotations.Test;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.util.Emailer;

public class PassportnConversionStrategyTest extends AbstractAggregatorTest
{

    @Test(groups =
    {
        "unittest"
    })
    public void testFuturesConvert () throws Exception
    {
        initializeFix2CmfUtil();

        List<Message> fileMessages = readFixMessagesFromFile("PassportExecutions.txt");

        assertTrue(fileMessages.size() > 0);
        DatabaseMapper dbm = new DatabaseMapper(true);
        ConversionStrategy cs = new PassportConversionStrategy(dbm);

        Emailer emailer = createEmailer(new EmailSettings());
        int count = 0;
        for (Message message : fileMessages)
        {
            assertTrue(isValidExecution(message));
            count++;

            CmfMessage cmfMessage = cs.convertMessage((ExecutionReport)message, emailer);
            assertNotNull(cmfMessage);
            CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
            assertNotNull(tradeRecord);

            if (count == 1) // ECM8 Futures
            {
                assertEquals(tradeRecord.getBroker(), "MOGSNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MSFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX3954023-0"); // ECM8
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEqualsBD(tradeRecord.getPriceQuote(), "1.5513");
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 2) // SFM8 Futures
            {
                assertEquals(tradeRecord.getBroker(), "MOGSNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MSFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX3954015-0"); // SFM8
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEqualsBD(tradeRecord.getPriceQuote(), "98.62");
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 3) // BPM8 Futures
            {
                assertEquals(tradeRecord.getBroker(), "MOGSNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MSFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX3954043-0"); // BPM8
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 19);
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(tradeRecord.getPriceQuote(), "201.33");
            }
            else if (count == 4) // CDM8 Futures
            {
                assertEquals(tradeRecord.getBroker(), "MOGSNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MSFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX3956104-0"); // CDM8
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 4);
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
                
                assertEqualsBD(tradeRecord.getPriceQuote(), "101.22");
            }
            else if (count == 5) // JYM8 Futures
            {
                assertEquals(tradeRecord.getBroker(), "MOGSNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MSFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX3954063-0"); //JYM8
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 5);
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
                assertEqualsBD(tradeRecord.getPriceQuote(), "100.34");
            }
            else if (count == 6) // CI Equity
            {
                assertEquals(tradeRecord.getBroker(), "MOGSNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MSPB");
                assertEquals(tradeRecord.getSecurityId(), "CI US");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Equity);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 100);
                assertEquals(tradeRecord.getSide(), 3);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "EQUITY");
                assertEqualsBD(tradeRecord.getPriceQuote(), "47.93");
            }


        }
    }

    private boolean isValidExecution (Message message)
    {
        try
        {
            ExecType execType = new ExecType(message.getChar(ExecType.FIELD));
            OrdStatus orderStatus = new OrdStatus(message.getChar(OrdStatus.FIELD));
            // All of the sources are sending Fills and partial fills
            return ((execType.getValue() == ExecType.FILL || execType.getValue() == ExecType.PARTIAL_FILL) && orderStatus
                .getValue() != OrdStatus.NEW);
        }
        catch (FieldNotFound e)
        {
            e.printStackTrace();
        }

        return false;
    }
}
