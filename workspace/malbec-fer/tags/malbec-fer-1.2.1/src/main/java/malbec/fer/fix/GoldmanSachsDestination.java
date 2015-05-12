package malbec.fer.fix;

import java.util.List;
import java.util.Properties;

import malbec.fer.CancelRequest;
import malbec.fer.Order;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.EmailSettings;

import quickfix.Group;
import quickfix.Message;
import quickfix.field.CFICode;
import quickfix.field.ListID;
import quickfix.field.PartyID;
import quickfix.field.PartyIDSource;
import quickfix.field.PartyRole;
import quickfix.field.Symbol;
import quickfix.field.TargetSubID;

/**
 * TODO Implement logic for Goldman Sachs
 */
public class GoldmanSachsDestination extends FixDestination {

    public GoldmanSachsDestination(String name, Properties config, EmailSettings emailSettings, DatabaseMapper dbm) {
        super(name, config, emailSettings, dbm);
        platform = "REDI";
    }

    /* (non-Javadoc)
     * @see malbec.fer.fix.FixDestination#populateCommonFields(malbec.fer.Order, quickfix.Message, java.util.List)
     */
    @Override
    protected void populateCommonFields(Order order, Message fixMessage, List<String> conversionErrors) {
        super.populateCommonFields(order, fixMessage, conversionErrors);

        String targetDestination = order.getExchange();
        
        // We support 2 exchanges destinations - TKTS (ticket) and DMA(SIGMA)
        if (order.getBasketName() != null) {
            if ("TICKET".equalsIgnoreCase(order.getExchange())) {
                targetDestination = "IPT";
                fixMessage.setField(new ListID(order.getBasketName()));
            } else {
                conversionErrors.add("Must specify exchange as 'ticket' when basketname is set");
            }
        } else {
            if ("SIGMA".equalsIgnoreCase(order.getExchange())) {
                // TKTS or DMA for not ticketing
                // GSDESK to remove REDI Desk
                targetDestination = "DMA";
            } else {
                targetDestination = "TKTS";
            }
        }
        order.setExchange(targetDestination);
        fixMessage.setField(new TargetSubID(targetDestination));

        addUserInfo(fixMessage);
    }

    private void addUserInfo(Message fixMessage) {
        // Handle the Goldman userID requirements
        Group group = getFixClient().createNumberOfPartiesGroup();

        group.setField(new PartyID(getUserID()));
        group.setField(new PartyIDSource('C'));
        group.setField(new PartyRole(PartyRole.ORDER_ORIGINATION_TRADER));

        fixMessage.addGroup(group);
    }  

    @Override
    protected void populateFuturesFields(Order order, Message fixMessage, List<String> errors) {

//        fixMessage.setField(new SecurityType(SecurityType.FUTURE));
        fixMessage.setField(new CFICode("FXXXXX"));
        
        //fixMessage.setField(new SecurityIDSource(SecurityIDSource.BLOOMBERG_SYMBOL));
        //fixMessage.setField(new SecurityIDSource()); // blank should be smart order router
        order.setSecurityIDSource("A"); // Bloomberg
        fixMessage.setField(new Symbol(order.getSymbol()));
        
     // for testing purposes send a market order to get the current price
//        fixMessage.setField(new OrdType(OrdType.MARKET));
//        fixMessage.removeField(44);

    }

    @Override
    protected void populateEquityFields(Order order, Message fixMessage, List<String> errors) {
        fixMessage.setField(new CFICode("EXXXXX"));
        fixMessage.setField(new Symbol(order.getSymbol()));

        // for testing purposes send a market order to get the current price
//        fixMessage.setField(new OrdType(OrdType.MARKET));
//        fixMessage.removeField(44);
    }

    /* (non-Javadoc)
     * @see malbec.fer.fix.FixDestination#populateCommonFieldsForCancel(malbec.fer.Order, quickfix.Message, java.util.List)
     */
    @Override
    protected void populateCommonFieldsForCancel(CancelRequest cancelRequest, Message fixMessage, List<String> errors) {
        super.populateCommonFieldsForCancel(cancelRequest, fixMessage, errors);
        addUserInfo(fixMessage);
    }
    
    
}
