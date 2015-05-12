package com.fftw.bloomberg.aggregator;

import static malbec.util.FerretIntegration.createEmailer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.EmailSettings;

import org.testng.annotations.Test;

import quickfix.Message;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.util.Emailer;

public class GoldmanConversionStrategyTest extends AbstractAggregatorTest
{

    @Test(groups =
    {
        "unittest"
    })
    public void testExecutionConvert () throws Exception
    {
        List<Message> fileMessages = readFixMessagesFromFile("RediExecutions.txt");

        assertEquals(fileMessages.size(), 4);

        DatabaseMapper dbm = new DatabaseMapper(true);
        
        ConversionStrategy cs = new RediConversionStrategy(dbm);

        Emailer emailer = createEmailer(new EmailSettings());
        int count = 0;
        for (Message message : fileMessages)
        {
            count++;
            CmfMessage cmfMessage = cs.convertMessage((ExecutionReport)message, emailer);
            assertNotNull(cmfMessage);
            CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
            assertNotNull(tradeRecord);

            if (count == 1) // TYH9 Comdty 
            {
                assertEquals(tradeRecord.getBroker(), "GOLDNY");
                assertEquals(tradeRecord.getPrimeBroker(), "GSFUT");
                assertEquals(tradeRecord.getSecurityId(), "TYH9");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Comdty);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEqualsBD(tradeRecord.getPriceQuote(), "124.0625");  // 124.0625 * 1
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 2) // ADM9 Curncy
            {   // Currencies have an additional translation
                assertEquals(tradeRecord.getBroker(), "GOLDNY");
                assertEquals(tradeRecord.getPrimeBroker(), "GSFUT");
                assertEquals(tradeRecord.getSecurityId(), "IX5765447-0");  // ADM9
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Curncy);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 5);
                assertEqualsBD(tradeRecord.getPriceQuote(), "72.68"); // 0.726800 * 100
                assertEquals(tradeRecord.getSide(), 1);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 3) // ESM9 Index
            {
                assertEquals(tradeRecord.getBroker(), "GOLDNY");
                assertEquals(tradeRecord.getPrimeBroker(), "GSFUT");
                assertEquals(tradeRecord.getSecurityId(), "ESM9");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Index);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 1);
                assertEqualsBD(tradeRecord.getPriceQuote(), "887.25");  // 887.250000 * 1
                assertEquals(tradeRecord.getSide(), 1);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 4) // XLP Equity
            {
                assertEquals(tradeRecord.getBroker(), "GOLDNY");
                assertEquals(tradeRecord.getPrimeBroker(), "MSPB");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Equity);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 300);
                assertEqualsBD(tradeRecord.getPriceQuote(), "23.96");
                assertEquals(tradeRecord.getSecurityId(), "XLP US");
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "EQUITY");
            }
        }
    }
}
