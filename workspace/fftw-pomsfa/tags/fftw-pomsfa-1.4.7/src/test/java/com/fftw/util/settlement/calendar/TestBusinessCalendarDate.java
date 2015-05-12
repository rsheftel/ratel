package com.fftw.util.settlement.calendar;

import junit.framework.TestCase;

import org.joda.time.LocalDate;

import com.fftw.util.settlement.calendar.MarketCalendarDate;

public class TestBusinessCalendarDate extends TestCase
{

    public void testConstructor ()
    {
        MarketCalendarDate bcd1 = new MarketCalendarDate("TT", new LocalDate(2007, 8, 10), true, true,
            "Valid test date");
        assertEquals(new LocalDate(2007, 8, 10), new LocalDate(bcd1.getDate()));

        MarketCalendarDate bcd2 = new MarketCalendarDate("TT", new LocalDate(2007, 8, 10), false, false,
            "Invalid test date");
        assertEquals(new LocalDate(2007, 8, 10), new LocalDate(bcd2.getDate()));

        MarketCalendarDate bcd3 = new MarketCalendarDate("TT", new LocalDate(2007, 8, 10), true, false,
            "Valid trade date, invalid settlement date");
        assertEquals(new LocalDate(2007, 8, 10), new LocalDate(bcd3.getDate()));

    }
}
