package com.fftw.bloomberg.types;

/**
 * Represent an account type
 * 
 * 
 */
public enum AccountType
{
    Equity("EQUITY", "Equity"), Futures("FUTURES", "Futures"), FX("FX", "Foreign Exchange"), 
    TBA("TBA", "Mortgages"), Unknown("TEST", "Test/Unknown");

    private final String text;

    private final String longText;

    private AccountType (String shortText, String longText)
    {
        this.text = shortText;
        this.longText = longText;
    }

    public String getText ()
    {
        return text;
    }

    public String getLongText ()
    {
        return longText;
    }

    public static AccountType valueFor (String text)
    {

        if (Equity.getText().equals(text))
        {
            return Equity;
        }
        else if (Futures.getText().equals(text))
        {
            return Futures;
        }
        else if (FX.getText().equals(text))
        {
            return FX;
        }
        else if (TBA.getText().equals(text))
        {
            return TBA;
        }
        else
        {
            return Unknown;
        }
    }

    public static AccountType valueForLongText (String text)
    {

        if (Equity.getLongText().equals(text))
        {
            return Equity;
        }
        else if (Futures.getLongText().equals(text))
        {
            return Futures;
        }
        else if (FX.getLongText().equals(text))
        {
            return FX;
        }

        else if (TBA.getLongText().equals(text))
        {
            return TBA;
        }
        else
        {
            return Unknown;
        }
    }

}
