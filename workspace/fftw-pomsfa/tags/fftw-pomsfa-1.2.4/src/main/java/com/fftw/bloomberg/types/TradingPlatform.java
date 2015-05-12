package com.fftw.bloomberg.types;

/**
 * Represent a trading platform
 * 
 * 
 */
public enum TradingPlatform
{
    TradeStation("TS", "TRADSTAT"), Passport("PP", "PASSPORT"), REDI("REDI", "REDI");

    private final String text;
    
    private final String longText;

    private TradingPlatform (String shortText, String longText)
    {
        this.text = shortText;
        this.longText = longText;
    }

    public String getText ()
    {
        return text;
    }
    
    public String getLongText()
    {
        return longText;
    }

    public static TradingPlatform valueFor (String text)
    {

        if (TradeStation.getText().equals(text))
        {
            return TradeStation;
        }
        else if (Passport.getText().equals(text))
        {
            return Passport;
        }
        else if (REDI.getText().equals(text))
        {
            return REDI;
        } else {
            throw new IllegalArgumentException("No type defined for "+ text);
        }
    }
    
    public static TradingPlatform valueForLongText (String text)
    {

        if (TradeStation.getLongText().equals(text))
        {
            return TradeStation;
        }
        else if (Passport.getLongText().equals(text))
        {
            return Passport;
        }
        else if (REDI.getLongText().equals(text))
        {
            return REDI;
        } else {
            throw new IllegalArgumentException("No type defined for "+ text);
        }
    }

}
