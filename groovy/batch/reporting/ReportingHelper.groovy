package reporting


/**
 *
 */
public class ReportingHelper {
    
    static final String SQL_SENDER_COMP_IDS = "'TRAD', 'MSDW-PPT', 'REDIRPT', 'TRADEWEB', 'BLPDROP', 'TSRPT'"
    
    static final String SQL_SIDE_MAP = 
    "case Side when 1 then 'Buy' when 2 then 'Sell' when 3 then 'Buy Minus' " +
    "when 4 then 'Sell Plus' when 5 then 'Sell Short' when 6 then 'Sell Short Exempt' " +
    "when 7 then 'Undisclosed' when 8 then 'Crossed' end "
    
    static final String SQL_SIDE_CONSOLIDATION = 
    "case Side when 1 then 'Buy' when 2 then 'Sell' when 3 then 'Buy' when 4 then 'Sell' " +
    "when 5 then 'Sell' when 6 then 'Sell' when 7 then 'Undisclosed' when 8 then 'Crossed' "  +
    "end "
    
    static final String SQL_AGGREGATE = "sum(OrderQuantity) TotalOrderQuantity, " + 
    "sum(AveragePrice * PriceMultiplier * OrderQuantity) TotalAveragePrice "
    
    static final String SQL_ALL_WHERE = "from dbo.FixFill where OrderStatus=2 and SenderCompId " +
    " in ("+ ReportingHelper.SQL_SENDER_COMP_IDS + ") " +
    "and TradeDate=?"
    
    static final String SQL_AND_BROKER = " and ExecutingBroker=?"
    
    
    static int getSideValue(Map record, String side) {
        if (record.containsKey(side)) {
            return record.get(side);
        }
        return 0;
    }
    
    static double getSideAveragePrice(Map record, String side) {
        if (record.containsKey(side+"tap")) {
            return record.get(side+"tap");
        }
        return 0.0d;
    }
    
}
