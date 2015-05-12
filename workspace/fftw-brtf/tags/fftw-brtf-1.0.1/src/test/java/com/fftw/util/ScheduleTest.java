package com.fftw.util;

import org.testng.annotations.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.util.Properties;

import com.fftw.sbp.SessionSchedule;

/**
 * Schedule Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created March 12, 2008
 * @since 1.0
 */
public class ScheduleTest {


    @Test(groups =
            {
                    "unittest"
                    })
    public void testInitializeFromPropertiesDefaultWeekend() {

        Properties props = new Properties();

        props.setProperty("startTime", "08:00 US/Eastern");
        props.setProperty("endTime", "22:00 US/Eastern");

        props.setProperty("startDay", "Monday");
        props.setProperty("endDay", "Friday");

        Schedule schedule = new Schedule(props);

        DateTime now = new DateTime();
        DateTime startTime = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                7, 0, 0, 0);

        startTime = startTime.property(DateTimeFieldType.dayOfWeek()).setCopy("Monday");

        assert !schedule.isTimeIncluded(startTime.getMillis()) : "Schedule not initialized correctly - time before schedule";

        startTime = startTime.property(DateTimeFieldType.hourOfDay()).setCopy(8);
        assert schedule.isTimeIncluded(startTime.getMillis()) : "Schedule not initialized correctly";

        startTime = startTime.property(DateTimeFieldType.hourOfDay()).setCopy(23);
        assert !schedule.isTimeIncluded(startTime.getMillis()) : "Schedule not initialized correctly - time after schedule";

        startTime = startTime.property(DateTimeFieldType.dayOfWeek()).setCopy("Sunday");
        assert !schedule.isTimeIncluded(startTime.getMillis()) : "Schedule not initialized correctly - weekend";

    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testInitializeFromPropertiesSpecialWeekend() {

        Properties props = new Properties();

        props.setProperty("timeZone", "US/Eastern");
        props.setProperty("startTime", "08:00 US/Eastern");
        props.setProperty("endTime", "22:00 US/Eastern");

        props.setProperty("startDay", "Monday");
        props.setProperty("endDay", "Friday");


        props.setProperty("weekednStartDay", "Friday");
        props.setProperty("weekendEndDay", "Sunday");

        props.setProperty("weekendStartTime", "21:00 US/Eastern");
        props.setProperty("weekendEndTime", "23:00 US/Eastern");

        try {
            // This functionality is not currently implemented
            Schedule schedule = new Schedule(props);

            DateTime now = new DateTime();
            DateTime startTime = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                    7, 0, 0, 0);

            startTime = startTime.property(DateTimeFieldType.dayOfWeek()).setCopy("Monday");

            assert !schedule.isTimeIncluded(startTime.getMillis()) : "Schedule not initialized correctly - time before schedule";

            startTime = startTime.property(DateTimeFieldType.hourOfDay()).setCopy(8);
            assert schedule.isTimeIncluded(startTime.getMillis()) : "Schedule not initialized correctly";

            startTime = startTime.property(DateTimeFieldType.hourOfDay()).setCopy(23);
            assert !schedule.isTimeIncluded(startTime.getMillis()) : "Schedule not initialized correctly - time after schedule";

            startTime = startTime.property(DateTimeFieldType.dayOfWeek()).setCopy("Sunday");
            assert !schedule.isTimeIncluded(startTime.getMillis()) : "Schedule not initialized correctly - weekend";
        } catch (IllegalArgumentException e) {
            assert true : "We should get here";
        }

    }

}
