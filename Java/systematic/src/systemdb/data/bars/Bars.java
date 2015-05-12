package systemdb.data.bars;

import static java.lang.Math.*;
import static java.util.Collections.*;
import static util.Arguments.*;
import static util.Dates.*;
import static util.Errors.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Range.*;
import static util.Sequence.*;

import java.util.*;

import systemdb.data.*;
import util.*;

public class Bars {

    public static void main(String[] ss) {
        doNotDebugSqlForever();
        Arguments args = arguments(ss, list("symbol", "head", "tail", "oldestLast", "start", "end", "interval"));
        String name = args.get("symbol");
        Symbol symbol = new Symbol(name);
        
        int head = args.get("head", 0);
        int tail = args.get("tail", 0);
        Date startDate = args.get("start", (Date) null);
        Date endDate= args.get("end", (Date) null);
        boolean oldestLast = args.get("oldestLast", false);
        Interval interval = Interval.lookup(args.get("interval", "daily"));
        
        List<Bar> bars = symbol.bars(range(startDate, endDate), interval);
        bombIf(head > 0 && tail > 0, "can't specify non-zero values for both head and tail");
        bombIf(bars.isEmpty(), "no bars for " + symbol);

        if (head > 0) bars = bars.subList(0, min(head, bars.size()));
        if (tail > 0) {
            int start = bars.size() - tail;
            bars = bars.subList(start < 0 ? 0 : start, bars.size());
        }
        if (oldestLast) reverse(bars);
        info("");
        for(Bar bar : bars) out(bar);
    }

    private static void out(Bar b) { 
        System.out.printf("%10s %12.2f %12.2f %12.2f %12.2f %10d %10d%n", 
            ymdHuman(b.date()),
            b.open(),
            b.high(),
            b.low(),
            b.close(),
            b.volume() == null ? -1 : b.volume(),
            b.openInterest() == null ? -1 : b.openInterest()
        );
    }

	public static RBarData rBars(List<Bar> bars) {
		RBarData result = new RBarData();
	    result.open = new double[bars.size()];
	    result.close = new double[bars.size()];
	    result.high = new double[bars.size()];
	    result.low = new double[bars.size()];
	    result.openInterest = new long[bars.size()];
	    result.volume = new long[bars.size()];
	    result.dates = new long[bars.size()];
	    for (Integer i : along(bars)) {
	        Bar bar = bars.get(i);
	        result.open[i] = bar.open();
	        result.high[i] = bar.high();
	        result.low[i] = bar.low();
	        result.close[i] = bar.close();
	        result.dates[i] = bar.date().getTime();
	        result.openInterest[i] = bar.rOpenInterest(); 
	        result.volume[i] = bar.rVolume();
	    }
	    return result;
	}
    
}
