package systemdb.data;


import static systemdb.data.AsciiTable.*;
import static systemdb.data.Interval.*;
import static systemdb.metadata.SystemTimeSeriesTable.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static tsdb.DataSource.*;
import static tsdb.TimeSeries.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Range.*;
import static util.Strings.*;
import static util.Times.*;

import java.util.*;

import systemdb.metadata.*;
import tsdb.*;
import util.*;
import db.*;

public class TestSymbol extends DbTestCase {
	
	private final class TestTickListener implements TickListener {
		private Tick tick;

		public void onTick(Tick aTick) {
			this.tick = aTick;
		}

		public void waitForMessage(long timeout) {
			long start = nowMillis();
			while (tick == null && reallyMillisSince(start) < timeout) { sleep(20); }
			bombNull(tick, "tick not captured within " + timeout + " millis.");
		}

		public void reset() {
			tick = null;
		}
	}
	private static class TestObservationListener implements ObservationListener {
	    private Date date;
        private Double value;
        private static int SLEEP_TIME = 50;

        public void onUpdate(Date aDate, double aValue) {
            this.date = aDate;
            this.value = aValue;
	    }

        public void waitForMessage(int timeoutMillis) {
            for(int i = 0; i < timeoutMillis / SLEEP_TIME; i++) {
                if(date != null) break;
                sleep(SLEEP_TIME);
            }
        }
	}

	public void testSymbolSubscribe() throws Exception {
		SYSTEM_TS.insert("testone", "ASCII", "ActiveMQ", "somedangtopic");
		Symbol symbol = new Symbol("testone");
		TestTickListener listener = new TestTickListener();
		symbol.subscribe(listener);
		symbol.jmsLive().publish(new Tick(10.01, 19009, 12.3, 14.56, 9.0, date("2008/06/05 13:10:08")));
		listener.waitForMessage(500);
		assertEquals(10.01, listener.tick.last);
		listener.reset();
		symbol.jmsLive().publish(new Tick(11.01, 19009, 12.3, 14.56, 9.0, now()));
		listener.waitForMessage(500);
		assertEquals(11.01, listener.tick.last);
	}
	
	public void testSymbolHistorical() throws Exception {
	    Symbol symbol = new Symbol("DRC10.ABY5X");
	    assertEmpty(symbol.observations());
	}
	
	public void testBloombergHistorical() throws Exception {
	    SYSTEM_TS.insertBloomberg("testone", "XLF US Equity");
	    Symbol symbol = new Symbol("testone");
	    List<Bar> bars = symbol.bars(range("2009/06/01", "2009/06/08"));
	    assertEquals(new Bar(date("2009/06/01"), 12.3268, 12.5258, 12.1776, 12.297, 191994594L, null), first(bars));
	    List<Bar> fiddyBars = symbol.lastBars(50);
	    assertSize(50, fiddyBars);
	    assertEquals(penultimate(symbol.bars(range(daysAgo(7, midnight()), midnight()))), penultimate(fiddyBars));
	    BloombergTable.BLOOMBERG.setCalculateMethod("testone", "OpenIsPriorClose");
	    bars = symbol.bars(range("2009/06/01", "2009/06/08"));
	    assertEquals(new Bar(date("2009/06/01"), 12.1676, 12.297, 12.1676, 12.297, 191994594L, null), first(bars));
	}
	
	public void functestJeromesSecurity() throws Exception {
		new Symbol("MOO.BBG").bars();
	}
	
	public void testLiveDescription() throws Exception {
	    SYSTEM_TS.insert("testone", "ASCII", "ActiveMQ", "somedangtopic");
	    Symbol symbol = new Symbol("testone");
        assertEquals(new JmsLiveDescription("somedangtopic", "ActiveMQ", "timestamp", "value"), symbol.live());
	}

	public void testSymbolSubscribeObservations() throws Exception {
	    SYSTEM_TS.insert("testone", "ASCII", "ActiveMQ", "somedangtopic");
	    Symbol symbol = new Symbol("testone");
	    TestObservationListener listener = new TestObservationListener();
        symbol.subscribe(listener);
        symbol.jmsLive().publish(97.1, date("2008/08/06"));
        listener.waitForMessage(500);
        assertEquals(date("2008/08/06"), listener.date);
        assertEquals(97.1, listener.value);
    }
	
