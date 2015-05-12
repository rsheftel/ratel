package malbec.fix.message;

import static malbec.util.FuturesSymbolUtil.bloombergToMaturityMonthYear;
import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.mapping.IDatabaseMapper;
import malbec.util.FuturesSymbolUtil;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.BeginString;
import quickfix.field.CFICode;
import quickfix.field.LastMkt;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.Symbol;

class RediFixFillConverter extends DefaultFixFillConverter {

    public RediFixFillConverter(IDatabaseMapper dbm) {
        super(dbm);
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fix.message.DefaultFixFillConverter#extractOrderDetails(malbec.fix.message.FixFill,
     * quickfix.fix42.ExecutionReport)
     */
    @Override
    protected void extractOrderDetails(FixFill fill, Message er) throws FieldNotFound {
        super.extractOrderDetails(fill, er);

        if ("FIX.4.4".equalsIgnoreCase(er.getHeader().getString(BeginString.FIELD))
            && er.isSetField(CFICode.FIELD)) {
            String cfiCode = er.getString(CFICode.FIELD);
            if ("FXXXXX".equalsIgnoreCase(cfiCode)) {
                // the test environment always send back FXXXXX even for equity 
                if (er.isSetField(LastMkt.FIELD)) {
                    String lastMarket = er.getString(LastMkt.FIELD);
                    if ("C".equals(lastMarket)) {
                        fill.setSecurityType("EQUITY");
                    } else {
                        fill.setSecurityType(SecurityType.FUTURE);
                    }
                } else {
                    fill.setSecurityType(SecurityType.FUTURE);
                }
            } else if ("EXXXXX".equalsIgnoreCase(cfiCode)) {
                fill.setSecurityType("EQUITY");
            }
        }

        if (fill.isEquity()) {
            fill.setSecurityIdSource(SecurityIDSource.EXCHANGE_SYMBOL);
            fill.setBloombergProductCode(BBYellowKey.Equity.toString());
        } else {
            fill.setSecurityIdSource(SecurityIDSource.BLOOMBERG_SYMBOL);
            String platformSymbol = er.getString(Symbol.FIELD);
            String platformRoot = FuturesSymbolUtil.extractSymbolRoot(platformSymbol);
            String bloombergRoot = getDatabaseMapper().lookupBloombergRootForPlatformRoot(getPlatform(),
                platformRoot);
            String monthYear = FuturesSymbolUtil.extractMaturityMonthFromSymbol(platformSymbol);
            String bloombergSymbol = FuturesSymbolUtil.combineRootMaturityMonthYear(bloombergRoot, monthYear);
            fill.setSecurityId(bloombergSymbol);
        }

        fill.setSecurityId(fill.getSymbol());

        if (SecurityType.FUTURE.equals(fill.getSecurityType())) {
            // we have assumed that all futures are Bloomberg symbols
            fill.setMaturityMonth(bloombergToMaturityMonthYear(fill.getSecurityId()));
        }

        IDatabaseMapper dbm = getDatabaseMapper();

        fill.setStrategy(dbm.lookupStrategy(getPlatform(), fill.getAccount()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fix.message.DefaultFixFillConverter#getPlatform()
     */
    @Override
    protected String getPlatform() {
        return "REDI";
    }

}