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

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.BBProductCode;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class RediConversionStrategy extends AbstractConversionStrategy
{
    protected RediConversionStrategy()
    {
        setPlatform(TradingPlatform.REDI);
    }
    
    @Override
    protected List<String> convertCommon (ExecutionReport message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = super.convertCommon(message, tradeRecord);
        // All accounts are DVP, and all DVP are Morgan Stanley - TradeStation
        tradeRecord.setPrimeBroker("MSPB");
        tradeRecord.setBroker("GOLDNY");
        
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
        SecurityExchange exchange = extractExchange(message, new SecurityExchange("N"));
        
        tradeRecord.setSecurityId(Fix2CmfUtil.equitySymbol(symbol, exchange));
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
        
        BBProductCode productCode = Fix2CmfUtil.mapFuturesProductCode(getPlatform(), symbol);
        tradeRecord.setProductCode(productCode);
        
        // if we have a currency future, we need to send the Bloomberg unique ID
        if (BBProductCode.Currency == productCode)
        {
            tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.BBUID);
            String bbuid = Fix2CmfUtil.tickerToBloombergUniqueId(bbSymbol.getValue());
            // if there is no mapping, use the original ticker
            if (bbuid == null)
            {
                bbuid = bbSymbol.getValue();
            }
            tradeRecord.setSecurityId(bbuid);
        }
        else
        {
            tradeRecord.setSecurityId(bbSymbol.getValue());
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
        
        // Change this for futures
        tradeRecord.setPrimeBroker("GS FUT");
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

    }

}
