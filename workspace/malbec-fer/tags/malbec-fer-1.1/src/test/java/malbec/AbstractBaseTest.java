package malbec;

import malbec.util.IWaitFor;


public abstract class AbstractBaseTest {

    protected void sleep(long delay) {
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
