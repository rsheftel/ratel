package com.fftw.util.settlement;

import junit.framework.TestCase;
import quickfix.field.SecurityType;
import quickfix.field.SettlmntTyp;

public class TestSettlementUtil extends TestCase
{

    public void testRegularEquities ()
    {
        // Regular Equities
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.REGULAR), null);
        assertEquals(SettlementDate.T3, sd);

        // Common Stock
        sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(SettlmntTyp.REGULAR),
            new SecurityType(SecurityType.COMMON_STOCK));
        assertEquals(SettlementDate.T3, sd);

        // Preferred Stock
        sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(SettlmntTyp.REGULAR),
            new SecurityType(SecurityType.PREFERRED_STOCK));
        assertEquals(SettlementDate.T3, sd);
    }

    public void testRegularOptions ()
    {
        // Regular Options
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.REGULAR), new SecurityType(SecurityType.OPTION));
        assertEquals(SettlementDate.T1, sd);
    }

    public void testRegularFutures ()
    {
        // Regular Futures
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.REGULAR), new SecurityType(SecurityType.FUTURE));
        assertEquals(SettlementDate.T0, sd);
    }

    public void testRegularFX ()
    {
        // Regular FX
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.REGULAR), new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT));
        assertEquals(SettlementDate.T0, sd);
    }

    public void testRegular ()
    {
        // Regular
        try
        {
            SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
                SettlmntTyp.REGULAR), new SecurityType("BAD_SECURITY_TYPE"));
            fail("Processed a bad security type");
        }
        catch (IllegalArgumentException e)
        {

        }
    }

    public void testCash ()
    {
        // Cash
        SettlementDate sd = SettlementUtil.determineSettlementDate(
            new SettlmntTyp(SettlmntTyp.CASH), null);
        assertEquals(SettlementDate.T0, sd);
    }

    public void testNextDay ()
    {
        // Cash
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.NEXT_DAY), null);
        assertEquals(SettlementDate.T1, sd);
    }

    public void testT2 ()
    {
        // T+2
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.T_PLUS_2), null);
        assertEquals(SettlementDate.T2, sd);
    }

    public void testT3 ()
    {
        // T+3
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.T_PLUS_3), null);
        assertEquals(SettlementDate.T3, sd);
    }

    public void testT4 ()
    {
        // T+4
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.T_PLUS_4), null);
        assertEquals(SettlementDate.T4, sd);
    }

    public void testT5 ()
    {
        // T+5
        SettlementDate sd = SettlementUtil.determineSettlementDate(new SettlmntTyp(
            SettlmntTyp.T_PLUS_5), null);
        assertEquals(SettlementDate.T5, sd);
    }

}
