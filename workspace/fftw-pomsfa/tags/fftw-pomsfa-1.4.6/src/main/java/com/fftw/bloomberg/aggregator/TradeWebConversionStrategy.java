package com.fftw.bloomberg.aggregator;

import java.util.List;

import malbec.bloomberg.types.BBYellowKey;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.ExecBroker;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.SettlmntTyp;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.AccountType;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class TradeWebConversionStrategy extends AbstractConversionStrategy
{
    public TradeWebConversionStrategy ()
    {
        setPlatform(TradingPlatform.TradeWeb);
    }

    @Override
    protected List<String> convertCommon (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = super.convertCommon(message, tradeRecord);
        tradeRecord.setPrimeBroker("MSPB");
        // This needs to be mapped from tag 76
        if (message.isSetField(ExecBroker.FIELD))
        {
            tradeRecord.setBroker(Fix2CmfUtil.mapExecutingBrokerToBloomberg(message
                .getString(ExecBroker.FIELD), "MOGSNY"));
        }
        else
        {
            tradeRecord.setBroker("MOGSNY");
        }

        String securityIDSource = message.getString(SecurityIDSource.FIELD);

        if (SecurityIDSource.CUSIP.equals(securityIDSource))
        {
            tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.Cusip);
            tradeRecord.setSecurityId(message.getString(SecurityID.FIELD));
        }
        else
        {
            tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.Unknown);
        }

        SecurityType securityType = new SecurityType(message.getString(6609));
        SettlmntTyp settlementType = extractSettlementType(message);

        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType, securityType));

        return errorMessages;
    }

    @Override
    protected void convertTba (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // Start of specific stuff
        tradeRecord.setProductCode(BBYellowKey.Mtge);
        tradeRecord.setPrimeBroker(Fix2CmfUtil.mapPrimeBroker(getPlatform(), AccountType.TBA, "NO PB"));
    }

    @Override
    protected void convertCds (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // must extract the FpML and set the fields
        
    }

    @Override
    protected void convertGovt (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        tradeRecord.setProductCode(BBYellowKey.Govt);
        tradeRecord.setPrimeBroker(Fix2CmfUtil.mapPrimeBroker(getPlatform(), AccountType.Government, "NO PB"));
    }

    @Override
    protected void convertEquities (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("TradeWeb does not support Equity trades");
    }

    @Override
    protected void convertFutures (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("TradeWeb does not support Futures trades");
    }

    @Override
    protected void convertOptions (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("TradeWeb does not support Options trades");
    }

}
