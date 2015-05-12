package com.fftw.bloomberg.aggregator;

import java.util.List;

import quickfix.FieldNotFound;
import quickfix.field.ExecBroker;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.SettlmntTyp;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.AccountType;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class TradeWebConversionStrategy extends AbstractConversionStrategy
{
    protected TradeWebConversionStrategy ()
    {
        setPlatform(TradingPlatform.TradeWeb);
    }

    @Override
    protected List<String> convertCommon (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = super.convertCommon(message, tradeRecord);
        tradeRecord.setPrimeBroker("MSPB");
        // This needs to be mapped from tag 76
        if (message.isSetExecBroker())
        {
            tradeRecord.setBroker(Fix2CmfUtil.mapExecutingBrokerToBloomberg(message
                .getString(ExecBroker.FIELD), "MOGSNY"));
        }
        else
        {
            tradeRecord.setBroker("MOGSNY");
        }

        return errorMessages;
    }

    @Override
    protected void convertTba (quickfix.fix42.ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // Only TradeWeb supports TBA - this is the code that could probably be
        // shared
        tradeRecord.setProductCode(BBProductCode.Mortgage);
        String securityIDSource = message.getString(SecurityIDSource.FIELD);

        if (SecurityIDSource.CUSIP.equals(securityIDSource))
        {
            tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.Cusip);
            tradeRecord.setSecurityId(message.get(new SecurityID()).getValue());
        }
        else
        {
            tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.Unknown);
        }

        SecurityType securityType = new SecurityType(message.getString(6609));
        SettlmntTyp settlementType = extractSettlementType(message);

        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType, securityType));

        // Start of specific stuff
        tradeRecord.setPrimeBroker(Fix2CmfUtil.mapPrimeBroker(getPlatform(), AccountType.TBA, "NO PB"));
    }

    @Override
    protected void convertEquities (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("TradeWeb does not support Equity trades");
    }

    @Override
    protected void convertFutures (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("TradeWeb does not support Futures trades");
    }

    @Override
    protected void convertOptions (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("TradeWeb does not support Options trades");
    }

}
