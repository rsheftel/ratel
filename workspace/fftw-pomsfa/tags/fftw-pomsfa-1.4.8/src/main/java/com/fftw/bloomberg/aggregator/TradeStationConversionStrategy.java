package com.fftw.bloomberg.aggregator;

import static malbec.util.FuturesSymbolUtil.combineRootMaturityMonthYear;

import java.util.List;

import malbec.fer.mapping.DatabaseMapper;
import malbec.fer.mapping.IDatabaseMapper;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;

public class TradeStationConversionStrategy extends AbstractConversionStrategy
{
    protected TradeStationConversionStrategy (DatabaseMapper dbm)
    {
        setPlatform(TradingPlatform.TradeStation);
        setDatabaseMapper(dbm);
    }

    @Override
    protected List<String> convertCommon (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = super.convertCommon(message, tradeRecord);
        // All accounts are DVP, and all DVP are Morgan Stanley - TradeStation
        tradeRecord.setPrimeBroker("MSPB");

        return errorMessages;
    }

    @Override
    protected void convertEquities (Message message, CmfTradeRecord tradeRecord)
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

    /**
     * Populate the trade record futures symbol, returning the Bloomberg root.
     * 
     * TradeStation sends us the exchange root, so we need to convert it and
     * then build the full Bloomberg Symbol
     */
    @Override
    protected String populateFuturesSymbol (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // We need to build a Bloomberg symbol
        String exchangeRoot = message.getString(Symbol.FIELD);
        String monthYear = message.getString(MaturityMonthYear.FIELD);
        String platform = getPlatform().getText();

        IDatabaseMapper dbm = getDatabaseMapper();
        String bbRoot = dbm.lookupBloombergRootForPlatformRoot(platform, exchangeRoot);
        String bbSymbol = combineRootMaturityMonthYear(bbRoot, monthYear);

        tradeRecord.setSecurityId(bbSymbol);

        return bbRoot;
    }

    @Override
    protected void convertFutures (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertFutures(message, tradeRecord);
        // set the broker
        tradeRecord.setBroker("RJOBCH");
    }

    @Override
    protected void convertOptions (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        // TradeStation only supports Equity Options
        super.convertOptions(message, tradeRecord);
        // Default to Chicago Board Options Exchange
        SecurityExchange exchange = extractExchange(message, new SecurityExchange("W"));

        MaturityMonthYear monthYear = new MaturityMonthYear(message
            .getString(MaturityMonthYear.FIELD));
        PutOrCall putOrCall = new PutOrCall(message.getInt(PutOrCall.FIELD));
        StrikePrice strikePrice = new StrikePrice(message.getDouble(StrikePrice.FIELD));

        String symbol = Fix2CmfUtil.optionsSymbol(extractSymbol(message), exchange, monthYear,
            putOrCall, strikePrice);

        tradeRecord.setSecurityId(symbol);
        tradeRecord.setBroker("BEARNY");

    }

}
