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

public class TradeStationConversionStrategyTest extends AbstractAggregatorTest
{

    @Test(groups =
    {
        "unittest"
    })
    public void testFuturesConvert () throws Exception
    {
        initializeFix2CmfUtil();

        List<Message> fileMessages = readFixMessagesFromFile("TradeStationExecutions.txt");

        assertTrue(fileMessages.size() > 0);

        DatabaseMapper dbm = new DatabaseMapper(true);
        
        ConversionStrategy cs = new TradeStationConversionStrategy(dbm);

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

            if (count == 1) // BPH8 Futures
            {
                assertEquals(tradeRecord.getBroker(), "RJOBCH");
                assertEquals(tradeRecord.getPrimeBroker(), "GSFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX3577016-0"); // BPH8
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEqualsBD(tradeRecord.getPriceQuote(), "198.54");

                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 2) // TU Futures
            {
                assertEquals(tradeRecord.getBroker(), "RJOBCH");
                assertEquals(tradeRecord.getPrimeBroker(), "GSFUT");
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEquals(tradeRecord.getSecurityId(), "TUH8");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Comdty);
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
                assertEqualsBD(tradeRecord.getPriceQuote(), "106.796875");
            }
            else if (count == 3) // UNM Equity
            {
                assertEquals(tradeRecord.getBroker(), "BEARNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MSPB");
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEquals(tradeRecord.getSecurityId(), "UNM US");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Equity);
                assertEquals(tradeRecord.getSide(), 3);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "EQUITY");
                assertEqualsBD(tradeRecord.getPriceQuote(), "23.13");
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
