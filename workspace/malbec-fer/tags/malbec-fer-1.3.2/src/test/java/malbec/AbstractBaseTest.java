package malbec;

import java.math.BigDecimal;
import java.util.List;

import malbec.util.DateTimeUtil;
import malbec.util.IWaitFor;

import org.testng.annotations.AfterMethod;


public abstract class AbstractBaseTest {

    @AfterMethod(groups = { "unittest" })
    public void thawTime() {
        DateTimeUtil.thawTime();
    }
    
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
    public void assertEqualsBD(BigDecimal actual, BigDecimal expected) {

        if (actual.compareTo(expected) != 0) {
            assert false : "expected: <" + expected + "> but was: <" + actual + ">";
        }
    }

    public void assertEqualsBD(BigDecimal actual, long expected) {
        assertEqualsBD(actual, BigDecimal.valueOf(expected));
    }

    public void assertEqualsBD(BigDecimal actual, double expected) {
        assertEqualsBD(actual, BigDecimal.valueOf(expected));
    }

    public void assertEqualsBD(long actual, BigDecimal expected) {
        assertEqualsBD(BigDecimal.valueOf(actual), expected);
    }

    @SuppressWarnings("unchecked")
    public void assertNotEquals(Object a, Object b) {
        if (a instanceof Comparable<?> && b instanceof Comparable<?>) {
            Comparable aa = (Comparable) a;
            Comparable bb = (Comparable) b;
            assert (aa.compareTo(bb) != 0) : "expected: <" + aa + "> equals actual: <" + bb + ">";
            return;
        }
        
        if (a != null) {
            assert (!a.equals(b)) : "expected: <" + a + "> equals actual: <" + b + ">";
        } else if (b != null) {
            assert (!b.equals(a)) : "expected: <" + a + "> equals actual: <" + b + ">";
        }
        
        assert a != b : "expected: <" + a + "> equals actual: <" + b + ">";
    }
    
    protected String listError(List<String> errors) {
        if (errors != null && errors.size() > 0) {
            return errors.get(0);
        }
        
        return "";
    }
}
