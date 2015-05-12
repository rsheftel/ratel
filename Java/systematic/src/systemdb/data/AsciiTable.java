package systemdb.data;

import static java.lang.Math.*;
import static transformations.Constants.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Objects.*;
import static util.Sequence.*;

import java.util.*;

import systemdb.metadata.*;
import util.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.SystemDB.*;
import file.*;

public class AsciiTable extends ASCIIBase implements HistoricalProvider {
    private static final long serialVersionUID = 1L;
    public static final AsciiTable SYSTEM_ASCII = new AsciiTable();
    
    public AsciiTable() {
        super("ascii");
    }

    public void insert(String name, String filename, boolean isDaily, double priceMultiplier) {
        insert(
            C_FILENAME.with(filename),
            C_NAME.with(name),
            C_COLUMNCLOSE.with("Close"),
            C_COLUMNDATE.with(isDaily ? "Date" : "DateTime"),
            C_COLUMNHIGH.with("High"),
            C_COLUMNLOW.with("Low"),
            C_COLUMNOPEN.with("Open"),
            C_COLUMNOPENINTEREST.with(isDaily ? "Total Open Interest": null),
            C_COLUMNVOLUME.with(isDaily ? "Total Volume" : "Volume"),
            C_PRICEMULTIPLIER.with(priceMultiplier)
            
        );
    }

    public class AsciiRow extends Row implements HistoricalDailyData, IntradaySource {
        private static final long serialVersionUID = 1L;
        private Csv csv;

        public AsciiRow(Clause matches) {
            super(row(matches));
        }

        public int barCount() {
            return csv().records().size();
        }

        private Csv csv() {
            if (csv != null) return csv;
            String fileName = value(C_FILENAME);
            fileName = fileName.replaceAll("V:\\\\", dataDirectory());
            fileName = fileName.replaceAll("\\\\\\\\nyux51\\\\data", dataDirectory());
            fileName = fileName.replaceAll("\\\\", "/"); 
            csv = new Csv(new QFile(fileName), true);
            return csv;
        }

        @Override public List<Bar> bars(Range range, Interval interval) {
            List<Bar> all = bars(range);
            return interval.aggregate(all);
        }

        public List<Bar> bars(Range range) {
            List<Bar> result = empty();
            for (List<String> record : csv().records()) {
                Bar bar = bar(record);
                if (!bar.in(range)) continue; 
                result.add(bar);
            }
            return result;
        }

        private Bar bar(List<String> record) {
            double multiplier = value(C_PRICEMULTIPLIER);
            Bar current = new Bar(
                date(csv().value(value(C_COLUMNDATE), record)),
                asDouble(record, C_COLUMNOPEN) * multiplier,
                asDouble(record, C_COLUMNHIGH) * multiplier,
                asDouble(record, C_COLUMNLOW) * multiplier,
                asDouble(record, C_COLUMNCLOSE) * multiplier,
                asLong(record, C_COLUMNVOLUME),
                asLong(record, C_COLUMNOPENINTEREST)
            );
            return current;
        }

        private Double asDouble(List<String> record, NvarcharColumn column) {
            return Double.valueOf(trimValue(record, column));
        }

        private Long asLong(List<String> record, NvarcharColumn column) {
            if (isEmpty(column)) return null;
            return Long.valueOf(trimValue(record, column));
        }
        
        private String trimValue(List<String> record, NvarcharColumn column) {
            String columnName = value(column);
            String csvValue = csv().value(columnName, record);
            bombIf(Strings.isEmpty(csvValue), 
                "empty value found where double expected in column " + columnName);
            csvValue = csvValue.trim();
            return csvValue;
        }

        public List<Bar> lastBars(int count) {
            List<Bar> result = empty();
            int barCount = barCount();
            count = min(count, barCount);
            for(Integer i : oneTo(count).reverse())
                result.add(bar(csv().record(barCount - i)));
            return result;
        }

        public Date firstBarDate() {
            return bar(csv().record(0)).date();
        }
        
        public Date lastBarDate() {
            return bar(csv().record(barCount() - 1)).date();
        }

        @Override public Double lastCloseBefore(Date date) {
            throw bomb("unimplemented for csv files");
        }
        
    }
    
    public AsciiRow dataSource(String name) {
        return new AsciiRow(C_NAME.is(name));
    }

}
