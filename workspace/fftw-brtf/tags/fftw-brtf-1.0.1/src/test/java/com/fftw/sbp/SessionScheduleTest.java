package com.fftw.sbp;

import org.testng.annotations.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.util.Properties;

/**
 * SessionSchedule Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created February 12, 2008
 * @since 1.0
 */
public class SessionScheduleTest {

    @Test(groups =
            {
                    "unittest"
                    })
    public void testGetStartTime() {
        Properties props = new Properties();

        props.setProperty("session.startTime", "08:00:00 US/Eastern");
        props.setProperty("session.endTime", "23:00:00 US/Eastern");

        props.setProperty("session.startDay", "Monday");
        props.setProperty("session.endDay", "Friday");

        SessionSchedule schedule = new SessionSchedule(props);

        DateTime now = new DateTime();
        DateTime startTime = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                8, 0, 0, 0);

        startTime = startTime.property(DateTimeFieldType.dayOfWeek()).setCopy("Monday");

        assert startTime.equals(schedule.getStartTime()) : "Start time not set correctly";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testGetEndTime() {
        Properties props = new Properties();

        props.setProperty("session.startTime", "08:00:00 US/Eastern");
        props.setProperty("session.endTime", "23:00:00 US/Eastern");

        props.setProperty("session.startDay", "Monday");
        props.setProperty("session.endDay", "Friday");

        SessionSchedule schedule = new SessionSchedule(props);

        DateTime now = new DateTime();
        DateTime endTime = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
              23, 0, 0, 0);

        endTime = endTime.property(DateTimeFieldType.dayOfWeek()).setCopy("Friday");

        assert endTime.equals(schedule.getEndTime()) : "End time not set correctly";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testIsSessionActive() {
        SessionSchedule schedule = new SessionSchedule();

        assert schedule.isSessionActive() : "Failed active session";
    }

}
