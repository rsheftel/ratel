package systemdb.metadata;

import static amazon.S3Cache.*;
import static systemdb.data.bars.BasicBarSmith.*;
import static bloomberg.BloombergSession.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Systematic.*;
import static util.Log.*;
import static util.Objects.*;
import static util.QTimeZone.*;
import static util.Strings.*;
import static util.Range.*;

import java.io.*;
import java.util.*;

import systemdb.data.*;
import systemdb.data.bars.*;
import tsdb.*;
import util.*;
import amazon.*;
import amazon.MetaBucket.*;
import bloomberg.*;

import com.bloomberglp.blpapi.*;
import com.bloomberglp.blpapi.Message;

import db.*;
import db.clause.*;
import db.tables.SystemDB.*;
import static systemdb.live.MarketTickersTable.*;

public class BloombergTable extends BloombergBase implements HistoricalProvider {
	static final Range ALL = Range.range("1869/01/01", "2099/01/01");
    public class BloombergDaily extends Row implements HistoricalDailyData {
		private static final String PX_LAST = "PX_LAST";
		private static final long serialVersionUID = 1L;

		public BloombergDaily(Row row) {
			super(row);
		}

		@Override public List<Bar> bars(Range range) {
			BloombergSecurity security = security();
			ObservationsMap<String> observationsMap = new ObservationsMap<String>();
			List<String> fields = list("PX_OPEN", "PX_HIGH", "PX_LOW", PX_LAST, "PX_VOLUME");
			for (String field : fields) 
				observationsMap.add(field, security.observations(field, range));
			List<ProtoBar> protoBars = protoBars(observationsMap, "PX_OPEN", "PX_HIGH", "PX_LOW", PX_LAST, "PX_VOLUME", null);
			return calculator(value(C_CALCULATE_METHOD)).convert(this, protoBars);
		}

		private BloombergSecurity security() {
			return new BloombergSecurity(value(C_SECURITY));
		}

		@Override public List<Bar> lastBars(int count) {
			List<Bar> all = bars(ALL);
			if (all.size() <= count) return all;
			return all.subList(all.size() - count, all.size());
		}

		@Override public Double lastCloseBefore(Date date) {
			Range lastWeek = range(daysAgo(7, date), daysAgo(1, date));
			Observations observations = security().observations(PX_LAST, lastWeek);
			return observations.mostRecentValueMaybe();
		}

	}

	public static class IntradayBarRequest implements Serializable, S3Cacheable<List<Bar>> {
        private static final long serialVersionUID = 2L;
        private final String name;
        private final Range range;
        private final Interval interval;

        public IntradayBarRequest(String name, Range range, Interval interval) {
            this.name = bombNull(name, "name is null");
            this.range = range;
            this.interval = interval;
        }

        @Override public Key key(MetaBucket bucket) {
            return bucket.key("bloomberg.", urlEncode(serialize(this)));
        }

        @Override public List<Bar> response() {
            return BLOOMBERG.intraday(name).bars(range, interval);
        }
        
        @Override public String toString() {
            return "IntradayRequest" + serialVersionUID + ":" + name + " " + range + " " + interval;
        }

    }

    private static final long serialVersionUID = 1L;
    public static final BloombergTable BLOOMBERG = new BloombergTable();
    public static final String LOOKUP_MARKET_TICKER_SENTINEL = "MarketTicker";
    private static final String USE_MID_SENTINEL = "QuantysMid";
    
    public BloombergTable() {
        super("bbg");
    }

    public BloombergIntraday intraday(String symbol) {
        return new BloombergIntraday(row(nameMatches(symbol)));
    }

    private Clause nameMatches(String symbol) {
        return C_NAME.is(symbol);
    }

    public class BloombergIntraday implements LiveDataSource, Serializable, IntradaySource {
        
        private abstract class BloombergDataListener implements BloombergListener {
            private final String security;
            private final String lastField;
            protected final String askField;
            protected final String bidField;
            private final String timeField;
            protected final boolean useMid;
            private final List<String> fields = empty();
            protected final TypedMap values = new TypedMap();
            private String symbolName;
            private CorrelationID correlationId;
            private int retriesLeft = 3;

            public BloombergDataListener(Row row) {
                String proto = row.value(C_SECURITY);
                symbolName = row.value(C_NAME);
                security = proto.equals(LOOKUP_MARKET_TICKER_SENTINEL) ? TICKERS.lookup(symbolName) : proto;
                timeField = row.value(C_TIMEFIELD);
                lastField = row.value(C_LASTFIELD);
                bidField = row.value(C_BIDFIELD);
                askField = row.value(C_ASKFIELD);
                useMid = USE_MID_SENTINEL.equals(lastField);
                fields.add(timeField);
                fields.add("EVENT_TIME"); // bug workaround force bloomberg to provide TIME field
                if (useMid) fields.addAll(list(bidField, askField));
                else fields.add(lastField);
            }
            
            void addFields(String...additionalFields) {
                fields.addAll(list(additionalFields));
            }

