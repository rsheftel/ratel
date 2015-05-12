package sto;

import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.tables.MetricDB.*;

public class MetricResultsTable extends STOMetricResultsBase {
    private static final long serialVersionUID = 1L;   
    public static MetricResultsTable METRICS = new MetricResultsTable();

    public MetricResultsTable() {
        super("metrics");
    }

    public void insert(int systemId, String market, int run, Map<String, Double> values) {
        deleteAll(matches(systemId, market, run));
        List<Row> toInsert = empty();
        for (String metric : values.keySet()) {
            if (metric.equals("run")) continue;
            toInsert.add(new Row(
                C_MARKET.with(market),
                C_METRIC.with(metric),
                C_RUN.with(run),
                C_SYSTEMID.with(systemId),
                C_VALUE.with(nDecimals(12, values.get(metric)))
            ));
        }
        insert(toInsert);
    }
    
    public static void main(String[] args) {
        info(nDecimals(12, Double.NaN));
        info(nDecimals(12, Double.POSITIVE_INFINITY));
        info("" + Double.POSITIVE_INFINITY);
    }

    public double value(int systemId, String market, int run, String metric) {
        return Double.parseDouble(C_VALUE.value(matches(systemId, market, run).and(C_METRIC.is(metric))));
    }

    private Clause matches(int systemId, String market, int run) {
        return matches(systemId, market).and(C_RUN.is(run));
    }

    private Clause matches(int systemId, String market) {
        return systemMatches(systemId).and(marketMatches(market));
    }

    private Clause marketMatches(String market) {
        return C_MARKET.is(market);
    }

    private Clause systemMatches(int systemId) {
        return C_SYSTEMID.is(systemId);
    }
    
    public Map<String, Double> blank() {
        return emptyMap();
    }

    public List<String> metricNames(int systemId) {
        Clause systemMatches = systemMatches(systemId);
        Clause marketMatches = C_MARKET.is(C_MARKET.min().value(systemMatches));
        int minRunByMarket = C_RUN.min().value(marketMatches.and(systemMatches));
        Clause runMatches = C_RUN.is(minRunByMarket);
        Clause matches = systemMatches.and(marketMatches).and(runMatches).and(C_METRIC.isNot("run"));
        return C_METRIC.distinct(matches);
    }

    public List<String> markets(int systemId) {
        return C_MARKET.distinct(systemMatches(systemId));
    }

    public List<Integer> runNumbers(int systemId, String market) {
        return C_RUN.distinct(matches(systemId, market));
    }

    public Map<String, String> metricValues(int systemId, String market, int run) {
        Map<String, String> result = emptyMap();
        for(Row r : rows(matches(systemId, market, run))) {
            result.put(r.value(C_METRIC), r.string(C_VALUE));
        }
        return result;
    }

}
