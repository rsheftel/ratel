package com.fftw.bloomberg.aggregator;

import junit.framework.TestCase;

import org.joda.time.DateTime;

public class TestAggregatorSessionStateListener extends TestCase
{

    public void testIsWeekendFriday ()
    {
        AggregatorSessionStateListener assl = new AggregatorSessionStateListener(null, null);

        DateTime friday = new DateTime(2008, 3, 28, 0, 0, 0, 0);
        assertFalse("Is not weekend", assl.isWeekend(friday));
    }

    public void testIsWeekendLateFriday ()
    {
        AggregatorSessionStateListener assl = new AggregatorSessionStateListener(null, null);

        DateTime lateFriday = new DateTime(2008, 3, 28, 22, 0, 0, 0);
        assertTrue("Is weekend", assl.isWeekend(lateFriday));
    }

    public void testIsWeekendSaturday ()
    {
        AggregatorSessionStateListener assl = new AggregatorSessionStateListener(null, null);

        DateTime saturday = new DateTime(2008, 3, 29, 0, 0, 0, 0);
        assertTrue("Is weekend", assl.isWeekend(saturday));
    }

    public void testIsWeekendSunday ()
    {
        AggregatorSessionStateListener assl = new AggregatorSessionStateListener(null, null);

        DateTime sunday = new DateTime(2008, 3, 30, 22, 0, 0, 0);
        assertTrue("Is weekend", assl.isWeekend(sunday));
    }

    public void testIsWeekendEarlyMonday ()
    {
        AggregatorSessionStateListener assl = new AggregatorSessionStateListener(null, null);

        DateTime earlyMonday = new DateTime(2008, 3, 31, 4, 0, 0, 0);
        assertTrue("Is weekend", assl.isWeekend(earlyMonday));
    }

    public void testIsWeekendMonday ()
    {
        AggregatorSessionStateListener assl = new AggregatorSessionStateListener(null, null);

        DateTime monday = new DateTime(2008, 3, 31, 8, 0, 0, 0);
        assertFalse("Is not weekend", assl.isWeekend(monday));
    }

}
