package malbec.fix.message;

import java.math.BigDecimal;

import malbec.fer.mapping.IDatabaseMapper;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.Text;

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

    /* (non-Javadoc)
     * @see malbec.fix.message.DefaultFixFillConverter#extractOrderDetails(malbec.fix.message.FixFill, quickfix.Message)
     */
    @Override
    protected void extractOrderDetails(FixFill fill, Message er) throws FieldNotFound {
        super.extractOrderDetails(fill, er);
        
        if (er.isSetField(Text.FIELD)) {
            fill.setStrategy(er.getString(Text.FIELD));
        }
    }
}