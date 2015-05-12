package com.fftw.util.settlement.calendar;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import com.fftw.util.settlement.SettlementDate;

/**
 * Represent a calendar for a specific market days.
 * 
 * The data for this is currently stored in the Malbec Partners BA database
 * (BADB). The MARKET_CALENDAR only contains holidays. If the date is not within
 * the table it is considered a valid trading/settlement date. Some markets
 * (NYSE) use two (2) calendars to determine the settlement dates.
 * 
 * 
 */
public class MarketCalendar
{

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    /**
     * Predefined market calendars that use two calendars for settlement date.
     * 
     * In the case that there is no second calendar, an empty string will work
     * to fill out the array
     */
    private static final String[][] CALENDAR_SETS = new String[][]
    {
        {
            "NYSE", "USBH"
        },
        {
            "USBH", ""
        }
    };

    private String calendarId;

    private Map<LocalDate, MarketCalendarDate> days = new HashMap<LocalDate, MarketCalendarDate>();

    private static MarketCalendar instance;

    private static Map<String, MarketCalendar> calendars;

    /**
     * Return the market calendar represented by the calendar key.
     * 
     * @param calendarKey
     * @return
     */
    public static MarketCalendar getInstance (String calendarKey)
    {
        try
        {
            synchronized (MarketCalendar.class)
            {
                if (instance == null)
                {
                    // Work within Spring
                    new MarketCalendar().initializeCalendars();
                }
            }
        }
        catch (SQLException e)
        {
            throw new ExceptionInInitializerError("Unable to load market calendars");
        }
        return calendars.get(calendarKey.toUpperCase());
    }

    /**
     * Read the calendars from the database.
     * 
     * @return
     */
    private void initializeCalendars () throws SQLException
    {
        instance = this;
        // calendars = readCalendars();
        calendars = readCalendarsJpa();
    }

    @SuppressWarnings("unchecked")
    private Map<String, MarketCalendar> readCalendarsJpa ()
    {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<MarketCalendarDate> results;

        Map<String, MarketCalendar> calendars = new HashMap<String, MarketCalendar>();

        try
        {
            // for each calendar read in the dates
            for (String[] keys : CALENDAR_SETS)
            {
                Query query = em
                    .createQuery("from MarketCalendarDate as mcd where mcd.marketCalendarKey in (:p, :s)");
                query.setParameter("p", keys[0]);
                query.setParameter("s", keys[1]);
                tx.begin();
                results = query.getResultList();
                tx.commit();
                if (results == null)
                {
                    results = Collections.<MarketCalendarDate> emptyList();
                }

                // create the calendar
                // The first key is the calendar key
                MarketCalendar bc = new MarketCalendar(keys[0]);

                for (MarketCalendarDate dbDate : results)
                {
                    MarketCalendarDate mcd = bc.getMarketDate(dbDate.getDate(), dbDate
                        .getDescription());
                    if (keys[0].equals(dbDate.getMarketCalendarKey()))
                    {
                        mcd.setValidTradeDate(false);
                        mcd.setValidSettleDate(false);
                    }
                    else
                    {
                        mcd.setValidSettleDate(false);
                    }
                    bc.addDate(mcd);
                }
                calendars.put(bc.getCalendarKey(), bc);
            }
        }
        finally
        {
            em.close();
        }

        return calendars;
    }

    /**
     * used for testings
     */
    MarketCalendar ()
    {
        // prevent

    }

    private MarketCalendar (String calendarId)
    {
        this.calendarId = calendarId;
    }

    public String getCalendarKey ()
    {
        return calendarId;
    }

    private void addDate (MarketCalendarDate bcd)
    {
        days.put(new LocalDate(bcd.getDate()), bcd);
    }

    public boolean isValidTradeDate (LocalDate date)
    {
        MarketCalendarDate busDate = days.get(date);

        if (busDate == null)
        {
            return true;
        }
        else
        {
            return busDate.isValidTradeDate();
        }
    }

    public boolean isValidSettledate (LocalDate date)
    {
        MarketCalendarDate busDate = days.get(date);

        if (busDate == null)
        {
            return true;
        }
        else
        {
            return busDate.isValidSettleDate();
        }
    }

    public boolean isValidSettledate (String calendarKey, LocalDate date)
    {
        return getInstance(calendarKey).getMarketDate(date).isValidSettleDate();
    }

