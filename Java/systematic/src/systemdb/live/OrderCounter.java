package systemdb.live;

import static util.Dates.*;
import db.*;
import db.tables.LiveDB.*;

public class OrderCounter extends OrderCounterBase {
    private static final long serialVersionUID = 1L;
    public static final OrderCounter COUNTER = new OrderCounter();
    
    public OrderCounter() {
        super("counter");
    }

    public int nextId() {
        return Db.doInSidestepTransaction(new SidestepThreadResult<Integer>(false) {
            @Override public Integer result() {
                insert(C_DATE.now());
                int id = Db.identity();
                Db.reallyCommit();
                return id - C_ID.min().value(C_DATE.greaterThan(midnight()));
            }
        });
    }
}
