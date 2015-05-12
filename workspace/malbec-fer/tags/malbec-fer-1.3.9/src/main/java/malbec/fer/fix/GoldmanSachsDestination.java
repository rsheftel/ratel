package malbec.fer.fix;

import static malbec.util.FuturesSymbolUtil.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import malbec.fer.CancelRequest;
import malbec.fer.Order;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.EmailSettings;
import malbec.util.FuturesSymbolUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.Message;
import quickfix.field.CFICode;
import quickfix.field.ListID;
import quickfix.field.OrdType;
import quickfix.field.PartyID;
import quickfix.field.PartyIDSource;
import quickfix.field.PartyRole;
import quickfix.field.Price;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.Symbol;
import quickfix.field.TargetSubID;
import quickfix.field.TimeInForce;

/**
 * Handle Goldman Sachs logic for Futures and Equity Orders.
 */
public class GoldmanSachsDestination extends FixDestination {

    private Logger log = LoggerFactory.getLogger(getClass());

    private List<String> validUsers = new ArrayList<String>();

    public GoldmanSachsDestination(String name, Properties config, EmailSettings emailSettings,
        DatabaseMapper dbm) {
        super(name, config, emailSettings, dbm);
        platform = "REDI";

        validUsers.add("IMCDONAL");
        validUsers.add("DHOROWIT");
        validUsers.add("JBOURGEO");
        validUsers.add("RSHEFTEL");
        validUsers.add("BLACKBOX");
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fer.fix.FixDestination#populateCommonFields(malbec.fer.Order, quickfix.Message,
     * java.util.List)
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
            if ("SIGMA".equalsIgnoreCase(order.getExchange()) || "DMA".equalsIgnoreCase(order.getExchange())
                && !isForceToTicket()) {
                // TKTS or DMA for not ticketing
                // GSDESK to remove REDI Desk
                targetDestination = "DMA";
            } else {
                targetDestination = "TKTS";
            }
        }
        order.setExchange(targetDestination);
        fixMessage.getHeader().setField(new TargetSubID(targetDestination));

        try {
            char tif = fixMessage.getChar(TimeInForce.FIELD);
            char orderType = fixMessage.getChar(OrdType.FIELD);

            // Convert to FIX 4.2 MOC and LOC orders
            if (TimeInForce.AT_THE_CLOSE == tif) {
                fixMessage.setChar(TimeInForce.FIELD, TimeInForce.DAY);
                if (OrdType.MARKET == orderType) {
                    fixMessage.setChar(OrdType.FIELD, OrdType.MARKET_ON_CLOSE);
                    if (fixMessage.isSetField(Price.FIELD)) {
                        fixMessage.removeField(Price.FIELD);
                    }
                } else if (OrdType.LIMIT == orderType) {
                    fixMessage.setChar(OrdType.FIELD, OrdType.LIMIT_ON_CLOSE);
                }
            }
        } catch (FieldNotFound e) {
            // TODO Auto-generated catch block
            log.error("Generated log", e);
        }
     
        addUserInfo(fixMessage, order.getClientUserId());
    }

    /**
     * set the userId for the order.
     * 
     * Try to extract the network id from the passed value (EXCEL App). If the value does not match any of the
     * known ids, use the default.
     * 
     * @param fixMessage
     * @param clientUserId
     */
    private void addUserInfo(Message fixMessage, String clientUserId) {
        // Handle the Goldman userID requirements
        Group group = getFixClient().createNumberOfPartiesGroup();

        String userId = getUserID();

        if (clientUserId != null) {
            String[] networkId = clientUserId.split("\\\\");
            if (networkId.length == 2) {
                userId = networkId[1].toUpperCase();
            } else {
                userId = networkId[0].toUpperCase();
            }

            if (!validUsers.contains(userId)) {
                log.warn("Found UserId of " + userId + ", not in valid list, using default: " + getUserID());
                userId = getUserID();
            }
        }
        group.setField(new PartyID(userId));
        group.setField(new PartyIDSource('C'));
        group.setField(new PartyRole(PartyRole.ORDER_ORIGINATION_TRADER));

        fixMessage.addGroup(group);
    }

    @Override
    protected void populateFuturesFields(Order order, Message fixMessage, List<String> errors) {

        fixMessage.setField(new CFICode("FXXXXX"));

        String ticker = order.getSymbol();
        // Need to send RIC code
        String symbolRoot = extractSymbolRoot(ticker);
        String maturityMonthYear = extractMaturityMonthFromSymbol(ticker);

        String platformSendingRoot = getDatabaseMapper().mapBloombergRootToPlatformSendingRoot(getPlatform(),symbolRoot);
        if (platformSendingRoot == null) {
            errors.add("Futures symbol not configured for " + getPlatform());
        } else {
            // use the else, to prevent the Null field value for QFJ
            // Goldman is not really using RIC, but that is how they want them sent
            order.setSecurityIDSource(SecurityIDSource.RIC_CODE);
            fixMessage.setField(new Symbol(ticker));
            fixMessage.setField(new SecurityIDSource(SecurityIDSource.RIC_CODE));
            fixMessage.setField(new SecurityID(combineRootMaturityMonthYear(platformSendingRoot, maturityMonthYear)));
        }

        // for testing purposes send a market order to get the current price
        // fixMessage.setField(new OrdType(OrdType.MARKET));
        // fixMessage.removeField(44);
    }

    @Override
    protected void populateEquityFields(Order order, Message fixMessage, List<String> errors) {
        fixMessage.setField(new CFICode("EXXXXX"));
        fixMessage.setField(new Symbol(order.getSymbol()));

        // for testing purposes send a market order to get the current price
        // fixMessage.setField(new OrdType(OrdType.MARKET));
        // fixMessage.removeField(44);
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fer.fix.FixDestination#populateCommonFieldsForCancel(malbec.fer.Order, quickfix.Message,
     * java.util.List)
     */
    @Override
    protected void populateCommonFieldsForCancel(CancelRequest cancelRequest, Message fixMessage,
        List<String> errors) {
        super.populateCommonFieldsForCancel(cancelRequest, fixMessage, errors);
        
        if (cancelRequest.getExchange() != null) {
            fixMessage.getHeader().setField(new TargetSubID(cancelRequest.getExchange()));
        }
        
        addUserInfo(fixMessage, cancelRequest.getClientUserId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see malbec.fer.fix.FixDestination#getOutboundMultiplier(java.lang.String)
     */
    @Override
    protected BigDecimal getOutboundMultiplier(Order order) {
        if (order.isFutures()) {
            String bloombergRoot = FuturesSymbolUtil.extractSymbolRoot(order.getSymbol());
            return getDatabaseMapper().lookupFuturesOutboundPriceMultiplier(getPlatform(), bloombergRoot);
        } else {
            return super.getOutboundMultiplier(order);
        }

    }

}