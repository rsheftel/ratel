package schedule;

import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static util.Objects.*;

import java.util.*;

import jms.*;
import mail.*;
import tsdb.*;

public abstract class AbstractJobTest extends JMSTestCase {
	static final EmailAddress OTHER = new EmailAddress("other@foos.com");
	static final TimeSeries AAPL = series("aapl close");
	static final TimeSeries AAPL_HIGH = series("aapl high");
	static final SeriesSource AAPL_TEST = TEST_SOURCE.with(AAPL);
	static Map<String, Date> run = emptyMap();
	static final String THREE_PM = "15:00:00";
	static final String FOUR_PM = "16:00:00";

}
