package futures;

import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static util.Log.*;

import java.util.*;

import tsdb.*;
import util.*;
import db.*;

public class FixOptionTimeSeriesWithMissingExpiries {

    public static void main(String[] args) {
        doNotDebugSqlForever();
        AttributeValues values = values(
            INSTRUMENT.value("futures_option"),
            EXPIRY.value("actual"),
            CONTRACT.value("tu", "fv", "ty", "us")
        );
        List<TimeSeries> serieses = TimeSeries.multiSeries(values);
        int count = 0;
        for (TimeSeries series : serieses) {
            count++;
            AttributeValues attributes = series.attributes();
//            if(attributes.has(EXPIRY_DATE)) continue;
            if(attributes.has(EXPIRY_DATE)) attributes.remove(EXPIRY_DATE);
            ContractCurrent contract = new ContractCurrent(attributes.get(CONTRACT).name(), "Comdty");
            OptionCurrent option = new OptionCurrent(contract, attributes.get(OPTION_CONTRACT).name());
            int year = Integer.parseInt(attributes.get(OPTION_YEAR).name());
            int month = Integer.parseInt(attributes.get(OPTION_MONTH).name());
            try {
                YearMonth ym = new YearMonth(year, month);
                Date expiry = option.expiry(ym).expiration(ym);
                info(count + " of " + serieses.size() + ": adding " + expiry + " to series " + series);
                attributes.add(EXPIRY_DATE.value(expiry).createIfNeeded());

            } catch (ExpiryNotFoundException ex) {
                info(count + " of " + serieses.size() + ": skipping " + series + ": no expiry found");
            }
            series.replaceAll(attributes);
            Db.commit();
            
        }
    }

}
