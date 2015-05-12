package malbec.fix.message;

import java.math.BigDecimal;

import malbec.fer.mapping.IDatabaseMapper;
import quickfix.FieldNotFound;
import quickfix.Message;

class TradingScreenFixFillConverter extends DefaultFixFillConverter {

    public TradingScreenFixFillConverter(IDatabaseMapper dbm) {
        super(dbm);
    }

    /* (non-Javadoc)
     * @see malbec.fix.message.DefaultFixFillConverter#getPlatform()
     */
    @Override
    protected String getPlatform() {
        return "TRADS";
    }
    

    @Override
    protected void extractFuturesSymbolDetails(FixFill fill, Message er) throws FieldNotFound {
        // store the multiplied value
        String futuresRootSymbol = getFuturesSymbolRoot(fill.getSecurityId());
        
        BigDecimal multiplier = getDatabaseMapper().lookupFuturesInboundPriceMultiplier(getPlatform(), futuresRootSymbol);
        fill.setPriceMultiplier(multiplier);
        fill.setBloombergProductCode(getDatabaseMapper().lookupYellowKey(futuresRootSymbol).toString());
    }
}