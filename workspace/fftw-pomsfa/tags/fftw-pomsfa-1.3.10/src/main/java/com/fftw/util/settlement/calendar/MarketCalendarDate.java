package com.fftw.util.settlement.calendar;

import java.util.Date;

import org.joda.time.LocalDate;

/**
 * Represent a market date.
 * 
 * A market date represents a calendar date that is:
 * <ul>
 * <li>valid trade and settlement date</li>
 * <li>invalid trade and invalid settlement date</li>
 * <li>valid trade date, but invalid settlement date</li>
 * </ul>
 */
public class MarketCalendarDate
{

    private String marketCalendarKey;

    private LocalDate date;

    private boolean validTradeDate = true;

    private boolean validSettleDate = true;

    private String description;

    private MarketCalendarDate ()
    {
        // prevent
    }

    /**
     * A fully constructed <code>MarketCalendarDate</code>
     * 
     * @param calendar
     * @param date
     * @param flag
     * @param description
     */
    MarketCalendarDate (String calendar, Date date, boolean validTrade, boolean validSettle,
        String description)
    {
        this(calendar, new LocalDate(date), validTrade, validSettle, description);
    }

    MarketCalendarDate (String calendar, LocalDate date, boolean validTrade, boolean validSettle,
        String description)
    {
        this(); // make the compiler happy
        this.marketCalendarKey = calendar;
        this.date = date;
        this.validTradeDate = validTrade;
        this.validSettleDate = validSettle;
        this.description = description;
    }

    MarketCalendarDate (String calendar, LocalDate date, boolean validTrade, boolean validSettle)
    {
        this(calendar, date, validTrade, validSettle, null);
    }

    void setValidTradeDate (boolean tradeDate)
    {
        this.validTradeDate = tradeDate;
    }

    public boolean isValidTradeDate ()
    {
        return validTradeDate;
    }

    void setValidSettleDate (boolean settleDate)
    {
        this.validSettleDate = settleDate;
    }

    public boolean isValidSettleDate ()
    {
        return validSettleDate;
    }

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder(128);

        sb.append(date.toString());
        sb.append(" ");

        if (validTradeDate && validSettleDate)
        {
            sb.append("Valid trade/settle date");
        }
        else if (validTradeDate && !validSettleDate)
        {
            sb.append("Valid trade/invalid settle");
        }
        else
        {
            sb.append("Invalid trade/settle date");
        }

        sb.append(" for calendar ");
        sb.append(marketCalendarKey);

        if (description != null)
        {
            sb.append(", ").append(description);
        }

        return sb.toString();
    }

    public String getDescription ()
    {
        return this.description;
    }

    String getMarketCalendarKey ()
    {
        return marketCalendarKey;
    }

    public LocalDate getDate ()
    {
        return date;
    }

}
