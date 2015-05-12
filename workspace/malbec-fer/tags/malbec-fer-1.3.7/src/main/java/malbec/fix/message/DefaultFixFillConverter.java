package malbec.fix.message;

import static malbec.util.FuturesSymbolUtil.*;

import java.math.BigDecimal;

import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.DateTimeUtil;
import malbec.util.FuturesSymbolUtil;

import org.joda.time.LocalDate;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.Message.Header;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.BeginString;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecBroker;
import quickfix.field.ExecID;
import quickfix.field.ExecRefID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastCapacity;
import quickfix.field.LastMkt;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.ListID;
import quickfix.field.MaturityMonthYear;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.PositionEffect;
import quickfix.field.PossDupFlag;
import quickfix.field.PossResend;
import quickfix.field.Price;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.SenderCompID;
import quickfix.field.SenderSubID;
import quickfix.field.SendingTime;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.TimeInForce;
import quickfix.field.TradeDate;
import quickfix.field.TransactTime;

public class DefaultFixFillConverter implements IFixFillConverter {

    private IDatabaseMapper dbm;

    public DefaultFixFillConverter(IDatabaseMapper dbm) {
        this.dbm = dbm;
    }

    @Override
    public FixFill valueOf(Message er) throws FieldNotFound {
        FixFill fill = new FixFill();
        // header
        extractHeaderFields(fill, er);

        // Order details
        extractOrderDetails(fill, er);

        // Fill details
        extractFillDetails(fill, er);

        // make any changes that we need to insert correctly
        modifyFill(fill);

        return fill;
    }

    private void modifyFill(FixFill fill) {
        if (!SecurityIDSource.BLOOMBERG_SYMBOL.equalsIgnoreCase(fill.getSecurityId())
            && !SecurityIDSource.EXCHANGE_SYMBOL.equalsIgnoreCase(fill.getSecurityId())) {
            convertSecurityToBloomberg(fill);
        }
    }

    protected void extractHeaderFields(FixFill fill, Message er) throws FieldNotFound {
        Header header = er.getHeader();

        fill.setBeginString(header.getString(BeginString.FIELD));
        fill.setSenderCompId(header.getString(SenderCompID.FIELD));
        fill.setTargetCompId(header.getString(TargetCompID.FIELD));

        // optional fields
        if (header.isSetField(SendingTime.FIELD)) {
            fill.setSendingTime(header.getUtcTimeStamp(SendingTime.FIELD));
        }
        if (header.isSetField(SenderSubID.FIELD)) {
            fill.setSenderSubId(header.getString(SenderSubID.FIELD));
        }
        if (header.isSetField(PossDupFlag.FIELD)) {
            fill.setPossibleDuplicate(header.getBoolean(PossDupFlag.FIELD) ? 'Y' : 'N');
        }
        if (header.isSetField(PossResend.FIELD)) {
            fill.setPossibleResend(header.getBoolean(PossResend.FIELD) ? 'Y' : 'N');
        }
    }

    protected void extractOrderDetails(FixFill fill, Message er) throws FieldNotFound {

        fill.setOrderQuantity(BigDecimal.valueOf(er.getDouble(OrderQty.FIELD)));
        fill.setOrderStatus(er.getChar(OrdStatus.FIELD));
        fill.setSide(er.getChar(Side.FIELD));
        fill.setSymbol(er.getString(Symbol.FIELD));
        fill.setOrderId(er.getString(OrderID.FIELD));

        if (er.isSetField(StopPx.FIELD)) {
            fill.setStopPrice(BigDecimal.valueOf(er.getDouble(StopPx.FIELD)));
        }

        if (er.isSetField(PositionEffect.FIELD)) {
            fill.setPositionEffect(er.getChar(PositionEffect.FIELD));
        }

        if (er.isSetField(SecurityType.FIELD)) {
            // if not set, defaults to EQUITY
            fill.setSecurityType(er.getString(SecurityType.FIELD));
        }

        if (er.isSetField(SecurityIDSource.FIELD)) {
            // if not set, the symbol is populated
            fill.setSecurityIdSource(er.getString(SecurityIDSource.FIELD));
            fill.setOriginalSecurityIdSource(er.getString(SecurityIDSource.FIELD));
        }

        if (er.isSetField(SecurityID.FIELD)) {
            // if not set, the symbol is populated
            fill.setSecurityId(er.getString(SecurityID.FIELD));
            fill.setOriginalSecurityId(er.getString(SecurityID.FIELD));
        }

        if (er.isSetField(TradeDate.FIELD)) {
            // if not populated, defaults to today
            LocalDate ld = DateTimeUtil.getLocalDate(er.getString(TradeDate.FIELD));
            fill.setTradeDate(ld.toDateMidnight().toDate());
            // Should we extract the date from the TransactionTime and use that? TransactionTime is
            // in the execution details
        }

        if (er.isSetField(Account.FIELD)) {
            fill.setAccount(er.getString(Account.FIELD));
        } else {
            fill.setAccount("MISSING");
        }

        if (er.isSetField(ListID.FIELD)) {
            fill.setListId(er.getString(ListID.FIELD));
        }
        if (er.isSetField(ClOrdID.FIELD)) {
            fill.setClientOrderId(er.getString(ClOrdID.FIELD));
        }
        if (er.isSetField(OrdType.FIELD)) {
            fill.setOrderType(er.getChar(OrdType.FIELD));
        }

        if (er.isSetField(TimeInForce.FIELD)) {
            fill.setTimeInForce(er.getChar(TimeInForce.FIELD));
        } else {
            fill.setTimeInForce(TimeInForce.DAY);
        }

        if (er.isSetField(MaturityMonthYear.FIELD)) {
            fill.setMaturityMonth(er.getString(MaturityMonthYear.FIELD));
        }
    }

