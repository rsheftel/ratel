package com.fftw.bloomberg.aggregator;

import static malbec.util.FerretIntegration.createEmailer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.List;

import malbec.util.EmailSettings;

import org.testng.annotations.Test;

import quickfix.Message;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.util.Emailer;

public class EmsxConversionStrategyTest extends AbstractAggregatorTest
{

    @Test(groups =
    {
        "unittest"
    })
    public void testExecutionConvert () throws Exception
    {
        List<Message> fileMessages = readFixMessagesFromFile("EmsxExecutions.txt");

        assertEquals(fileMessages.size(), 68);

        ConversionStrategy cs = new EmsxConversionStrategy();

        Emailer emailer = createEmailer(new EmailSettings());
        int count = 0;
        for (Message message : fileMessages)
        {
            count++;
            CmfMessage cmfMessage = cs.convertMessage((ExecutionReport)message, emailer);
            // We don't need to send these back to Bloomberg, so this should always be null
            assertNull(cmfMessage);
        }
    }
}
