package malbec.fix.message;

import malbec.fer.mapping.DatabaseMapper;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.SecurityType;

class RediFixFillConverter extends DefaultFixFillConverter {
    
    public RediFixFillConverter(DatabaseMapper dbm) {
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
            fill.setSecurityIdSource("8"); // Exchange Symbol
        } else {
            fill.setSecurityIdSource("A"); // Bloomberg
        }
        
        fill.setSecurityId(fill.getSymbol());
        
        if (SecurityType.FUTURE.equals(fill.getSecurityType())) {
            // we have assumed that all futures are Bloomberg symbols
            fill.setMaturityMonth(getDatabaseMapper().extractMaturityMonthFromSymbol(fill.getSecurityId()));
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