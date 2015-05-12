package malbec.fix.message;

import malbec.bloomberg.types.BBYellowKey;
import malbec.fer.mapping.IDatabaseMapper;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.SecondaryOrderID;
import quickfix.field.SecurityID;
import quickfix.field.SecurityType;
import quickfix.field.Text;

class TradeWebFixFillConverter extends DefaultFixFillConverter {

    public TradeWebFixFillConverter(IDatabaseMapper dbm) {
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

        if ("FIXED".equalsIgnoreCase(fill.getSymbol()) && er.isSetField(SecurityID.FIELD)) {
            fill.setSymbol(er.getString(SecurityID.FIELD));
        }

        if (er.isSetField(6609)) {
            fill.setSecurityType(er.getString(6609));
            if (SecurityType.TO_BE_ANNOUNCED.equalsIgnoreCase(fill.getSecurityType())) {
                fill.setBloombergProductCode(BBYellowKey.Mtge.toString());
            } else if ("UST".equalsIgnoreCase(fill.getSecurityType())) {
                fill.setBloombergProductCode(BBYellowKey.Govt.toString());
            }
        }

        if (er.isSetField(Text.FIELD)) {
            fill.setAccount(er.getString(Text.FIELD).toUpperCase());
        }

        // TradeWeb uses 198
        if (er.isSetField(SecondaryOrderID.FIELD)) {
            fill.setOrderId(er.getString(SecondaryOrderID.FIELD));
        }

        fill.setOrderType('Q'); // short for RFQ (Request For Quote)
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

        fill.setSecurityExchange("TRADEWEB");
    }

    /* (non-Javadoc)
     * @see malbec.fix.message.DefaultFixFillConverter#getPlatform()
     */
    @Override
    protected String getPlatform() {
        return "TW";
    }

}