    /**
     * Get the market date represented by the <code>LocalDate</code>.
     * 
     * @param date
     * @return
     */
    public MarketCalendarDate getMarketDate (LocalDate date)
    {
        return getMarketDate(date, null);
    }

    MarketCalendarDate getMarketDate (Date date, String description)
    {
        return getMarketDate(new LocalDate(date), description);
    }

    /**
     * This is used to create instances since we load from the database. It is
     * also used by the public version to get a market date. If the date does
     * not exist, it is created dynamically with the specified description and
     * set as a valid trade/settlement date.
     * 
     * @param date
     * @param description
     * @return
     */
    MarketCalendarDate getMarketDate (LocalDate date, String description)
    {
        MarketCalendarDate busDate = days.get(date);

        // dates not in the map are valid trading and settlement dates
        if (busDate == null)
        {
            return new MarketCalendarDate(this.calendarId, date, true, true, description);
        }
        else
        {
            return busDate;
        }
    }

    public LocalDate determineDate (LocalDate tradeDate, SettlementDate settlementDate)
    {
        switch (settlementDate)
        {
            case T0:
                return moveSettlementDate(tradeDate, 0);
            case T1:
                return moveSettlementDate(tradeDate, 1);
            case T2:
                return moveSettlementDate(tradeDate, 2);
            case T3:
                return moveSettlementDate(tradeDate, 3);
            case T4:
                return moveSettlementDate(tradeDate, 4);
            case T5:
                return moveSettlementDate(tradeDate, 5);
        }
        throw new IllegalArgumentException("Unknown settlement date " + settlementDate);
    }

    /**
     * Move the date the specified number of days, skipping weekends and
     * holidays.
     * 
     * @param workingDate
     * @param tdays
     * @return
     */
    LocalDate moveSettlementDate (LocalDate localDate, int tdays)
    {

        // while the next date is a weekend or invalid settlement date
        // keep increasing until we have incremented the specified
        // number of days, and have a valid settlement date

        LocalDate workingDate = localDate;

        if (tdays > 0)
        {
            for (int i = 0; i < tdays; i++)
            {
                workingDate = workingDate.plusDays(1);
                while (isWeekend(workingDate) || !isValidSettledate(workingDate))
                {
                    workingDate = workingDate.plusDays(1);
                }
            }
            return workingDate;
        }
        else
        {
            // Use the same logic as increment, but decrement
            for (int i = tdays; i < 0; i++)
            {
                workingDate = workingDate.minusDays(1);
                while (isWeekend(workingDate) || !isValidSettledate(workingDate))
                {
                    workingDate = workingDate.minusDays(1);
                }
            }
            return workingDate;
        }
    }

    /**
     * Move the date the specified number of days, skipping weekends and
     * holidays.
     * 
     * This uses two calendars, the current calendar and the supplied calendar.
     * 
     * @param secondCalendar
     * @param workingDate
     * @param tdays
     * @return
     */
    LocalDate moveSettlementDate (String secondCalendar, LocalDate localDate, int tdays)
    {

        // while the next date is a weekend or invalid settlement date
        // keep increasing until we have incremented the specified
        // number of days, and have a valid settlement date
        LocalDate workingDate = localDate;

        if (tdays > 0)
        {
            for (int i = 0; i < tdays; i++)
            {
                workingDate = workingDate.plusDays(1);
                while (isWeekend(workingDate) || !isValidSettledate(workingDate)
                    || !isValidSettledate(secondCalendar, workingDate))
                {
                    workingDate = workingDate.plusDays(1);
                }
            }
            return workingDate;
        }
        else
        {
            // Use the same logic as increment, but decrement
            for (int i = tdays; i < 0; i++)
            {
                workingDate = workingDate.minusDays(1);
                while (isWeekend(workingDate) || !isValidSettledate(workingDate)
                    || !isValidSettledate(secondCalendar, workingDate))
                {
                    workingDate = workingDate.minusDays(1);
                }
            }
            return workingDate;
        }
    }

    public boolean isWeekend (LocalDate ld)
    {
        int dayOfWeek = ld.dayOfWeek().get();

        return (DateTimeConstants.SATURDAY == dayOfWeek || DateTimeConstants.SUNDAY == dayOfWeek);
    }

    @Override
    public String toString ()
    {
        return calendarId + " " + days;
    }
}
