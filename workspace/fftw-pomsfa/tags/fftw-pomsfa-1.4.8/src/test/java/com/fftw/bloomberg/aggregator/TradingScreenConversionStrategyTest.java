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

public class TradingScreenConversionStrategyTest extends AbstractAggregatorTest
{

    @Test(groups =
    {
        "unittest"
    })
    public void testFuturesConvert () throws Exception
    {
        initializeFix2CmfUtil();

        List<Message> fileMessages = readFixMessagesFromFile("TradingScreenExecutions.txt");

        assertEquals(fileMessages.size(), 3);
        DatabaseMapper dbm = new DatabaseMapper(true);
        ConversionStrategy cs = new TradingScreenConversionStrategy(dbm);

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

            
            if (count == 1) // Futures
            {
                assertEquals(tradeRecord.getBroker(), "MANFNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX5765435-0"); //JYM9
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEqualsBD(tradeRecord.getPriceQuote(), "0.0001");
                assertEquals(tradeRecord.getSide(), 1);
                assertEquals(tradeRecord.getTradingStrategy(), "QF.NDayBreak");
                assertEquals(tradeRecord.getAccount(), "QMF");
            }
            else if (count == 2) // Futures
            {
                assertEquals(tradeRecord.getBroker(), "MANFNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX5765435-0"); // TYM9
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEqualsBD(tradeRecord.getPriceQuote(), new BigDecimal("0.0001"));
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 3) // Equity
            {
                assertEquals(tradeRecord.getBroker(), "MANFNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFPB");
                assertEquals(tradeRecord.getSecurityId(), "MSFT US");
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 10300);
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "EQUITY");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Equity);
                assertEqualsBD(tradeRecord.getPriceQuote(), "92");
            }
            else if (count == 6)
            {
                assertEquals(tradeRecord.getBroker(), "MANFNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFFUT");
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                //assertEquals(tradeRecord.getSecurityId(), "JYM9");
                assertEquals(tradeRecord.getSecurityId(), "JY");
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
//                assertEquals(tradeRecord.getProductCode(), BBProductCode.Currency);
                assertEqualsBD(tradeRecord.getPriceQuote(), "0.000004");
            }
            else if (count == 7) // FX
            {
                message.setString(58, "FXS");
                System.err.println(message);

                assertEquals(tradeRecord.getBroker(), "MANFNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFFUT");
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1000000);
                assertEquals(tradeRecord.getSecurityId(), "USD/CAD");
                
                assertEquals(tradeRecord.getSide(), 1);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FX");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(tradeRecord.getPriceQuote(), "1.0005");
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
