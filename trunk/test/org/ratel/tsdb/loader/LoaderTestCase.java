package org.ratel.tsdb.loader;

import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;

import java.util.*;

import org.ratel.tsdb.*;
import org.ratel.db.*;

public abstract class LoaderTestCase extends DbTestCase {

    protected static final DataSource TEST_SOURCE = new DataSource("test");
    
    protected void requireNoFailures() {
        emailer.requireEmpty();
    }

    protected void assertObservation(String seriesName, Double value, Date date) {
        TimeSeries ts = series(seriesName);
        Observations observations = ts.observations(TEST_SOURCE, range(date));
        assertSize(1, observations);
        assertEquals(value, observations.value(first(observations.times())), 1e-8);
    }
    
    protected void assertNoObservation(String seriesName, Date date) {
        TimeSeries ts = series(seriesName);
        Observations observations = ts.observations(TEST_SOURCE, range(date));
        assertEmpty(observations);
    }
}
