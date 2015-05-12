package malbec;

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
}