            @Override public void onMessage(Message m) {
                Map<String, String> newValues;
                try {
                    Element e = m.asElement();
                    newValues = values(e, security);
                } catch(Exception ex) {
                    throw bomb("error parsing message for " + symbolName + ", " + security + ":\n" + m, ex);
                }
                if(verbose())
                    info("received " + toHumanString(newValues));
                values.putAll(newValues);
                
                if (!hasRequired(timeField)) return;
                if (!useMid && !hasRequired(lastField)) return;
                try {
                    subOnMessage();
                    if (isLoggingTicks()) Log.info("successfully processed tick for " + symbolName);
                } catch(Exception ex) {
                    if(Log.errMessage(ex).contains("not found in") && retriesLeft-- > 0) {
                        BloombergSession.session().unsubscribe(correlationId);
                        correlationId = BloombergSession.session().subscribe(security, commaSep(fields), this);
                    }
                    Log.err("error processing tick for " + symbolName + ", " + security, ex);
                }
            }

            protected abstract void subOnMessage();
            
            
            private boolean hasRequired(String field) {
                if (values.containsKey(field)) return true;
                if (verbose()) info(errorMessage(" not publishing did not have " + field));
                return false;
            }

            private String errorMessage(String error) {
                return security + (useMid ?"(MID)" :"" ) + error + "\n" + toHumanString(values);
            }

            protected double last() {
                if (!useMid) return values.numeric(lastField);
                return (values.numeric(askField) + values.numeric(bidField))/2;
            }
            
            protected Date time() {
                Date result = GMT.toLocalTime(values.timeOn(midnightGMT(), timeField));
                if(result.after(minutesAhead(1, now())))
                    result = daysAgo(1, result);
                return result;
            }

            public void start() {                
                String fieldString = join(",", fields);
                correlationId = BloombergSession.session().subscribe(security, fieldString, this);
            }
            
            public void setValues(Map<String, String> newValues) {
                values.clear();
                values.putAll(newValues);
            }
            
            public String security() {
                return security;
            }
            
        }
        
        public class BloombergTickListener extends BloombergDataListener {
            private String openField;
            private String highField;
            private String lowField;
            private String sizeField;
            private final TickListener listener;
            public BloombergTickListener(Row row, TickListener listener) {
                super(row);
                this.listener = listener;
                openField = row.value(C_OPENFIELD);
                highField = row.value(C_HIGHFIELD);
                lowField = row.value(C_LOWFIELD);
                sizeField = row.value(C_SIZEFIELD);
                addFields(openField, highField, lowField, sizeField);
            }
            
            @Override public void subOnMessage() {
                if (row.value(C_ISFILTERED))
                    if (useMid && (values.numeric(bidField) <= 0 || values.numeric(askField) <= 0)) return;
                    else if (last() <= 0) return;
                listener.onTick(new Tick(
                    last(), 
                    values.long_(sizeField, 0),
                    values.numeric(openField),
                    
                    values.numeric(highField),
                    values.numeric(lowField),
                    time()
                ));
            }


        }
        
        public class BloombergObservationListener extends BloombergDataListener {
            private final ObservationListener listener;
            private BloombergObservationListener(Row row, ObservationListener listener) {
                super(row);
                this.listener = listener;
            }

            @Override public void subOnMessage() {
                listener.onUpdate(time(), last());
            }
        }

        private static final long serialVersionUID = 1L;
        private final Row row;
        public BloombergIntraday(Row row) {
            this.row = row;
        }

        public List<Bar> bars(Range range, Interval interval) {
            IntradayBarRequest amazonRequest = new IntradayBarRequest(row.value(C_NAME), range, interval);
            if(sqsDbMode()) return s3cache().retrieve(amazonRequest);
            BloombergSecurity security = new BloombergSecurity(row.value(C_SECURITY));
            List<Bar> result = security.bars(range, interval);
            return saveResultsIfNeeded(amazonRequest, result);
        }

        @Override public void subscribe(final ObservationListener listener) {
            new BloombergObservationListener(row, listener).start();
        }

        @Override public void subscribe(final TickListener listener) {
            new BloombergTickListener(row, listener).start();
        }

        public BloombergTickListener tickListener(TickListener listener) {
            return new BloombergTickListener(row, listener);
        }



    }

    public void insert(String symbol, String bloombergTicker) {
        insert(symbol, bloombergTicker, null, null, null, null, null, null, null, null);
    }

    public void insert(String symbol, String bloombergTicker, 
        String open, String high, String low, String last, String bid, String ask, String size, String time
    ) {
        insert(
            C_NAME.with(symbol), 
            C_SECURITY.with(bloombergTicker),
            C_OPENFIELD.with(open),
            C_HIGHFIELD.with(high),
            C_LOWFIELD.with(low),
            C_LASTFIELD.with(last),
            C_BIDFIELD.with(bid),
            C_ASKFIELD.with(ask),
            C_SIZEFIELD.with(size),
            C_TIMEFIELD.with(time)
        );
    }

    public void setFiltered(String name, boolean isFiltered) {
        C_ISFILTERED.updateOne(nameMatches(name), isFiltered);
    }

    public void useMids(String name, String bidField, String askField) {
        updateOne(new Row(C_BIDFIELD.with(bidField), C_ASKFIELD.with(askField), C_LASTFIELD.with(USE_MID_SENTINEL)), nameMatches(name));
    }

	@Override
	public HistoricalDailyData dataSource(String name) {
		return new BloombergDaily(row(nameMatches(name)));
	}

	public void setCalculateMethod(String name, String method) {
		C_CALCULATE_METHOD.updateOne(nameMatches(name), method);
	}

}
