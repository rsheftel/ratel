package db;

import static db.clause.Clause.*;
import static tsdb.TSAMTable.*;
import static util.Objects.*;
import static util.Sequence.*;
import static util.Times.*;

import java.util.*;

public class PerformanceTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SelectOne<Integer> select = TSAM.C_TIME_SERIES_ID.select(FALSE);
        Table temp = select.intoTemp("perftest");
        long start = nowMillis();
        List<Row> rows = empty();
        for (int i : sequence(0,1000000))
            rows.add(new Row(temp.column(TSAM.C_TIME_SERIES_ID).with(i)));
        temp.insert(rows);
        System.out.println("Insert time = " + reallyMillisSince(start));
        
        start = nowMillis();
        temp.rows(TRUE);
        System.out.println("Select time = " + reallyMillisSince(start));
    }

}
