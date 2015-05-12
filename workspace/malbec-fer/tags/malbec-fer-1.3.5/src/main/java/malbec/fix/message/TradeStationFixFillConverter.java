package malbec.fix.message;

import malbec.fer.mapping.IDatabaseMapper;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.Symbol;

class TradeStationFixFillConverter extends DefaultFixFillConverter {

    public TradeStationFixFillConverter(IDatabaseMapper dbm) {
        super(dbm);
    }

    /* (non-Javadoc)
     * @see malbec.fix.message.DefaultFixFillConverter#getPlatform()
     */
    @Override
    protected String getPlatform() {
        return "TS";
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fix.message.DefaultFixFillConverter#extractFillDetails(malbec.fix.message.FixFill,
     * quickfix.fix42.ExecutionReport)
     */
    @Override
    protected void extractFillDetails(FixFill fill, Message er) throws FieldNotFound {
        super.extractFillDetails(fill, er);
        fill.setExecutingBroker("TRADESTATION");
        if (fill.getSecurityExchange() == null) {
            fill.setSecurityExchange("TRADESTATION");
        }

        if ("EQUITY".equalsIgnoreCase(fill.getSecurityType())) {
            fill.setSecurityIdSource("8"); // exchange symbol
            fill.setSecurityId(fill.getSymbol());
        } 
    }

    @Override
    protected String getFuturesSymbolRoot(FixFill fill, Message er) {
        try {
            return er.getString(Symbol.FIELD);
        } catch (FieldNotFound e) {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see malbec.fix.message.DefaultFixFillConverter#extractFuturesSymbolDetails(malbec.fix.message.FixFill, quickfix.Message)
     */
    @Override
    protected void extractFuturesSymbolDetails(FixFill fill, Message er) throws FieldNotFound {
        super.extractFuturesSymbolDetails(fill, er);
        
        // set the symbol to the bloomberg id
        fill.setSymbol(fill.getSecurityId());
    }

}