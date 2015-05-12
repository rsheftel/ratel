package sto;

import static sto.MetricResultsTable.*;
import static systemdb.metadata.SystemDetailsTable.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import static util.Index.*;
import java.util.*;

import systemdb.metadata.*;
import systemdb.metadata.SystemDetailsTable.*;
import util.*;

import au.com.bytecode.opencsv.*;
import db.*;
import file.*;

public class STO {
    private final QDirectory stos;
    private final QDirectory sto;
    private SystemDetails details;
    private String stosDir;

    public STO(String dir, String id) {
        this.stosDir = dir;
        this.stos = new QDirectory(dir);
        this.stos.requireExists();
        this.sto = stos.directory(id); 
    }
    
    public STO(SystemDetails details) {
        this(details.stoDir(), details.stoId());
        this.details = details;
    }

    public static STO fromId(int systemId) {
        return new STO(DETAILS.details(systemId));
    }

    public static void main(String[] args) {
        
        int runsWritten = 0;
        STO sto = new STO("V:/Market Systems/Linked Market Systems/Equities/StrategyCountryETFs/STO", "Detailed.IS.1.1");
        QDirectory metrics = sto.directory("Metrics");
        doNotDebugSqlForever();
        boolean started = false;
        for(QFile csvFile : metrics.files()) {
        //for(QFile csvFile : list(metrics.file("LiqInj_1.1_daily_PTT11.EWWSPY.csv"))) {
            if (csvFile.name().equals("LiqInj_1.1_daily_PTT11.EWWSPY.csv")) started = true;
            if (!started) continue;
            info("processing file " + csvFile.name());
            String[] svim = csvFile.basename().split("_");
            Csv csv = csvFile.csv(true);
            
            List<List<String>> records = csv.records();
            for(List<String> record : records) {
                int run = -1;
                try {
                    Map<String, Double> values = emptyMap();
                    for(String column : csv.columns()) {
                        values.put(column, Double.parseDouble(csv.value(column, record)));
                    }
                    run = Integer.parseInt(csv.value("run", record));
                    METRICS.insert(sto.systemId(), svim[3], run, values);
                } catch (NumberFormatException e) {
                    info("failed processing " + commaSep(record) + " " + e.getMessage());
                }
                if (++runsWritten % 1000 != 0) continue;
                Db.commit();
                info("committed up to run " + run + " " + runsWritten + " / " + records.size());
            }
        }
        info("final commit");
        Db.commit();
    }
    
    public Map<String, String> parameters(String system, String interval, String version, int run) {
        return bombNull(parameters(system, interval, version).get(run),
            "could not find run " + run + " in file " + paramsFile(system, interval, version).path());
    }
    
    public Map<Integer, Map<String, String>> parameters(String system, String interval, String version) {
        Map<Integer, Map<String, String>> result = emptyMap();
        QFile paramsFile = paramsFile(system, interval, version);
        Csv params = new Csv(paramsFile, true);

        for (List<String> record : params.records()) {
            Map<String, String> runParams = emptyMap();
            for (String parameter : params.columns()) 
                runParams.put(parameter, params.value(parameter, record).trim());
            result.put(Integer.valueOf(params.value("run", record)), runParams);
        }
        return result;
    }
    
    public QFile paramsFile() {
        Siv siv = siv();
        return paramsFile(siv.system(), siv.interval(), siv.version());
    }

    private QFile paramsFile(String system, String interval, String version) {
        return sto.file("Parameters", join("_", system, version, interval) + ".csv");
    }
    
    public int lastRunNumber() {
    	return Integer.parseInt(first(last(the(sto.directory("Parameters").files(".*.csv")).csv(true).records())));
    }

    public Set<Integer> metricRunNumbers() {
        Set<Integer> result = emptySet();
        for(String svim : svimNames())
            result.addAll(metricRunNumbers(svim));
        return result;
    }

    public Set<Integer> curvesRunNumbers() {
        Set<Integer> result = emptySet();
        for(String svim : svimNames())
            result.addAll(curveRunNumbers(svim));
        return result;
    }

    Set<Integer> curveRunNumbers(String svim) {
        final Set<Integer> result = emptySet();        
        String svimCurveDir = curvesDir().directory(svim).path();
        info("fetching runNumbers from curves dir " + svimCurveDir);
        new File(svimCurveDir).listFiles(new FileFilter() {
            // using file filter side effect to traverse file list.
            @Override public boolean accept(File f) {
                if (f.isDirectory()) return false;
                String runNumber = f.getName().replaceAll("run_(.*).bin", "$1");
                result.add(Integer.parseInt(runNumber));
                return false; 
            }
        });
        return result;
    }

    Set<Integer> metricRunNumbers(String svim) {
        int metricColumnCount = -1;
        Set<Integer> result = emptySet();
        CSVReader r = null;
        String[] line = null;
        info("fetching runNumbers from metrics file for " + svim);
        try {
            r = new CSVReader(new FileReader(sto.file("Metrics/" + svim + ".csv").path()));
            line = r.readNext(); // header
            while ((line = r.readNext()) != null) {
                if (metricColumnCount == -1) {
                    metricColumnCount = line.length;
                    info("EXPECTED COL COUNT SET TO " + line.length);
                }
                else {
                    if (metricColumnCount != line.length) {
                        info("ERROR: " + svim + " " + commaSep(line) + " bad column count");
                        continue;
                    }
                }
//                info("processing " + commaSep(line));
                try { result.add(Integer.parseInt(line[0])); }
                catch (Exception e) { info("ERROR: " + svim + " " + commaSep(line) + e.getMessage());}
            }
        } catch (IOException e) {
            throw bomb("failed", e);
        }finally {
            if (r != null) try {
                r.close();
            } catch (IOException e) {
                err("close failed", e);
            }
        }
        return result;
    }