	@Deprecated
	public void functestSymbolSubscribeBloomberg() throws Exception { // functional test only
	    //Log.setVerboseLogging(true);
	    SYSTEM_TS.insertBloomberg("testone", "GCA Comdty", "OPEN_TDY", "HIGH_TDY", "LOW_TDY", "LAST_PRICE", "SIZE_LAST_TRADE", "TIME");
	    SYSTEM_TS.insertBloomberg("testtwo", "TYA Comdty", "OPEN_TDY", "HIGH_TDY", "LOW_TDY", "BID_TDY", "ASK_TDY", "SIZE_LAST_TRADE", "BID_ASK_TIME");
	    SYSTEM_TS.insertBloomberg("testthree", "FVA Comdty", "OPEN_TDY", "HIGH_TDY", "LOW_TDY", "LAST_PRICE", "SIZE_LAST_TRADE", "TIME");
	    final Symbol symbol = new Symbol("testone");
	    //final Symbol symbol2 = new Symbol("testtwo");
	    final Symbol symbol3 = new Symbol("testthree");
	    symbol.subscribe(new TickListener() {
            @Override public void onTick(Tick tick) {
                info(symbol + " received tick:  " + tick);
            }
	    });
//        symbol2.subscribe(new TickListener() {
//            @Override public void onTick(Tick tick) {
//                info(symbol2 + " received tick: " + tick);
//            }
//	    });
	    symbol3.subscribe(new ObservationListener() {
            @Override public void onUpdate(Date date, double value) {
                info(symbol3 + " received value: " + paren(commaSep(ymdHuman(date), value)));
            }
	    });
//        symbol2.subscribe(new ObservationListener() {
//            @Override public void onUpdate(Date date, double value) {
//                info(symbol2 + " received value: " + paren(commaSep(ymdHuman(date), value)));
//            }
//	    });
//        symbol3.subscribe(new ObservationListener() {
//            @Override public void onUpdate(Date date, double value) {
//                info(symbol3 + " received value: " + paren(commaSep(ymdHuman(date), value)));
//            }
//        });
	    sleepSeconds(6);
	}
	
	public void testNullContractSize() throws Exception {
	    Symbol s = new Symbol("testone");
	    try {
	        s.bigPointValue();
	        fail();
	    } catch (Exception success) {
	        assertMatches("contract size not defined", success);
	    }
    }
	
	public void testSymbolType() throws Exception {
	    assertEquals("Future", new Symbol("RE.TEST.TY.1C").type());
    }
	
	public void testSymbolIntradayBars() throws Exception {
	    SYSTEM_TS.insertBloomberg("testone", "TYA Comdty");
	    Symbol symbol = new Symbol("testone");
	    Date start = hoursAhead(10, businessDaysAgo(1, midnight(), "nyb"));
	    Date end = hoursAhead(1, start);
	    List<Bar> hourly = symbol.bars(range(start, end), HOURLY);
	    List<Bar> minutely = symbol.bars(range(start, end), MINUTE);
	    double open = first(minutely).open();
	    double close = last(minutely).close();
	    double high = first(minutely).high();
	    double low = first(minutely).low();
	    long volume = 0;
	    for (Bar bar : minutely) {
            if(bar.high() > high) high = bar.high();
            if(bar.low() < low) low = bar.low();
            volume += bar.volume();
        }
	    // volume doesn't quite match up...
	    assertEquals(the(hourly), new Bar(end, open, high, low, close, the(hourly).volume(), null));
    }
	
	public void testAsciiIntradayBars() throws Exception {
	    SYSTEM_TS.insertAsciiIntraday("testone", "test/systemdb/IntradayAsciiTest.csv");
	    Symbol symbol = new Symbol("testone");
	    List<Bar> minuteBars = symbol.bars(MINUTE);
	    assertSize(19, minuteBars);
	    // 2000/02/02 11:31:00,1607.5000,1610.7500,1606.0000,1609.2500,1151,0
	    assertEquals(new Bar(date("2000/02/02 11:31:00"), 1607.5, 1610.75, 1606.0, 1609.25, 1151L, null), second(minuteBars));
	    List<Bar> fiveMinuteBars = symbol.bars(FIVE_MINUTES);
	    assertSize(5, fiveMinuteBars);
	    assertEquals(new Bar(date("2000/02/02 11:30:00"), 1605.7500,1608.0000,1604.7500,1607.7500,841L, null), first(fiveMinuteBars));
	    assertEquals(new Bar(date("2000/02/02 11:35:00"), 1607.5, 1610.75, 1602.25, 1608.5, 3843L, null), second(fiveMinuteBars));
	    assertEquals(new Bar(date("2000/02/02 11:40:00"), 1609.0, 1616.75, 1597.5, 1602.0, 9363L, null), third(fiveMinuteBars));
	    assertEquals(new Bar(date("2000/02/02 11:50:00"), 1602, 1606, 1600.75, 1606, 651L, null), fourth(fiveMinuteBars));
	    assertEquals(new Bar(date("2000/02/03 11:35:00"), 1606, 1606, 1603, 1604.5, 68L, null), fifth(fiveMinuteBars));
    }
	
