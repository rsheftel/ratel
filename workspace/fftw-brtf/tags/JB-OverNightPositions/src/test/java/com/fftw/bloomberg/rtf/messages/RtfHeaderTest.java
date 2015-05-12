package com.fftw.bloomberg.rtf.messages;

import org.testng.annotations.*;
import org.joda.time.LocalDate;
import com.fftw.bloomberg.rtf.types.RtfCommand;

/**
 * RtfHeader Tester.
 */
public class RtfHeaderTest {

    @Test(groups =
            {
                    "unittest"
                    })
    public void testGetProtocolString() {
        String testResult = "4 002/04/08      0000        0000000100000000";
        RtfHeader header = new RtfHeader(RtfCommand.Accept, new LocalDate(2008, 2, 4), 1);

        String message = header.protocolString();
        assert 45 == message.length();
        assert testResult.equals(message);

    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testValueOfOnline() {
        //String sourceAccept = "4 002/04/08      0000        00000002        ";
        String sourceAccept = "4 002/04/08      0000        0000000200000000";

        RtfHeader header = RtfHeader.valueOf(sourceAccept);

        assert sourceAccept.equals(header.protocolString());
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testValueOfStx() {
        byte[] bytes = {0x02};
        String stx = new String(bytes);

        try {
            RtfHeader.valueOf(stx);
            assert false : "STX passed without failure";
        } catch (IllegalArgumentException e) {

        }
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testValueOfShortString() {

        try {
           RtfHeader.valueOf("1 002/05/08");
            assert false : "Parsed short message without failure";
        } catch (IllegalArgumentException e) {

        }
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testValueOfBatch() {
        String sourceAccept = "7 102/04/08txt   0010File    0000000300000000";

        RtfHeader header = RtfHeader.valueOf(sourceAccept);
        assert sourceAccept.equals(header.protocolString());
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testSequence() {
        String sourceOverride = "5 102/04/08      0010        0000000300000000";
        String sourceOverrideMod = "5 102/04/08      0010        0000000400000000";
        RtfHeader header = RtfHeader.valueOf(sourceOverride);

        header.setSequenceNumber(4);

        assert sourceOverrideMod.equals(header.protocolString());
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testStatusCommand() {
        String sourceStatus = "6 102/04/08      0010        0123400100000000";

        RtfHeader header = RtfHeader.valueOf(sourceStatus);

        assert sourceStatus.equals(header.protocolString());

        String sourceStatusMod = "6 102/04/08      0010        0012300200000000";
        header.setPricingAndSiteNumber(123, 2);
        assert sourceStatusMod.equals(header.protocolString());

        try {
            header.setSequenceNumber(987);
            assert false : "Set sequence number on Status command.";
        } catch (IllegalArgumentException e) {

        }
    }
}
