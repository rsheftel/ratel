package com.fftw.bloomberg.rtf.messages;

import org.testng.annotations.*;
import org.joda.time.LocalDate;
import com.fftw.bloomberg.rtf.types.RtfCommand;

/**
 * RtfMessageFactory Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created February 22, 2008
 * @since 1.0
 */
public class RtfMessageFactoryTest {
    private static final String str1 = "40        2465                 0546     1USD/ZAR1    0          00          0 0               0 0               0 0               0 0               0 0               0   0   0EMF     10USD/ZAR1B2080           20080222            518                                     EM.EEA_ZAR_FX      511                                                FX      510                                               ZAR      239                                          EM_CEMEA                                                                                                                      NNMSPB  ";

    private static final RtfMessageFactory messageFactory = new RtfMessageFactory();

    @Test(groups =
            {
                    "unittest"
                    })
    public void testCreateMessage() {

        RtfHeader header = new RtfHeader(RtfCommand.Data, new LocalDate(), 1);

        RtfMessage message = messageFactory.createMessage(header, str1);

        assert message != null : "Unable to create message";
    }

    @Test
    public void testCreateMessage1() {
        //TODO: Test goes here...
        assert false : "testCreateMessage1 not implemented.";
    }

    @Test
    public void testCreateHeader() {
        //TODO: Test goes here...
        assert false : "testCreateHeader not implemented.";
    }

}
