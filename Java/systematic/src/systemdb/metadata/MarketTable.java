package systemdb.metadata;

import static db.columns.FunctionColumn.*;
import static db.tables.SystemDB.ExchangeBase.*;
import static systemdb.metadata.MarketSessionTable.*;
import static systemdb.metadata.SystemTimeSeriesTable.*;
import static util.Errors.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.SystemDB.*;

public class MarketTable extends MarketBase {
    private static final long serialVersionUID = 1L;
    public static final MarketTable MARKET = new MarketTable();
    
    public MarketTable() {
        super("market");
    }

    public double bigPointValue(String name) {
        return exchangeDefaulted(name, C_BIGPOINTVALUE, T_EXCHANGE.C_DEFAULTBIGPOINTVALUE);
    }

    public double fixedSlippage(String name) {
        bombUnless(matches(name).exists(), "no market exists for " + name);
        return exchangeDefaulted(name, C_SLIPPAGE, T_EXCHANGE.C_DEFAULTSLIPPAGE);
    }
    
    public String slippageCalculator(String name) {
        bombUnless(matches(name).exists(), "no market exists for " + name);
        return exchangeDefaulted(name, C_SLIPPAGECALCULATOR, T_EXCHANGE.C_SLIPPAGECALCULATOR);
    }
    
    @SuppressWarnings("unchecked") 
    private <T> T exchangeDefaulted(String name, Column<T> marketColumn, Column<T> exchangeColumn) {
        Clause tsJoin = SYSTEM_TS.C_NAME.is(C_NAME);
        Clause exchangeJoin = SYSTEM_TS.C_EXCHANGE.is(T_EXCHANGE.C_EXCHANGE);
        Clause join = tsJoin.and(exchangeJoin);
        FunctionColumn<T> coalesce = coalesce(marketColumn, exchangeColumn);
        return coalesce.value(matches(name).and(join));
    }

    private Clause matches(String name) {
        return C_NAME.is(name);
    }
    
    public void insert(String name, Double slippage) {
        insert(name, "TEST", slippage, null, null);
    }
    public void insert(String name, String exchange, Double slippage, String closeTime, Integer processCloseOrdersOffsetSeconds) {
        bombUnless(T_EXCHANGE.C_EXCHANGE.is(exchange).exists(), "no exchange named " + exchange);
        if(!SYSTEM_TS.has(name))
            SYSTEM_TS.insert(name, "TSDB", "ActiveMQ");
        SYSTEM_TS.updateAll(
            new Row(SYSTEM_TS.C_EXCHANGE.with(exchange)), 
            SYSTEM_TS.C_NAME.is(name)
        );
        insert(
            C_NAME.with(name), 
            C_BIGPOINTVALUE.with(1.0), 
            C_SLIPPAGE.with(slippage)
        );
        if(closeTime != null)
            SESSION.insert(name, "DAY", "NOTATIME", closeTime, processCloseOrdersOffsetSeconds);
    }
    
    public void setSlippageCalculator(String name, String calculator) {
        C_SLIPPAGECALCULATOR.updateOne(matches(name), calculator);
    }

}
