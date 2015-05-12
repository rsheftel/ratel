package systemdb.metadata;

import static systemdb.metadata.MarketHistoryTable.*;
import static systemdb.metadata.SystemTimeSeriesTable.*;
import static systemdb.metadata.MarketTable.*;
import static util.Dates.*;
import static util.Index.*;

import java.util.*;

import systemdb.data.*;
import util.*;

public class Market extends Symbol {

    private static final long serialVersionUID = 1L;

    public Market(String name) {
        super(name, contractSize(name));
    }

    public static double contractSize(String name) { // used in R code
        return MARKET.bigPointValue(name);
    }

    
    public String exchange() {
        return SYSTEM_TS.exchange(name());
    }

    public double fixedSlippage() {
        return MARKET.fixedSlippage(name());
    }

}
