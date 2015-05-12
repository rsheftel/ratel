package com.fftw.bloomberg.aggregator;

import java.util.List;

import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.FuturesSymbolUtil;

import org.joda.time.LocalDate;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.SettlmntTyp;
import quickfix.field.Symbol;
import quickfix.field.Text;

import com.fftw.bloomberg.cmfp.CmfTradeRecord;
import com.fftw.bloomberg.types.AccountType;
import com.fftw.bloomberg.types.BBSecurityIDFlag;
import com.fftw.bloomberg.types.TradingPlatform;
import com.fftw.bloomberg.util.Fix2CmfUtil;
import com.fftw.util.datetime.DateTimeUtil;

public class TradingScreenConversionStrategy extends AbstractConversionStrategy
{
    public TradingScreenConversionStrategy (DatabaseMapper dbm)
    {
        setPlatform(TradingPlatform.TradingScreen);
        setDatabaseMapper(dbm);
    }

    @Override
    protected List<String> convertCommon (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        List<String> errorMessages = super.convertCommon(message, tradeRecord);

        tradeRecord.setBroker("MANFNY");
        return errorMessages;
    }

    @Override
    protected void convertEquities (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertEquities(message, tradeRecord);
        // Security includes the exchange
        String bbSymbol = message.getString(SecurityID.FIELD);

        // Some equity executions are not translated properly
        String idSource = message.getString(SecurityIDSource.FIELD);
        if (!SecurityIDSource.BLOOMBERG_SYMBOL.equals(idSource))
        {
            // Default to NYSE
            SecurityExchange fixExchange = extractExchange(message, new SecurityExchange("N"));
            Symbol symbol = new Symbol(message.getString(Symbol.FIELD));
            tradeRecord.setSecurityId(Fix2CmfUtil.equitySymbol(symbol, fixExchange));
        }
        else
        {
            tradeRecord.setSecurityId(bbSymbol);
        }
    }

    /**
     * TradingScreen sends us Bloomberg symbols in tag 48
     */
    @Override
    protected String populateFuturesSymbol (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        String bbSymbol = message.getString(SecurityID.FIELD);
        tradeRecord.setSecurityId(bbSymbol);
        String bbRoot = FuturesSymbolUtil.extractSymbolRoot(bbSymbol);

        return bbRoot;
    }

    @Override
    protected void convertFutures (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        super.convertFutures(message, tradeRecord);
    }

    /**
     * Extract the root symbol from the Bloomberg symbol.
     * 
     * @param symbol
     * @return
     */
    protected Symbol extractFuturesRootSymbol (Symbol symbol)
    {
        String root = FuturesSymbolUtil.extractSymbolRoot(symbol.getValue());

        return new Symbol(root);
    }

    @Override
    protected void convertOptions (Message message, CmfTradeRecord tradeRecord)
        throws FieldNotFound
    {
        throw new UnsupportedOperationException("Cannot trade options with TradingScreen");
    }

    @Override
    protected void convertFX (Message message, CmfTradeRecord tradeRecord) throws FieldNotFound
    {
        tradeRecord.setSecurityIdFlag(BBSecurityIDFlag.FX);
        tradeRecord.setTransactionCode("D");

        SecurityType securityType = new SecurityType(message.getString(SecurityType.FIELD));
        SettlmntTyp settlementType = extractSettlementType(message);

        tradeRecord.setSettleDate(determineSettlementDate(message, settlementType, securityType));

        String primeBroker = Fix2CmfUtil.mapPrimeBroker(getPlatform(), AccountType.FX, "NO PB");
        tradeRecord.setPrimeBroker(primeBroker);

        tradeRecord.setSecurityId(extractSymbol(message).getValue());
        tradeRecord.setProductCode(BBYellowKey.Curncy);

    }

    @Override
    protected String getBloombergAccount (TradingPlatform platform, Message message)
    {
        String account = super.getBloombergAccount(platform, message);

        if (account == null)
        {
            String textTag = getStrategyTag(message);
            if (isValidStrategy(textTag))
            {
                return "QMF";
            }
        }

        return account;
    }

    @Override
    protected String getTradingStrategy (TradingPlatform platform, Message message)
    {
        String strategy = getStrategyTag(message);
        if (isValidStrategy(strategy))
        {
            int pipePos = strategy.indexOf('|');

            if (pipePos > 0)
            {
                return strategy.substring(0, pipePos);
            }
            return strategy;
        }
        return super.getTradingStrategy(platform, message);
    }

    private boolean isValidStrategy (String strategy)
    {
        return strategy != null && strategy.startsWith("QF.");
    }

    private String getStrategyTag (Message message)
    {
        if (message.isSetField(Text.FIELD))
        {
            try
            {
                return message.getString(Text.FIELD);
            }
            catch (FieldNotFound e)
            {
                // ignore
            }
        }
        return null;
    }

    @Override
    protected boolean isBuyToCover (Message message)
    {
        // We don't need this - but it might change. If we do we need to
        // finish the implementation
        // get the Ferret order, if it exists to determine if this is a
        // buy to cover or not
/*        OrderDao dao = OrderDao.getInstance();
        if (message.isSetField(OrderID.FIELD))
        {
            try
            {
                String orderId = message.getString(OrderID.FIELD);
                Order ferretOrder = dao.findByOrderId(orderId, new LocalDate(), getPlatform().getText());
                return "Y".equalsIgnoreCase(ferretOrder.getBuyToCover());
            }
            catch (FieldNotFound e)
            { // work around QFJ stupid API
            }
        }
*/
        return false;
    }

    @Override
    protected String determineSettlementDate (Message message, SettlmntTyp settlementType,
        SecurityType securityType) throws FieldNotFound
    {
        String settlementDate = super
            .determineSettlementDate(message, settlementType, securityType);
        LocalDate calculated = DateTimeUtil.getLocalDate(settlementDate);
        LocalDate today = new LocalDate();

        if (calculated.isBefore(today))
        {
            log.warn("Over-riding settlemetn date");
            return DateTimeUtil.getDateAsString(today);
        }
        return settlementDate;
    }
}
