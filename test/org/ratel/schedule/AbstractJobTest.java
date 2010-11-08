package org.ratel.schedule;

import static org.ratel.tsdb.DataSource.*;
import static org.ratel.tsdb.TimeSeries.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.jms.*;
import org.ratel.mail.*;
import org.ratel.tsdb.*;

public abstract class AbstractJobTest extends JMSTestCase {
    static final EmailAddress OTHER = new EmailAddress("other@foos.com");
    static final TimeSeries AAPL = series("aapl close");
    static final TimeSeries AAPL_HIGH = series("aapl high");
    static final SeriesSource AAPL_TEST = TEST_SOURCE.with(AAPL);
    static Map<String, Date> run = emptyMap();
    static final String THREE_PM = "15:00:00";
    static final String FOUR_PM = "16:00:00";

}