    protected void convertSecurityToBloomberg(FixFill fill) {
        if (fill.isEquity() && SecurityIDSource.RIC_CODE.equalsIgnoreCase(fill.getSecurityIdSource())) {
            String bcc = getDatabaseMapper().lookupBloombergCountryCode(fill.getSecurityExchange());
            if (bcc == null) {
                bcc = "US";
            }

            String symbol = fill.getSecurityId();
            int dotPosition = symbol.indexOf('.');
            String bbid = symbol.substring(0, dotPosition) + " " + bcc;
            fill.setSecurityId(bbid);
            fill.setSecurityIdSource(SecurityIDSource.BLOOMBERG_SYMBOL);
        }
    }

    protected void extractFillDetails(FixFill fill, Message er) throws FieldNotFound {
        fill.setAveragePrice(BigDecimal.valueOf(er.getDouble(AvgPx.FIELD)));
        fill.setCumulatedQuantity(BigDecimal.valueOf(er.getDouble(CumQty.FIELD)));
        fill.setExecutionId(er.getString(ExecID.FIELD));

        if (er.isSetField(ExecRefID.FIELD)) {
            fill.setExecutionReferenceId(er.getString(ExecRefID.FIELD));
        }

        // This is only for FIX 4.2
        if (er.isSetField(ExecTransType.FIELD)) {
            fill.setExecutionTransactionType(er.getChar(ExecTransType.FIELD));
        }
        fill.setLastPrice(BigDecimal.valueOf(er.getDouble(LastPx.FIELD)));
        fill.setLastShares(BigDecimal.valueOf(er.getDouble(LastShares.FIELD)));
        fill.setTransactionTime(er.getUtcTimeStamp(TransactTime.FIELD));
        fill.setExecutionType(er.getChar(ExecType.FIELD));

        // EMSX Drop copy is missing this
        if (er.isSetField(LeavesQty.FIELD)) {
            fill.setLeavesQuantity(BigDecimal.valueOf(er.getDouble(LeavesQty.FIELD)));
        } else {
            fill.setLeavesQuantity(new BigDecimal(fill.getOrderQuantity() - fill.getCumulatedQuantity()));
        }

        if (er.isSetField(LastMkt.FIELD)) {
            fill.setLastMarket(er.getString(LastMkt.FIELD));
        }

        if (er.isSetField(LastCapacity.FIELD)) {
            fill.setLastCapacity(String.valueOf(er.getChar(LastCapacity.FIELD)));
        }

        if (SecurityType.FUTURE.equalsIgnoreCase(fill.getSecurityType())) {
            extractFuturesSymbolDetails(fill, er);
        } else if (fill.isEquity()) {
            // TODO Why is this here instead of in the order details?
            fill.setBloombergProductCode(BBYellowKey.Equity.toString());
        }

        // Not all counter-parties send these
        if (er.isSetField(Price.FIELD)) {
            fill.setPrice(BigDecimal.valueOf(er.getDouble(Price.FIELD)));
        } else {
            fill.setPrice(fill.getLastPriceAsBigDecimal());
        }

        if (er.isSetField(SecurityExchange.FIELD)) {
            fill.setSecurityExchange(er.getString(SecurityExchange.FIELD));
        }

        if (er.isSetField(ExecBroker.FIELD)) {
            fill.setExecutingBroker(er.getString(ExecBroker.FIELD));
        }
    }

    /**
     * Map everything to Bloomberg codes without the yellow key
     * 
     * @param fill
     * @param er
     * @throws FieldNotFound
     */
    protected void extractFuturesSymbolDetails(FixFill fill, Message er) throws FieldNotFound {
        if (er.isSetField(MaturityMonthYear.FIELD)) {
            String maturityMonth = er.getString(MaturityMonthYear.FIELD);
            fill.setMaturityMonth(maturityMonth);
        } else {
            String maturityMonth = FuturesSymbolUtil.extractMaturityMonthFromSymbol(fill.getSymbol());
            fill.setMaturityMonth(maturityMonth);
        }

        // Convert the platform symbol to bloomberg
        String futuresRootSymbol = getFuturesSymbolRoot(fill, er);
        String bloombergSymbol = getDatabaseMapper().mapPlatformRootToBloombergSymbol(getPlatform(),
            futuresRootSymbol, fill.getMaturityMonth());
        fill.setSecurityIdSource("A"); // Bloomberg
        fill.setSecurityId(bloombergSymbol);

        // store the multiplied value
        String bloombergRoot = FuturesSymbolUtil.extractSymbolRoot(bloombergSymbol);
        BigDecimal multiplier = getDatabaseMapper().lookupFuturesInboundPriceMultiplier(getPlatform(),
            bloombergRoot);
        fill.setPriceMultiplier(multiplier);

        fill.setBloombergProductCode(getDatabaseMapper().lookupYellowKey(bloombergRoot).toString());
    }

    protected String getFuturesSymbolRoot(FixFill fill, Message er) {
        return extractSymbolRoot(fill.getSymbol());
    }

    protected String getFuturesSymbolRoot(String futuresSymbol) {
        return extractSymbolRoot(futuresSymbol);
    }

    protected String getPlatform() {
        return "";
    }

    protected int determineProtocolVersion(String beginString) {
        Double pv = Double.parseDouble(beginString.substring(4, beginString.length()));

        return (int) (pv * 10.0d);
    }

    protected IDatabaseMapper getDatabaseMapper() {
        return dbm;
    }

}
