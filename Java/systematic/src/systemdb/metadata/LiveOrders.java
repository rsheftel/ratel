package systemdb.metadata;

import static db.clause.Clause.*;
import static systemdb.metadata.LiveOrderEmailsTable.*;
import static util.Dates.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import mail.*;

import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.SystemDB.*;

public class LiveOrders extends LiveOrdersBase {
    private static final long serialVersionUID = 1L;
    public static LiveOrders ORDERS = new LiveOrders();
    
    public LiveOrders() {
        super("ordeur");
    }
    
    public int insert(
        int systemId, String market, Date filledAt, Date submittedAt, String entryExit, 
        String positionDirection, long size, Double price, String details, 
        String description, String hostname, String topicPrefix, String ferretOrderId
    ) {
        insert(
            C_SYSTEMID.with(systemId),
            C_MARKET.with(market),
            C_TIME.withMaybe(filledAt),
            C_SUBMITTEDTIME.withMaybe(submittedAt),
            C_ENTRYEXIT.with(entryExit),
            C_POSITIONDIRECTION.with(positionDirection),
            C_SIZE.with(size),
            C_PRICE.with(price),
            C_ORDERDETAILS.with(details),
            C_DESCRIPTION.with(description),
            C_HOSTNAME.with(hostname),
            C_TOPICPREFIX.with(topicPrefix),
            C_FERRETORDERID.with(ferretOrderId)
        );
        return Db.identity();
    }
    
    public class LiveOrder extends Row {
        private static final long serialVersionUID = 1L;
        public LiveOrder(Row r) { super(r); }
        
        public int id() { return value(C_ID); }
        public String ferretOrderId() { return value(C_FERRETORDERID); }
        public int systemId() { return value(C_SYSTEMID); }
        public String market() { return value(C_MARKET); }
        public Date simFillTime() { return value(C_TIME); }
        public Date submittedTime() { return value(C_SUBMITTEDTIME); }
        public String entryExit() { return value(C_ENTRYEXIT); }
        public String positionDirection() { return value(C_POSITIONDIRECTION); }
        public long size() { return value(C_SIZE); }
        public Double price() { return value(C_PRICE); }
        public String orderDetails() { return value(C_ORDERDETAILS); }
        public String description() { return value(C_DESCRIPTION); }
        public String hostname() { return value(C_HOSTNAME); }
        public LiveSystem liveSystem() { return SystemDetailsTable.liveSystem(systemId()); }
        public String sivName() { return liveSystem().siv().sivName("-"); }
        public String prefix() { return value(C_TOPICPREFIX); }
        public void updateFill(double price, Date filledAt) {
            put(C_PRICE.with(price));
            put(C_TIME.with(filledAt));
            updateOne(subMap(Column.columns(C_PRICE, C_TIME)), allMatch(C_ID));
        }

        public void emailInterestedParties(Email mail) { 
            List<String> emails = ORDER_EMAILS.emails(sivName(), liveSystem().pv().name(), market());
            if (emails.isEmpty()) return;
            mail.sendTo(join(",", emails));
        }
        
        public boolean isFerret() {
            return ferretOrderId() != null;
        }

    }
    
    public List<LiveOrder> ordersSubmitted(int systemId, String market) {
        return recentOrders(systemMarketMatches(systemId, market), C_SUBMITTEDTIME);
    }
    
    public List<LiveOrder> ordersFilled(int systemId, String market) {
        return recentOrders(systemMarketMatches(systemId, market), C_TIME);
    }

    private Clause systemMarketMatches(int systemId, String market) {
        return C_SYSTEMID.is(systemId).and(C_MARKET.is(market));
    }
  
    public int maxIdBeforeToday() {
        Integer result = C_ID.max().valueOrNull(C_TIME.lessThan(midnight()).or(C_SUBMITTEDTIME.lessThan(midnight())));
        return result == null ? 0 : result;
    }
    
    public List<LiveOrder> ordersAfter(int greaterThanMe) {
        return orders(C_ID.greaterThan(greaterThanMe));
    }
    
    public List<String> prefixes() {
        return C_TOPICPREFIX.distinct(TRUE);
    }
    
    public LiveOrder order(int id) {
        return new LiveOrder(row(C_ID.is(id)));
    }

    private List<LiveOrder> recentOrders(Clause matches, DatetimeColumn time) {
        return orders(matches.and(time.greaterThan(daysAgo(7, now()))));
    }

    private List<LiveOrder> orders(Clause matches) {
        SelectMultiple select = select(matches);
        List<LiveOrder> result = empty();
        select.orderBy(C_ID.descending());
        for(Row r : select.rows())
            result.add(new LiveOrder(r));
        return result;
    }
}
