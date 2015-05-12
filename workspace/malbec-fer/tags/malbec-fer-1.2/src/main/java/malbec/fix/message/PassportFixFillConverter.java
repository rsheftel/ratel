package malbec.fix.message;

import malbec.fer.mapping.IDatabaseMapper;
import quickfix.FieldNotFound;
import quickfix.Message;

class PassportFixFillConverter extends DefaultFixFillConverter {

    public PassportFixFillConverter(IDatabaseMapper dbm) {
        super(dbm);
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
        if (fill.getSecurityExchange() == null) {
            fill.setSecurityExchange("PASSPORT");
        }

        if (fill.getExecutingBroker() == null) {
            fill.setExecutingBroker("PASSPORT");
        }
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
            // we are getting Bloomberg symbols that include the exchange
            String[] symbolExchange = fill.getSymbol().split(" ");
            fill.setSymbol(symbolExchange[0]);
            if (fill.getSecurityExchange() == null && symbolExchange.length > 1) {
                fill.setSecurityExchange(symbolExchange[1]);
            }

            if (fill.getSecurityId() == null) {
                fill.setSecurityId(fill.getSymbol());
                fill.setSecurityIdSource("8"); // exchange ticker
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fix.message.DefaultFixFillConverter#getPlatform()
     */
    @Override
    protected String getPlatform() {
        return "PP";
    }

}