package malbec.util;

import static org.testng.Assert.*;
import static malbec.fer.FerretState.*;

import malbec.fer.FerretState;

import org.testng.annotations.Test;

public class ScheduleStateTest {

    @Test(groups = { "unittest" })
    public void testOrder() {
        FerretState[] testArray1 = new FerretState[] { Reject, Active };
        assertEquals(FerretState.highest(testArray1), Reject);

        FerretState[] testArray2 = new FerretState[] { Active, Reject, Stage };
        assertEquals(FerretState.highest(testArray2), Stage);

        FerretState[] testArray3 = new FerretState[] { Active, Ticket, Stage, Reject };
        assertEquals(FerretState.highest(testArray3), Ticket);

        FerretState[] testArray4 = new FerretState[] { Stage, DMA, Active, Ticket, Reject };
        assertEquals(FerretState.highest(testArray4), DMA);

        FerretState[] testArray5 = new FerretState[] { Stage, DMA, Inactive, Ticket, Reject };
        assertEquals(FerretState.highest(testArray5), Inactive);

        try {
            FerretState[] testArray6 = new FerretState[] { Active, Stage, DMA, Inactive, Ticket, Reject };
            FerretState.highest(testArray6);
            fail();
        } catch (IllegalArgumentException e) {

        }

    }
}
