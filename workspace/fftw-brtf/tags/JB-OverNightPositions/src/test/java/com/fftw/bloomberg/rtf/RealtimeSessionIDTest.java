package com.fftw.bloomberg.rtf;

import org.testng.annotations.*;
import com.fftw.bloomberg.rtf.types.RtfMode;

/**
 * RealtimeSessionID Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created February 11, 2008
 * @since 1.0
 */
public class RealtimeSessionIDTest {

    @Test(groups =
            {
                    "unittest"
                    })
    public void testIdAsString() {
        RealtimeSessionID sessionID = new RealtimeSessionID(1234, 5678, RtfMode.Batch);

        assert "1234-5678-Batch".equals(sessionID.idAsString()) : "failed to create ID as string";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testCreateFromString() {
        RealtimeSessionID sessionID = new RealtimeSessionID();

        sessionID.populateFromString("1246-5689-Online");

        assert "1246-5689-Online".equals(sessionID.idAsString()) : "Failed to create from string";

    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testEquals() {
        RealtimeSessionID sessionID1 = new RealtimeSessionID(1234, 5678, RtfMode.Batch);
        RealtimeSessionID sessionID2 = new RealtimeSessionID(1234, 5678, RtfMode.Batch);

        assert sessionID1.equals(sessionID1) : "Failed equal test";
        assert sessionID1.equals(sessionID2) : "Failed equal test";

        assert sessionID2.equals(sessionID2) : "Failed equal test";
        assert sessionID2.equals(sessionID1) : "Failed equal test";

        RealtimeSessionID sessionID3 = new RealtimeSessionID();

        sessionID3.populateFromString("1234-5678-Batch");
        assert sessionID1.equals(sessionID3) : "Failed equal test";
        assert sessionID2.equals(sessionID3) : "Failed equal test";

        assert sessionID3.equals(sessionID1) : "Failed equal test";
        assert sessionID3.equals(sessionID2) : "Failed equal test";
    }

}
