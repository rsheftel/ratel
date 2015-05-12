package malbec.fer.fix;

import java.util.List;
import java.util.Properties;

import malbec.fer.Order;
import malbec.fer.mapping.DatabaseMapper;
import malbec.util.EmailSettings;


import quickfix.Message;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SecurityType;
import quickfix.field.Symbol;

public class TradeStationDestination extends FixDestination {

    public TradeStationDestination(String name, Properties config, EmailSettings emailSettings, DatabaseMapper dbm) {
        super(name, config, emailSettings, dbm);
        platform = "TS";
    }

    /* (non-Javadoc)
     * @see malbec.fer.fix.FixDestination#populateCommonFields(malbec.fer.Order, quickfix.Message, java.util.List)
     */
    @Override
    protected void populateCommonFields(Order order, Message fixMessage, List<String> conversionErrors) {
        // ensure the exchange is correct
        if (! "TRAD".equalsIgnoreCase(order.getExchange())) {
            conversionErrors.add("Wrong exchange value for TradeStation (TRAD): " + order.getExchange());
        }
        
        super.populateCommonFields(order, fixMessage, conversionErrors);
    }  
    
    @Override
    protected void populateFuturesFields(Order order, Message fixMessage, List<String> errors) {

        fixMessage.setField(new SecurityType(SecurityType.FUTURE));
        //fixMessage.setField(new SecurityIDSource(SecurityIDSource.BLOOMBERG_SYMBOL));
        fixMessage.setField(new SecurityIDSource("100")); // trad
        // deal with the Bloomberg to TradeStation symbol
        String originalSymbol = order.getSymbol();
        String symbol = bloombergToTradeStationSymbol(originalSymbol);
        
        // For now Ryan wants these to be an error - I was correctly them
        if (! originalSymbol.equals(symbol)) {
            errors.add("Invalid Tradestation symbol.  Must specify 2 digit year.");
        }
        fixMessage.setField(new SecurityID(symbol));
        
        order.setSecurityIDSource("A"); // Bloomberg
    }

    @Override
    protected void populateEquityFields(Order order, Message fixMessage, List<String> errors) {
        // TS uses EQU
        fixMessage.setField(new SecurityType("EQU"));
        fixMessage.setField(new Symbol(order.getSymbol()));
    }

    private String bloombergToTradeStationSymbol(String symbol) {
        // split off the year
        // extract the month
        // do the lookup/mapping
        // rebuild the symbol
        
        // Tradestation needs a complete 2 digit year.
        String[] parts = symbol.split("\\d");  // get everything but the year
        int length = parts[0].length();
        String symbolMonth = parts[0].substring(0, length);
        String year = symbol.substring(parts[0].length());
        
        
        // Rebuild the symbol
        StringBuilder sb = new StringBuilder();
        sb.append(symbolMonth);

        if (year.length() == 1) {
            sb.append("0");
        }
        
        sb.append(year);
        
        return sb.toString();
    }
}
