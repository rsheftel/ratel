package sto;

import static systemdb.metadata.MarketTable.*;
import static systemdb.metadata.SystemDetailsTable.*;
import static util.Objects.*;
import static util.Systematic.*;

import java.util.*;
import static sto.MetricResultsTable.*;

import systemdb.metadata.*;
import db.*;
import file.*;

public class TestSTO extends DbTestCase {

    private static final String STODIR = mainDir().file("\\dotNET\\Q\\testdata").path();

    
    
    public void testStoParams() throws Exception {
        STO aSto = new STO(STODIR, "testSTO");
        assertEquals(15, Integer.parseInt(aSto.parameters("TEST", "daily", "1", 3).get("ATRlen")));
    }
    
    public void testMetricAndCurveRuns() {
        STO sto = setUpSimpleSto();
        assertEquals(list("ABC_1_daily_mkt1", "ABC_1_daily_mkt2", "ABC_1_daily_mkt3"), sto.svimNames());
        assertEquals(3, sto.lastRunNumber());
        assertEquals(set(1, 2, 3), sto.metricRunNumbers());
        assertEquals(set(1, 2, 3), sto.curvesRunNumbers());
        sto.validate();
    }
    
    public void testGenerateMetricFiles() {
        STO sto = setUpSimpleSto();
        Map<String, Double> metrics = map("ericAwesomeFactor", 17.0, "jeffSuxFactor", 19.1, "run", 17.0);
        sto.addMetric("mkt1", 1, metrics);
        metrics.remove("run"); // the add should ignore the "run" metric and the retrieve should ignore it as well.
        Map<String, Double> metrics2 = map("ericAwesomeFactor", -1.0, "jeffSuxFactor", 5000.0);
        sto.addMetric("mkt2", 1, metrics2);
        assertEquals(list("ericAwesomeFactor", "jeffSuxFactor"), METRICS.metricNames(sto.systemId()));
        sto.metricsDir().clear();
        sto.generateMetricCsvs();
        
        assertEquals(metrics, sto.allMetrics("mkt1").get(1));
        assertEquals(metrics2, sto.metricsFromFileSLOW("mkt2", 1));
        
    }
    
    public void testAddingMetrics() throws Exception {
        STO sto = setUpSimpleSto();
        
        Map<String, Double> metrics = map("ericSuxFactor", 17.0, "jeffAwesomeFactor", 19.1);
        sto.addMetric("mkt1", 1, metrics);
        assertEquals(19.1, sto.getMetric("jeffAwesomeFactor", "mkt1", 1));

        metrics = map("ericSuxFactor", 20.0, "jeffAwesomeFactor", 20.0);
        sto.addMetric("mkt1", 1, metrics);
        assertEquals(20.0, sto.getMetric("jeffAwesomeFactor", "mkt1", 1));
    }

    public static STO setUpSimpleSto() {
        QDirectory stoDir = mainDir().directory("/Java/systematic/bin/sto");
        String stoId = "SimpleSTOTemplate";
        STO sto = new STO(stoDir.path(), stoId);
        new QDirectory(sto.stosDir()).directory("SimpleSTO").destroyIfExists();
        sto.copyTo("SimpleSTO");

        DETAILS.insert("ABC", "1", "daily", "NA", sto.stosDir(), "SimpleSTO");

        ExchangesTable.insert("EX1", 17, 19, "16:00:00", 3600);
        MARKET.insert("mkt1", "EX1", 170.0, null, null);
        
        sto = new STO(stoDir.path(), "SimpleSTO");
        sto.curvesDir().directory("ABC_1_daily_expected").destroyIfExists();
        return sto;
    }

}
