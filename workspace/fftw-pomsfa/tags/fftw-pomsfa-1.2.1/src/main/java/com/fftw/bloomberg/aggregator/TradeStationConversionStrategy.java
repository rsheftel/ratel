package com.fftw.bloomberg.aggregator;

import java.math.BigDecimal;
import java.util.List;

import quickfix.FieldNotFound;
import quickfix.field.LastPx;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;
import quickfix.fix42.ExecutionReport;

import com.fftw.bloomberg.cmfp.CmfConstants;
import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class TradeStationConversionStrategy extends AbstractConversionStrategy
{
    protected TradeStationConversionStrategy ()
    {
        setPlatform(TradingPlatform.TradeStation);
    }

    @Override
    protected List<String> convertCommon (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = super.convertCommon(message, tradeRecord);
        // All accounts are DVP, and all DVP are Morgan Stanley - TradeStation
        tradeRecord.setPrimeBroker("MSPB");

        return errorMessages;
    }

    @Override
    protected void convertEquities (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertEquities(message, tradeRecord);
        // Security includes the exchange
        Symbol symbol = extractSymbol(message);
        // Default to NYSE
        SecurityExchange fixExchange = extractExchange(message, new SecurityExchange("N"));

        tradeRecord.setSecurityId(Fix2CmfUtil.equitySymbol(symbol, fixExchange));
        tradeRecord.setBroker("BEARNY");
    }

    @Override
    protected void convertFutures (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertFutures(message, tradeRecord);

        // We need to build a Bloomberg symbol
        Symbol symbol = extractSymbol(message);

        String bbSymbol = Fix2CmfUtil.futureSymbol(getPlatform(), symbol, message
            .get(new MaturityMonthYear()));

        int productCode = Fix2CmfUtil.mapFuturesProductCode(getPlatform(), symbol);
        tradeRecord.setProductCode(productCode);

        // if we have a currency future, we need to send the Bloomberg unique ID
        if (productCode == 10)
        {
            tradeRecord.setSecurityIdFlag(CmfConstants.SIF_BB_UID);
            String bbuid = Fix2CmfUtil.tickerToBloombergUniqueId(bbSymbol);
            // if there is no mapping, use the original ticker
            if (bbuid == null)
            {
                bbuid = bbSymbol;
            }
            tradeRecord.setSecurityId(bbuid);
        }
        else
        {
            tradeRecord.setSecurityId(bbSymbol);
        }
        
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

        // no prime broker for 'futures' but we do have a broker
        tradeRecord.setBroker("RJOBCH");
    }

    @Override
    protected void convertOptions (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // TradeStation only supports Equity Options
        super.convertOptions(message, tradeRecord);
        // Default to Chicago Board Options Exchange
        SecurityExchange exchange = extractExchange(message, new SecurityExchange("W"));

        String symbol = Fix2CmfUtil.optionsSymbol(extractSymbol(message), exchange, message
            .get(new MaturityMonthYear()), message.get(new PutOrCall()), message
            .get(new StrikePrice()));

        tradeRecord.setSecurityId(symbol);
        tradeRecord.setBroker("BEARNY");

    }

}
