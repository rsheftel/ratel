package malbec.fer.fix;

import java.util.List;
import java.util.Properties;

import malbec.fer.CancelRequest;
import malbec.fer.Order;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.EmailSettings;
import quickfix.Message;
import quickfix.field.ExecBroker;
import quickfix.field.ListID;
import quickfix.field.SenderSubID;
import quickfix.field.Text;

public class BloombergEmsxDestination extends FixDestination {

//    private static final String UUID_JEROME = "4768676";
//    private static final String UUID_ERIC = "4768604";
//    private static final String UUID_TEST = "TEST";  // Rob Campbell
//    private static final String UUID_TESTTW = "TESTTW";  // Tara Wilson
//    private static final String JEROME = "JEROME";
    
    public BloombergEmsxDestination(String name, Properties config, EmailSettings emailSettings, DatabaseMapper dbm) {
        super(name, config, emailSettings, dbm);
    }

    /* (non-Javadoc)
     * @see malbec.fer.fix.FixDestination#populateCommonFields(malbec.fer.Order, quickfix.Message, java.util.List)
     */
    @Override
    protected void populateCommonFields(Order order, Message fixMessage, List<String> conversionErrors) {
        
        if (order.getRoute() == null) {
            conversionErrors.add("EMSX orders must specify a route");
        }
        
        super.populateCommonFields(order, fixMessage, conversionErrors);
        
        if (order.getBasketName() != null) {
            fixMessage.setField(new ListID(order.getBasketName()));
        } 
        // For now, send the strategy tag as TEXT(tag 58)
        fixMessage.setString(Text.FIELD, order.getStrategy());
        // set TAG 76 to route/broker
        fixMessage.setString(ExecBroker.FIELD, order.getRoute());
        
        addUserInfo(fixMessage);
    }

    /* (non-Javadoc)
     * @see malbec.fer.fix.FixDestination#populateFuturesFields(malbec.fer.Order, quickfix.Message, java.util.List)
     */
    @Override
    protected void populateFuturesFields(Order order, Message fixMessage, List<String> errors) {
        errors.add("EMSX does not support futures orders");
    }

    /* (non-Javadoc)
     * @see malbec.fer.fix.FixDestination#determineAccount(malbec.fer.Order)
     */
    @Override
    protected String determineAccount(Order order) {
        // Based on the route, lookup the account
        return getDatabaseMapper().lookupAccountForRoute(getPlatform(), order.getRoute());
    }

    /* (non-Javadoc)
     * @see malbec.fer.fix.FixDestination#populateCommonFieldsForCancel(malbec.fer.CancelRequest, quickfix.Message, java.util.List)
     */
    @Override
    protected void populateCommonFieldsForCancel(CancelRequest cancelRequest, Message fixMessage,
            List<String> errors) {
        super.populateCommonFieldsForCancel(cancelRequest, fixMessage, errors);
        addUserInfo(fixMessage);
    }
    
    private void addUserInfo(Message fixMessage) {
        fixMessage.setString(SenderSubID.FIELD, getUserID());
    } 
}