    public List<String> svimNames() {
        final List<String> result = empty();
        QDirectory curves = curvesDir();
        new File(curves.path()).listFiles(new FileFilter() {
            // using file filter side effect to traverse file list.
            @Override public boolean accept(File f) {
                if (f.isDirectory() && !f.getName().startsWith(".svn"))
                    result.add(f.getName());
                return false; 
            }
        });
        return result;
    }
    private void compareRuns(String svim) {
        info("processing " + svim);
        Set<Integer> metricRuns = metricRunNumbers(svim);
        Set<Integer> brokenRuns = new HashSet<Integer>(metricRuns);
        info("found " + metricRuns.size() + " runs in the metrics files");
        Set<Integer> curveRuns = curveRunNumbers(svim);
        info("found " + curveRuns.size() + " runs in the curves dir");
        brokenRuns.removeAll(curveRuns); // remove metrics that have existing curves, leaving bad metric runs
        info("found " + brokenRuns.size() + " metrics runs with no curves");
        curveRuns.removeAll(metricRuns); // remove curves that have existing metrics, leaving bad curve runs
        info("found " + curveRuns.size() + " curve runs with no metrics");
        brokenRuns.addAll(curveRuns); // combine into brokenRuns list
        info("found " + brokenRuns.size() + " runs needing re-run");
        info("broken runs are: " + brokenRuns);
    }
    public void validate() {
        for(String svim : svimNames()) compareRuns(svim);
    }

    public void copyTo(String newName) {
        sto.copy(stos.directory(newName));
    }

    public void addMetric(String market, int run, Map<String, Double> values) {
        METRICS.insert(systemId(), market, run, values);
    }

    public int systemId() {
        return details().id();
    }

    private SystemDetails details() {
        if(details != null) return details;
        details = DETAILS.details(stosDir(), stoId());
        return details;
    }

    private String stoId() {
        return sto.name();
    }

    String stosDir() {
        return stosDir;
    }

    public double getMetric(String metric, String market, int run) {
        return METRICS.value(systemId(), market, run, metric);
    }

    public QDirectory curvesDir() {
        return directory("CurvesBin");
    }
    
    public QDirectory metricsDir() {
        return directory("Metrics");
    }

    private QDirectory directory(String dir) {
        return sto.directory(dir);
    }

    public Map<String, Double> metricsFromFileSLOW(String market, int run) {
        Map<String, Double> result = emptyMap();
        QFile metricFile = metricFile(market);
        bombIf(metricFile.text().contains("\""), 
            "metricFile " + metricFile + " contains \" character!");
        Csv metricCsv = metricFile.csv(true);
        for(List<String> record : metricCsv.records()) {
            if (Integer.parseInt(record.get(0)) == run) {
                List<String> columns = metricCsv.columns();
                return metricsMap(rest(columns), rest(record));
            }
        }
        return result;
    }
    
    public Map<Integer, Map<String, Double>> allMetrics(String market) {
        Map<Integer, Map<String, Double>> result = emptyMap();
        QFile metricFile = metricFile(market);
        bombIf(metricFile.text().contains("\""), 
            "metricFile " + metricFile + " contains \" character!");
        Csv metricCsv = metricFile.csv(true);
        for(List<String> record : metricCsv.records()) {
            List<String> columns = metricCsv.columns();
            result.put(Integer.parseInt(first(record)), metricsMap(rest(columns), rest(record)));
        }
        return result;
    }

    public QFile metricFile(String market) {
        return metricsDir().file(siv().sviName("_") + "_" + market + ".csv");
    }

    private Siv siv() {
        return details().siv();
    }

    Map<String, Double> metricsMap(List<String> keys, List<String> values){
        Map<String, Double> result = emptyMap();
        for(Index<String> index : indexing(keys))
            result.put(index.value, Double.parseDouble(values.get(index.num)));
        return result;
    }
    
    public void generateMetricCsvs() {
        Log.doNotDebugSqlForever();
        List<String> metricNames = METRICS.metricNames(systemId());
        info("running over " + metricNames.size() + " metrics");
        List<String> header = empty();
        header.add("run");
        header.addAll(metricNames);
        List<String> markets = METRICS.markets(systemId());
        info("running over " + markets.size() + " markets");
        List<Integer> runs = METRICS.runNumbers(systemId(), first(markets));
        for (String market : markets) {
            info("starting market " + market);
            QFile metricsFile = metricFile(market);
            Csv metrics = new Csv(false);
            metrics.addHeader(header);
            info("running over " + runs.size() + " runs");
            for(int run : runs) {
                Map<String, String> valuesMap = METRICS.metricValues(systemId(), market, run);
                List<String> values = empty();
                values.add("" + run);
                for(String column : metricNames)
                    values.add(valuesMap.get(column));
                metrics.add(values);
            }
            info("write csv file on " + metricsFile);
            metrics.write(metricsFile);
            info("metrics file saved.");
        }
    }

    
    
    
}
