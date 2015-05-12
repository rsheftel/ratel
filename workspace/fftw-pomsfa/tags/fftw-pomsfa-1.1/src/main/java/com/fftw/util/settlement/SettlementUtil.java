package com.fftw.util.settlement;

import quickfix.field.SecurityType;
import quickfix.field.SettlmntTyp;

/**
 * Utilities for manipulating settlement types and dates.
 * 
 * 
 */
public class SettlementUtil
{

    /**
     * Determine the settlement date for equity securities.
     * 
     * @param settlementType (TAG 63)
     * @return
     */
    public static SettlementDate determineSettlementDate (SettlmntTyp settlementType)
    {
        return determineSettlementDate(settlementType, null);
    }

    /**
     * Determine the settlement date based on the settlement type and the security type.
     * 
     * If settlement type is <code>null</code> equities is assumed.
     * 
     * @param settlementType (TAG 63)
     * @param securityType (TAG 167)
     * @return
     */
    public static SettlementDate determineSettlementDate (SettlmntTyp settlementType,
        SecurityType securityType)
    {
        if (SettlmntTyp.REGULAR == settlementType.getValue())
        {
            // assume equity trade for null values
            if (securityType == null || SecurityType.COMMON_STOCK.equals(securityType.getValue())
                || SecurityType.PREFERRED_STOCK.equals(securityType.getValue()))
            {
                return SettlementDate.T3;
            }
            else if (SecurityType.OPTION.equals(securityType.getValue()))
            {
                return SettlementDate.T1;
            }
            else if (SecurityType.FUTURE.equals(securityType.getValue()))
            {
                return SettlementDate.T0;
            }
            else if (SecurityType.FOREIGN_EXCHANGE_CONTRACT.equals(securityType.getValue()))
            {
                return SettlementDate.T0;
            }
            else
            {
                throw new IllegalArgumentException(
                    "Unable to determine settlement date based on security type " + securityType);
            }
        }
        else if (SettlmntTyp.CASH == settlementType.getValue())
        {
            return SettlementDate.T0;
        }
        else if (SettlmntTyp.NEXT_DAY == settlementType.getValue())
        {
            return SettlementDate.T1;
        }
        else if (SettlmntTyp.T_PLUS_2 == settlementType.getValue())
        {
            return SettlementDate.T2;
        }
        else if (SettlmntTyp.T_PLUS_3 == settlementType.getValue())
        {
            return SettlementDate.T3;
        }
        else if (SettlmntTyp.T_PLUS_4 == settlementType.getValue())
        {
            return SettlementDate.T4;
        }
        else if (SettlmntTyp.T_PLUS_5 == settlementType.getValue())
        {
            return SettlementDate.T5;
        }
        // We cannot do anything with what we were given. Give up.
        throw new IllegalArgumentException("Unable to determine settlement date from "
            + settlementType + " and " + securityType);
    }
}
