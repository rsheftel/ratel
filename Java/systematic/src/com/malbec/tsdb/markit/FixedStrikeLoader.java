package com.malbec.tsdb.markit;

import static db.clause.Clause.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValue.*;
import static tsdb.AttributeValues.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeriesDataTable.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;
import java.util.zip.*;

import org.apache.commons.httpclient.methods.*;

import schedule.dependency.*;
import tsdb.*;
import util.*;
import util.web.*;

import com.malbec.tsdb.loader.*;

import db.*;
import db.clause.*;
import file.*;

public class FixedStrikeLoader {

    class FixedStrikeData implements CdsData {
        List<String> record;
        Csv data;
        
        public FixedStrikeData(List<String> record, Csv data) {
            this.record = record;
            this.data = data;
        }
        
        @Override public String toString() {
            return cdsTicker() + strike();
        }
        
        @Override public AttributeValue ccy() {
            return value(CCY, "Ccy");
        }

        private AttributeValue value(Attribute attribute, String column) {
            return attribute.value(value(column).toLowerCase());
        }

        private String value(String column) {
            return data.value(column, record);
        }

        @Override public AttributeValue cdsTicker() {
            return CdsTimeSeriesDefinition.cdsTicker(this);
        }

        @Override public AttributeValue docClause() {
            return createdIfNecessary(DOC_CLAUSE, data.value("DocClause", record)); 
        }

        @Override public AttributeValue ticker() {
            return createdIfNecessary(TICKER, value("Ticker"), TickerTable.TICKER.C_TICKER_DESCRIPTION.with(value("ShortName"))); 
        }

        @Override public AttributeValue tier() {
            return createdIfNecessary(TIER, value("Tier")); 
        }
        
        public String strike() {
            String coupon = value("RunningCoupon");
            if (coupon.equals("0.0025")) return "25";
            if (coupon.equals("0.01")) return "100";
            if (coupon.equals("0.05")) return "500";
            if (coupon.equals("0.1")) return "1000";
            throw bomb("invalid coupon! \n" + record);
        }

        public Double value(String quoteType, String tenor) {
            String prefix = quoteType.equals("price") ? "Upfront" : "ConvSpread";
            String string = data.value(prefix + tenor, record);
            if(isEmpty(string)) return null;
            bombUnless (string.endsWith("%"), string + " is not expressed as a % in \n" + record);
            string = string.replaceAll("\\%$", "");
            return Double.parseDouble(string) / 100.0;
        }

        public Date date() {
            return Dates.date(value("Date"));
        }
        
    }
    
    class FixedStrikeSeriesDefinition extends TimeSeriesDefinition<FixedStrikeData> {
        private final String quoteType;
        private final String tenor;

        public FixedStrikeSeriesDefinition(String quoteType, String tenor) {
            this.quoteType = quoteType;
            this.tenor = tenor;
        }
        
        @Override public TimeSeriesDataPoint dataPoint(FixedStrikeData row, TimeSeriesLookup lookup) {
            AttributeValues extras = values(
                QUOTE_TYPE.value(quoteType),
                TENOR.value(tenor),
                CDS_STRIKE.value(row.strike())
            );
            AttributeValues all = CdsTimeSeriesDefinition.cdsValues(row, extras);
            Integer id = lookup.id(all);
            if (id == null) id = lookup.create(name(all), all);
            return new TimeSeriesDataPoint(id, row.value(quoteType, tenor));
        }

        @Override protected String name(AttributeValues values) {
            return  values.join("_", CDS_TICKER, CDS_STRIKE, QUOTE_TYPE, TENOR);
        }
        
    }

    private static final List<String> USD_STRIKES = list("100", "500");
    
    private final String filename;
    private final DataSource source;
    private TimeSeriesLookup theLookup;
    private final List<FixedStrikeSeriesDefinition> series = empty();
    private final List<String> tenors = list("6m", "1y", "2y", "3y", "4y", "5y", "7y", "10y", "15y", "20y", "30y");

    public FixedStrikeLoader(String filename, DataSource source, Clause filter) {
        this.filename = filename;
        this.source = source;
        theLookup = MarkitLoader.cdsSeriesLookup(filter);
        for(String tenor : tenors) {
            series.add(new FixedStrikeSeriesDefinition("price", tenor));
            series.add(new FixedStrikeSeriesDefinition("spread", tenor));
        }
    }

