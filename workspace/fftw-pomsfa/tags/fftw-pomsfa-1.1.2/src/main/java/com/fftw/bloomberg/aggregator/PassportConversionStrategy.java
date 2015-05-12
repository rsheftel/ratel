package com.fftw.bloomberg.aggregator;

import java.math.BigDecimal;

import quickfix.FieldNotFound;
import quickfix.field.LastPx;
import quickfix.field.MaturityMonthYear;
import quickfix.field.Symbol;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class PassportConversionStrategy extends AbstractConversionStrategy
{
    protected PassportConversionStrategy ()
    {
        setPlatform(TradingPlatform.Passport);
    }

    @Override
    protected void convertCommon (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertCommon(message, tradeRecord);
        tradeRecord.setPrimeBroker("MSPB");
        tradeRecord.setBroker("MOGSNY");
    }

    @Override
    protected void convertEquities (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertEquities(message, tradeRecord);
        // Passport sends us the Bloomberg symbol.  No need to do any manipulation
        tradeRecord.setSecurityId(extractSymbol(message).getValue());
    }

    @Override
    protected void convertFutures (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertFutures(message, tradeRecord);

        Symbol bbSymbol = extractSymbol(message);
        // We need to extract the root symbol
        MaturityMonthYear monthyear = message.get(new MaturityMonthYear());

        Symbol symbol = extractFuturesRootSymbol(bbSymbol, monthyear);
        
        tradeRecord.setProductCode(Fix2CmfUtil.mapFuturesProductCode(getPlatform(), symbol));

        tradeRecord.setSecurityId(bbSymbol.getValue());

        BigDecimal priceMultiplier = Fix2CmfUtil.mapFuturesPriceMultiplier(getPlatform(), symbol);
        if (priceMultiplier.equals(BigDecimal.ONE))
        {
            tradeRecord.setPriceQuote(message.get(new LastPx()).getValue());
        }
        else
        {
            // If we have a mapping (even if it is 1.000) we end up here
            BigDecimal price = BigDecimal.valueOf(message.get(new LastPx()).getValue());
            tradeRecord.setPriceQuote(priceMultiplier.multiply(price));
        }
    }

    @Override
    protected void convertOptions (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // TradeStation only supports Equity Options
        super.convertOptions(message, tradeRecord);
        // If we have to do a lookup on this product, we will need
        // to add a method to extract the root symbol.
        tradeRecord.setSecurityId(extractSymbol(message).getValue());
    }
    
}