	public void testDailyGoesToDailySource() throws Exception {
	    SYSTEM_TS.insertAsciiIntraday("testone", "test/systemdb/IntradayAsciiTest.csv");
	    try {
	        new Symbol("testone").bars(DAILY);
	        fail();
	    } catch (Exception success) {
	        assertMatches("valid source", success);
	    }
	    SYSTEM_TS.insert("testtwo", ASC11, ACTIVE_MQ);
        SYSTEM_ASCII.insert("testtwo", "test/systemdb/DailyAsciiTest.csv", true, 1);
        new Symbol("testtwo").bars(DAILY);
	}

	
	public void functestSymbolMinuteBarsRepeatedly() throws Exception {
	    SYSTEM_TS.insertBloomberg("testone", "FV1 Comdty");
	    Symbol symbol = new Symbol("testone");
	    Date start = hoursAhead(10, businessDaysAgo(1, midnight(), "nyb"));
	    Date end = minutesAhead(1, start);
	    Bar one = the(symbol.bars(range(start, end), MINUTE));
	    Bar two = one;
	    while(one.equals(two))
	        two = the(symbol.bars(range(start, end), MINUTE));
	    bomb("one != two:\n" + one + "\n" + two);
	}
	
	public void functestIntradayFullRange() throws Exception {
	    SYSTEM_TS.insertBloomberg("testone", "XLE US Equity");
        Symbol symbol = new Symbol("testone");
        List<Bar> bars = symbol.bars(new Range(date("2008/08/01"), tomorrow()), FIVE_MINUTES);
        info("first: " + first(bars).date());
        info("last: " + last(bars).date());
        info("count: " + bars.size());
    }
	
	public void functestty1c() throws Exception {
	    SYSTEM_TS.insertBloomberg("testone", "TY1 Comdty");
	    Symbol symbol = new Symbol("testone");
	    Date start = midnight();
	    Date end = daysAhead(1, start);
	    info("bars: " + symbol.bars(range(start, end), MINUTE));
    }
	
	public void testAdjustment() throws Exception {
	    Symbol test = new Symbol("RE.TEST.TY.1C");
	    Bar bar = the(test.lastBars(1));
	    SYSTEM_TS.setUseAdjustment(test.name(), true);
	    assertEquals(bar, the(test.lastBars(1)));
	    adjust(test, bar, "low", 2.0, DAILY);
	    adjust(test, bar, "high", 3.0, DAILY);
	    adjust(test, bar, "close", -5.0, DAILY);
	    adjust(test, bar, "open", -1.5, DAILY);
        assertEquals(bar.low() + 2.0, the(test.lastBars(1)).low());
        assertEquals(bar.high() + 3.0, the(last(test.bars())).high());
        assertEquals(bar.close() - 5.0, the(test.bars(range("2008/04/18"))).close());
        assertEquals(bar.open() - 1.5, the(test.lastBars(1)).open());
    }
	
	public void testIntradayAdjustment() throws Exception {
	    SYSTEM_TS.insertAsciiIntraday("testone", "test/systemdb/IntradayAsciiTest.csv");
	    MarketTable.MARKET.insert("testone", 0.0);
	    Symbol test = new Symbol("testone");
	    Bar first = first(test.bars(FIVE_MINUTES));
	    Bar second = second(test.bars(FIVE_MINUTES));
	    SYSTEM_TS.setUseAdjustment(test.name(), true);
	    adjust(test, first, "close", 2.5, FIVE_MINUTES);
	    adjust(test, second, "close", 3.5, FIVE_MINUTES);
	    assertEquals(first.close() + 2.5, first(test.bars(FIVE_MINUTES)).close());
	    assertEquals(second.close() + 3.5, second(test.bars(FIVE_MINUTES)).close());
	    
    }

    private void adjust(Symbol symbol, Bar bar, String quoteType, double amount, Interval interval) {
        TimeSeries adjustment = series("adjustment_" + symbol.name() + "_" + interval.name() + "_" + quoteType);
        if(!adjustment.exists())
            adjustment.create(values(
    	        INSTRUMENT.value("adjustment"),
    	        MARKET.value(symbol.name()),
    	        INTERVAL.value(interval.name()),
    	        QUOTE_TYPE.value(quoteType)
            ));
        adjustment.with(INTERNAL).write(bar.date(), amount);
    }

}
