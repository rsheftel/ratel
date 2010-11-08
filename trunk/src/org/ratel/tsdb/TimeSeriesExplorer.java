package org.ratel.tsdb;

import static org.ratel.db.Column.*;
import static org.ratel.db.clause.Clause.*;
import static org.ratel.tsdb.Attribute.*;
import static org.ratel.tsdb.AttributeTable.*;
import static org.ratel.tsdb.TSAMTable.*;
import static org.ratel.util.Arguments.*;
import static org.ratel.util.Errors.*;
import static org.ratel.util.Log.*;
import static org.ratel.util.Objects.*;

import java.util.*;

import org.ratel.util.*;
import org.ratel.db.*;
import org.ratel.db.clause.*;
import org.ratel.db.columns.*;

public class TimeSeriesExplorer {
    
    public static void main(String[] arguments) {
        List<String> empty = empty();
        Arguments args = arguments(arguments, empty);
        bombUnless(args.containsKey("attribute"), "Usage: tsdb.TimeSeriesExplorer -attribute attribute_name [-attributename attributevalue ...]\n");
        Attribute attr = attribute(args.get("attribute"));
        args.remove("attribute");
        AttributeValues filter = values(args);
        
        TSAMTable otherTsam = TSAMTable.alias("tsam_other");
        Clause isCds;
        Clause otherJoin;
        if(filter.isEmpty()) {
            isCds = TRUE;
            otherJoin = TRUE;
        } else {
            otherJoin = TSAM.C_TIME_SERIES_ID.joinOn(otherTsam);
            isCds = otherTsam.timeSeriesIdMatches(filter);
        }
        Clause attrMatches = attr.matches(ATTRIBUTE.C_ATTRIBUTE_ID);
        Clause attrJoin = TSAM.C_ATTRIBUTE_ID.joinOn(ATTRIBUTE);
        Clause matches = attrMatches.and(attrJoin).and(isCds).and(otherJoin);
        final CountTableColumn count = TSAM.countColumn();
        IntColumn id = TSAM.C_ATTRIBUTE_VALUE_ID;
        SelectMultiple select = TSAM.select(columns(
            id, 
            count
        ), matches);
        select.groupBy(id);
        List<Row> rows = select.rows();
        Collections.sort(rows, new Comparator<Row>() {
            @Override public int compare(Row o1, Row o2) {
                return o2.value(count).compareTo(o1.value(count));
            }
        });
        if(filter.isEmpty()) {
            otherJoin = TSAM.C_TIME_SERIES_ID.joinOn(otherTsam);
        }
        CountColumnColumn countCol = otherTsam.C_TIME_SERIES_ID.countDistinctColumn();
        Integer noValueTS = countCol.value(isCds.and(TSAM.notExists(TSAM.C_ATTRIBUTE_ID.is(attr.id()).and(otherJoin))));
        printReport(attr, count, id, rows, noValueTS);
    }

    @SuppressWarnings("deprecation") private static void printReport(Attribute attr, final CountTableColumn count, IntColumn id, List<Row> rows,
        Integer noValueTS) {
        debugSql(false);
        for (Row row : rows)
            System.out.println(attr.valueName(row.value(id)) + "\t\t\t" + row.value(count));
        System.out.println("NULL\t\t\t" + noValueTS);
    }


}