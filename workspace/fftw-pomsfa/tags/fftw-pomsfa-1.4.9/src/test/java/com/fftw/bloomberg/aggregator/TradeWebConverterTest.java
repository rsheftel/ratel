package com.fftw.bloomberg.aggregator;

import static malbec.util.FerretIntegration.createEmailer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

import malbec.bloomberg.types.BBYellowKey;
import malbec.util.EmailSettings;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import quickfix.Message;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfMessage;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.util.Emailer;

public class TradeWebConverterTest  extends AbstractAggregatorTest
{

    @Test(groups =
    {
        "unittest"
    })
    public void testExecutionConvert () throws Exception
    {
        // TODO add a test for Government
        List<Message> fileMessages = readFixMessagesFromFile("TradewebExecutions.txt");

        assertEquals(fileMessages.size(), 4);

        ConversionStrategy cs = new TradeWebConversionStrategy();

        Emailer emailer = createEmailer(new EmailSettings());
        int count = 0;
        for (Message message : fileMessages)
        {
            count++;
            CmfMessage cmfMessage = cs.convertMessage((ExecutionReport)message, emailer);
            assertNotNull(cmfMessage);
            CmfTradeRecord tradeRecord = cmfMessage.getTradeRecord();
            assertNotNull(tradeRecord);

            if (count == 1) // Futures
            {
                assertEquals(tradeRecord.getBroker(), "BASENY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFPB");
                assertEquals(tradeRecord.getSecurityId(), "01F050411");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Mtge);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 10000000);
                assertEqualsBD(tradeRecord.getPriceQuote(), "103.5625");
                assertEquals(tradeRecord.getSide(), 1);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 2) // Futures
            {
                assertEquals(tradeRecord.getBroker(), "BASENY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFPB");
                assertEquals(tradeRecord.getSecurityId(), "01F050411");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Mtge);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 10000000);
                assertEqualsBD(tradeRecord.getPriceQuote(), "103.5625");
                assertEquals(tradeRecord.getSide(), 1);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 3) // Futures
            {
                assertEquals(tradeRecord.getBroker(), "BASENY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFPB");
                assertEquals(tradeRecord.getSecurityId(), "01F052417");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Mtge);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 7000000);
                assertEqualsBD(tradeRecord.getPriceQuote(), "103.8125");
                assertEquals(tradeRecord.getSide(), 1);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            }
            else if (count == 4) // Futures
            {
                assertEquals(tradeRecord.getBroker(), "BASENY");
                assertEquals(tradeRecord.getPrimeBroker(), "MFPB");
                assertEquals(tradeRecord.getSecurityId(), "01F060410");
                assertEquals(tradeRecord.getProductCode(), BBYellowKey.Mtge);
                assertEqualsBD(new BigDecimal(tradeRecord.getQuantity()), 21000000);
                assertEqualsBD(tradeRecord.getPriceQuote(), "104.218750");
                assertEquals(tradeRecord.getSide(), 2);
                assertEquals(tradeRecord.getTradingStrategy(), "TEST-STRATEGY");
                assertEquals(tradeRecord.getAccount(), "FUTURES");
            } 
        }
    }
    
    
//  TODO implement CDS stuff for TradeWeb
    public void testProcessFpMLMessage () throws Exception
    {
        /*
        String fpmlMessage = getTestMessage();

        DefaultMessageFactory dmf = new DefaultMessageFactory();

        Message fixMessage = MessageUtils.parse(dmf, null, fpmlMessage);

        assertNotNull("failed to read in test file", fixMessage);

        // get the FpML data
        String fpml = fixMessage.getString(EncodedSecurityDesc.FIELD);

        assertNotNull("Failed to extract FpML content", fpml);
//        System.out.println(fpml);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        dbf.setNamespaceAware(false);

        DocumentBuilder parser = dbf.newDocumentBuilder();

        ByteArrayInputStream bais = new ByteArrayInputStream(fpml.getBytes());

        Document doc = parser.parse(bais);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(fpmlMessage.length());
        serialize(doc, baos);

        System.out.println(baos.toString());
         */
    }


    public void serialize(Document doc, OutputStream out) throws Exception {
        OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(doc);
    }

    private String getTestMessage () throws IOException
    {

        InputStream is = getClass().getClassLoader().getResourceAsStream("SampleFIXWithFpML-CDS.txt");

        InputStreamReader isr = new InputStreamReader(is);

        int size = is.available();

        char[] buffer = new char[size];

        isr.read(buffer);
        StringBuffer sb = new StringBuffer(size * 2);

        sb.append(buffer);
        isr.close();

        return sb.toString();
    }
}
