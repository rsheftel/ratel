package malbec.fix.message;

import malbec.fer.mapping.IDatabaseMapper;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;

import static malbec.util.FuturesSymbolUtil.*;

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

        if ("EQUITY".equalsIgnoreCase(fill.getSecurityType())) {
            fill.setSecurityIdSource(SecurityIDSource.EXCHANGE_SYMBOL);
        } else {
            fill.setSecurityIdSource(SecurityIDSource.BLOOMBERG_SYMBOL); 
            //fill.setSecurityIdSource(SecurityIDSource.RIC_CODE);  
        }
        
        fill.setSecurityId(fill.getSymbol());
        
        if (SecurityType.FUTURE.equals(fill.getSecurityType())) {
            // we have assumed that all futures are Bloomberg symbols
            fill.setMaturityMonth(bloombergToMaturityMonthYear(fill.getSecurityId()));
        }
    }

    /* (non-Javadoc)
     * @see malbec.fix.message.DefaultFixFillConverter#getPlatform()
     */
    @Override
    protected String getPlatform() {
        return "REDI";
    }

}