    public void load() {
        QFile file = new QFile(filename);
        Csv csv = file.csv(false);
        List<List<String>> records = csv.records();
        Csv newCsv = new Csv();
        newCsv.addHeader(records.get(1));
        List<List<String>> realRecords = records.subList(2, records.size());
        for(List<String> record : realRecords) {
            newCsv.add(record);
        }
        load(newCsv);
    }

    private void load(Csv csv) {
        List<Row> observationRows = empty();
        int index = 0;
        for(List<String> record : csv.records())
            load(csv, record, observationRows, index++);
        writeUsingTempWithCommits(observationRows);
    }

    private void load(Csv csv, List<String> record, List<Row> observationRows, int index) {
        FixedStrikeData data = new FixedStrikeData(record, csv);
        if(data.ccy().name().toLowerCase().equals("usd") && !USD_STRIKES.contains(data.strike())) return;
        for(FixedStrikeSeriesDefinition definition : series) {
            TimeSeriesDataPoint dataPoint = definition.dataPoint(data, theLookup);
            if (dataPoint.value() == null) continue;
            Observations observations = new Observations(setHour(data.date(), 15), dataPoint.value());
            observationRows.addAll(observationRows(dataPoint.id(), source.id(), observations));
        }
        Db.commit();
        info("wrote " + ymdHuman(data.date()) + " " + data + " index " + index);
    }
    
    public static void main(String[] args) throws Exception {
        Arguments arguments = Arguments.arguments(args, list("source", "date"));
        Date asOf = arguments.date("date");
        QFile file = FileDependency.csvFile(asOf);
        FixedStrikeLoader loader = new FixedStrikeLoader(file.path(), source(arguments.string("source")), TRUE);
        loader.load();
        Db.commit();
    }
    
    public static class FileDependency extends Dependency {

        private String password;
        private String user;

        public FileDependency(Integer id, Map<String, String> parameters) {
            super(id);
            password = parameters.containsKey("password") ? parameters.get("password") : "Jerome83";
            user = parameters.containsKey("user") ? parameters.get("user") : "bourgeois";
        }
        
        @Override public String explain(Date asOf) {
            QFile rawFile = zipFile(asOf);
            String error = (rawFile.exists() && rawFile.size() < 1024 ? "Response:\n" + rawFile.text() : rawFile.path() + " does not exist or is too big to put in error message.");
            return "File download of " + csvFile(asOf).path() + " failed.\n" + error;
        }
        
        public static void main(String[] args) {
            Arguments arguments = Arguments.arguments(args, list("date"));
            Date asOf = arguments.date("date");
            FileDependency dependency = new FileDependency(-1, map("user", "bourgeois", "password", "Jerome83"));
            if(dependency.isIncomplete(asOf))
                info("INCOMPLETE:\n" + dependency.explain(asOf));
            else
                info("SUCCESS");
        }

        @Override public boolean isIncomplete(Date asOf) {
            PostMethod request = new PostMethod("https://www.Markit.com/export.jsp");
            request.setParameter("version", "5");
            request.setParameter("date", "" + asLong(asOf));
            request.setParameter("format", "csv");
            request.setParameter("type", "cds");
            request.setParameter("report", "FIXED_COUPON");
            request.setParameter("user", user);
            request.setParameter("password", password);
            int statusCode;
            try {
                statusCode = QHttpClient.client.executeMethod(request);
                info("returned status: " + statusCode);
                QFile zip = zipFile(asOf);
                zip.deleteIfExists();
                zip.copyFrom(request.getResponseBodyAsStream());
                ZipFile response = new ZipFile(zip.file());
                boolean incomplete = true;
                QFile csv = csvFile(asOf);
                for(Enumeration<? extends ZipEntry> e = response.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = e.nextElement();
                    if(entry == null) return true;
                    if(!entry.getName().endsWith(".csv")) continue;
                    csv.deleteIfExists();
                    csv.copyFrom(response.getInputStream(entry));
                    incomplete = false;
                }
                if (incomplete) Log.err("did not find csv file in zip!");
                return incomplete;            
            } catch (Exception e) {
                Log.err("download failed", e);
                return true;
            }

        }

        private static QFile csvFile(Date asOf) {
            return file(asOf, ".csv");
        }
        private static QFile zipFile(Date asOf) {
            return file(asOf, ".zip");
        }

        private static QFile file(Date asOf, String extension) {
            return Systematic.dataDirectory().file("CDS", "FixedCoupon", asLong(asOf) + extension);
        }
        
    }

}
