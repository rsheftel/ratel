package systemdb.metadata;

import db.tables.SystemDB.*;
import static systemdb.metadata.ExchangeSessionTable.*;

public class ExchangesTable extends ExchangeBase {
    private static final long serialVersionUID = 1L;
    public static final ExchangesTable EXCHANGES = new ExchangesTable();
    
    public ExchangesTable() {
        super("exchange");
    }

    public static void insert(String name, double defaultBigPointValue, double defaultSlippage, String defaultClose, int defaultCloseOffset) {
        EXCHANGES.insertOne(name, defaultBigPointValue, defaultSlippage, defaultClose, defaultCloseOffset);
    }

    private void insertOne(String name, double defaultBigPointValue, double defaultSlippage, String defaultClose, int defaultCloseOffset) {
        insert(
            C_EXCHANGE.with(name), 
            C_DEFAULTBIGPOINTVALUE.with(defaultBigPointValue), 
            C_DEFAULTSLIPPAGE.with(defaultSlippage)
        );
        if(defaultClose != null)
            EXCHANGE_SESSION.insert(name, "DAY", "NOTATIME", defaultClose, defaultCloseOffset);
    }

    public static void insert(String exchange, double defaultBigPointValue, double defaultSlippage) {
        insert(exchange, defaultBigPointValue, defaultSlippage, null, 0);
    }
}
