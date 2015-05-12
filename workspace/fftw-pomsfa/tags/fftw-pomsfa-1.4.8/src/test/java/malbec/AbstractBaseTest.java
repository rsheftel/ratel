package malbec;

import java.math.BigDecimal;

import malbec.util.IWaitFor;

public class AbstractBaseTest
{
    protected static void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // we don't care - we are testing!
        }
    }
    
    protected void waitForValue(IWaitFor<Boolean> waitingFor, boolean expectedValue, long waitDuration) {
        long startedWaitingAt = System.currentTimeMillis();

        while (waitingFor.waitFor() != expectedValue
                && (startedWaitingAt + waitDuration > System.currentTimeMillis())) {
            sleep(10);
        }
    }
    
    /**
     * Compare to <code>BigDecimal</code>s for equality.
     * 
     * This uses <code>compareTo</code> instead of <code>equals</code> so that 1.0 == 1.00
     * 
     * @param actual
     * @param expected
     */
    protected void assertEqualsBD(BigDecimal actual, BigDecimal expected) {

        if (actual.compareTo(expected) != 0) {
            assert false : "expected: <" + expected + "> but was: <" + actual + ">";
        }
    }

    protected void assertEqualsBD(BigDecimal actual, long expected) {
        assertEqualsBD(actual, BigDecimal.valueOf(expected));
    }

    protected void assertEqualsBD(long actual, BigDecimal expected) {
        assertEqualsBD(BigDecimal.valueOf(actual), expected);
    }
    
    protected void assertEqualsBD(BigDecimal actual, String expected) {
        assertEqualsBD(actual, new BigDecimal(expected));
    }

}
