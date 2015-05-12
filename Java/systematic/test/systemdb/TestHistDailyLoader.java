package systemdb;

import static systemdb.data.AsciiTable.*;
import static systemdb.metadata.SystemTSDBTable.*;
import static systemdb.metadata.SystemTimeSeriesTable.*;
import static tsdb.DataSource.*;
import static util.Dates.*;
import static util.Objects.*;
import static util.Range.*;

import java.util.*;

import systemdb.data.*;
import systemdb.data.bars.*;
import tsdb.*;
import db.*;

public class TestHistDailyLoader extends DbTestCase {
    public void testHistDailyAscii() throws Exception {
        String name = "TEST.TEST";
        setUpAsciiTest(name);
        Symbol symbol = new Symbol(name);
        assertEquals(5249, symbol.barCount());
        List<Bar> bars = symbol.bars(range("1980/01/01", "2008/05/01"));
        assertEquals(
            new Bar(date("1987/06/01"), 394.375, 395.0, 387.96875, 388.59375, 20652L, 63332L), 
            first(bars)
        );
        assertSize(5249, bars);
        bars = symbol.bars(range("2001/04/19", "2001/06/29"));
        assertSize(51, bars);
        assertEquals(
            new Bar(date("2001/04/19"), 819.375,821.875,811.25,813.4375,248902L,617845L), 
            first(bars)
        ); 
        assertEquals(
            new Bar(date("2001/06/29"), 815.3125,816.09375,807.96875,810.15625,295931L,513045L), 
            last(bars)
        );
        bars = symbol.lastBars(50);
        assertSize(50, bars);
        assertEquals(date("2008/04/18"), last(bars).date());
        assertEquals(date("2008/02/07"), first(bars).date());
        bars = symbol.lastBars(10000);
        assertSize(5249, bars);
        assertEquals(date("1987/06/01"), symbol.firstBarDate());
        assertEquals(date("2008/04/18"), symbol.lastBarDate());
    }

    private void setUpAsciiTest(String name) {
        SYSTEM_TS.insert(name, ASC11, ACTIVE_MQ);
        SYSTEM_ASCII.insert(name, "test/systemdb/DailyAsciiTest.csv", true, 10);
    }
    
    public void testCloses() throws Exception {
        String name = "TEST.TEST";
        SYSTEM_TS.insert(name, TSDB, ACTIVE_MQ);
        SYSTEM_SERIES_DATA.insert(name, YAHOO, "aapl close");
        Symbol symbol = new Symbol(name);
        Observations o = assertSize(4999, symbol.observations(range("1987/06/01", "2008/05/01")));
        Date earliest = first(o.times());
        assertEquals(date("1987/06/01"), earliest);
        assertEquals(77.75, o.value(earliest));
        assertSize(5688, symbol.observations());
    }
    
    public void testHistDailyTsdb() throws Exception {
        String name = "TEST.TEST";
        SYSTEM_TS.insert(name, TSDB, ACTIVE_MQ);
        SYSTEM_SERIES_DATA.insert(name, YAHOO, "aapl close", "aapl open", "aapl high", "aapl low", "aapl volume", null);
        Symbol symbol = new Symbol(name);
        assertEquals(5688, symbol.barCount());
        List<Bar> bars = symbol.bars(range("1987/06/01", "2008/05/01"));
        assertEquals(
            new Bar(date("1987/06/01"), 79.5, 79.5, 77.5, 77.75, 2984000L, null), 
            first(bars)
        );
        assertSize(4999, bars);
        bars = symbol.bars(range("2001/04/19", "2001/06/29"));
        assertSize(51, bars);

        bars = symbol.lastBars(50);
        assertSize(50, bars);
        assertEquals(date("2007/01/12"), first(bars).date());
        assertEquals(date("2007/03/26"), last(bars).date());
        bars = symbol.lastBars(10000);
        assertSize(5688, bars);
        assertEquals(date("1984/09/07"), symbol.firstBarDate());
        assertEquals(date("2007/03/26"), symbol.lastBarDate());
    }
    
    public void testAllCloseHistDailyTsdb() throws Exception {
        String name = "TEST.TEST";
        SYSTEM_TS.insert(name, TSDB, ACTIVE_MQ);
        SYSTEM_SERIES_DATA.insert(name, YAHOO, "aapl close");
        Symbol symbol = new Symbol(name);
        List<Bar> bars = symbol.lastBars(50);
        assertSize(50, bars);
    }
    
    public void testOpenIsPriorClose() throws Exception {
        String name = "TEST.TEST";
        SYSTEM_TS.insert(name, TSDB, ACTIVE_MQ);
        SYSTEM_SERIES_DATA.insert(name, YAHOO, "aapl close", new OpenIsPriorClose());
        Symbol symbol = new Symbol(name);
        List<Bar> bars = symbol.lastBars(50);
        assertSize(50, bars);
        assertEquals(
            "\n" + new Bar(date("2007/03/26"), 93.52, 95.85, 93.52, 95.85, null, null) + "\n", 
            "\n" + last(bars) + "\n"
        );
        bars = symbol.bars(range("1980/01/01", "2008/05/01"));
        assertEquals(
            new Bar(date("1984/09/10"), 26.5, 26.5, 26.37, 26.37, null, null), 
            first(bars)
        );
        assertSize(5687, bars);
        bars = symbol.bars(range("1987/06/01", "2008/05/01"));
        assertEquals(
            new Bar(date("1987/06/01"), 79.0, 79.0, 77.75, 77.75, null, null), 
            first(bars)
        );
        bars = symbol.bars(range("1984/09/07", "2007/03/26"));
        assertSize(5687, bars);
        List<Bar> bars2 = symbol.bars(range("1984/09/10", "2007/03/26"));
        assertEquals(bars, bars2);
        assertEquals(bars.size(), symbol.barCount());
    }
    

    public void testOmitIncomplete() throws Exception {
        String name = "TEST.TEST";
        SYSTEM_TS.insert(name, TSDB, ACTIVE_MQ);
        SYSTEM_SERIES_DATA.insert(name, YAHOO, 
            "aapl close", "aapl open", "aapl high", "aapl low",
            "aapl volume", null,
            new OmitIncomplete());
        
        SeriesSource yahooAapl = YAHOO.with("aapl close");
        Observations aapl = yahooAapl.observations();
        assertEquals(5688, aapl.size());
        aapl.remove("2007/03/23 14:00:00");
        assertEquals(5687, aapl.size());
        yahooAapl.series().purgeAllData();
        yahooAapl.series().write(YAHOO, aapl);
        assertEquals(5687, yahooAapl.observations().size());

        Symbol symbol = new Symbol(name);
        assertEquals(5687, symbol.barCount());
        List<Bar> bars = symbol.bars(range("1987/06/01", "2008/05/01"));
        assertEquals(
            new Bar(date("1987/06/01"), 79.5, 79.5, 77.5, 77.75, 2984000L, null), 
            first(bars)
        );
        assertSize(4998, bars);
        bars = symbol.bars(range("2001/04/19", "2001/06/29"));
        assertSize(51, bars);

        bars = symbol.lastBars(50);
        assertSize(50, bars);
        assertEquals(date("2007/01/11"), first(bars).date()); // was 2007/01/12, should have dropped the 3/23 date above
        assertEquals(date("2007/03/26"), last(bars).date());
        bars = symbol.lastBars(10000);
        assertSize(5687, bars);
        assertEquals(date("1984/09/07"), symbol.firstBarDate());
        assertEquals(date("2007/03/26"), symbol.lastBarDate());        
    }
}
