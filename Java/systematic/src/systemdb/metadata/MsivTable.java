package systemdb.metadata;

import static db.Column.*;
import static db.clause.Clause.*;
import static db.columns.FunctionColumn.*;
import static db.tables.SystemDB.ExchangeBase.*;
import static db.tables.SystemDB.MarketBase.*;
import static systemdb.metadata.ParameterValuesTable.*;
import static systemdb.metadata.SystemTimeSeriesTable.*;
import static util.Objects.*;

import java.util.*;

import systemdb.data.*;
import db.*;
import db.clause.*;
import db.columns.*;
import db.tables.SystemDB.*;

public class MsivTable extends MSIVBase {
    private static final long serialVersionUID = 1L;
    public static final MsivTable MSIVS = new MsivTable();
    
	public MsivTable() { super("msiv"); }

    public List<MsivRow> msivs(String system) {
        return msivRows(systemMatches(system));
    }
    
    public MsivRow msiv(String market, Siv siv) {
        return the(msivRows(siv.matches(C_SYSTEM, C_INTERVAL, C_VERSION).and(C_MARKET.is(market))));
    }
    
    private List<MsivRow> msivRows(Clause matches) {
        List<MsivRow> result = empty();
        List<Row> rows = rows(matches);
        for (Row row : rows)
            result.add(new MsivRow(row));
        return result;
    }
    
    public List<Siv> sivs(String system) {
        return sivs(C_SYSTEM.is(system));
    }
    
    private List<Siv> sivs(Clause matches) {
        List<Column<?>> columns = columns(C_SYSTEM, C_INTERVAL, C_VERSION);
        List<Row> rows = selectDistinct(columns, matches).rows();
        List<Siv> result = empty();
        for (Row row : rows)
            result.add(new Siv(row.value(C_SYSTEM), row.value(C_INTERVAL), row.value(C_VERSION)));
        return result;
    }
    
    public List<Siv> allSivs() {
        return sivs(TRUE);
    }

    public List<Symbol> symbols(String system) {
        List<Symbol> result = empty();
        Clause matches = MSIVS.systemMatches(system);
        Clause marketJoin = T_MARKET.C_NAME.is(C_MARKET);
        Clause systemJoin = C_MARKET.is(SYSTEM_TS.C_NAME);
        Clause exchangeJoin = SYSTEM_TS.C_EXCHANGE.is(T_EXCHANGE.C_EXCHANGE);
        Clause join = marketJoin.and(systemJoin.and(exchangeJoin));
        FunctionColumn<Double> coalesce = function("coalesce", array(T_MARKET.C_BIGPOINTVALUE, T_EXCHANGE.C_DEFAULTBIGPOINTVALUE));
        columns(C_MARKET, coalesce);
        SelectMultiple select = MSIVS.select(matches.and(join));
        for (Row r : select.rows())
            result.add(new Symbol(r.value(C_MARKET), r.value(coalesce)));
        return result;
    }

    public List<String> markets(String system) {
        return C_MARKET.values(C_SYSTEM.is(system));
    }

	public class MsivRow extends Row implements Comparable<MsivRow> {
	    private static final long serialVersionUID = 1L;
	    public MsivRow(Row row) {
			super(row);
		}
        
        public Siv siv() {
            return new Siv(value(C_SYSTEM), value(C_INTERVAL), value(C_VERSION));
        }

        String marketName() {
            return value(C_MARKET);
        }
        
        public Market market() {
            return new Market(marketName());
        }

        public String name() {
            return value(C_NAME);
        }

        public Map<String, String> params(Pv pv) {
            return VALUES.params(system(), pv);
        }

        String system() {
            return value(C_SYSTEM);
        }

        public List<Pv> livePvs() {
            return MsivLiveHistory.LIVE.pvsForMsiv(this);
        }

        public Clause matches(StringColumn col) {
            return allMatch(C_NAME).and(C_NAME.is(col));
        }

        @Override public int compareTo(MsivRow o) {
            return name().compareTo(o.name());
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = prime;
            result = prime * result + ((name() == null) ? 0 : name().hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (getClass() != obj.getClass()) return false;
            final MsivRow other = (MsivRow) obj;
            if (name() == null) {
                if (other.name() != null) return false;
            } else if (!name().equals(other.name())) return false;
            return true;
        }

        public Cell<?> cell(StringColumn col) {
            return col.with(name());
        }

        public MsivPv with(Pv pv) {
            return new MsivPv(this, pv);
        }
        
	}
	
	public MsivRow forName(String msiv) {
		return new MsivRow(row(C_NAME.is(msiv)));
	}

    public Clause nameMatches(StringColumn msivName) {
        return C_NAME.is(msivName);
    }
    
    public Clause systemMatches(String system) {
        return C_SYSTEM.is(system);
    }

    public Clause matches(Siv siv) {
        return siv.matches(C_SYSTEM, C_INTERVAL, C_VERSION);
    }

    public Siv siv(Row r) {
        return new MsivRow(r).siv();
    }

    public boolean exists(Siv siv, String market) {
        return rowExists(matches(siv).and(C_MARKET.is(market)));
    }
    
    public void insert(String market, Siv siv) {
        Row toInsert = new Row();
        for (Cell<?> c : siv.cells(C_SYSTEM, C_INTERVAL, C_VERSION))
            toInsert.put(c);
        toInsert.put(C_MARKET.with(market));
        toInsert.put(C_NAME.with(market + "_" + siv.sivName("_")));
        insert(toInsert);
    }

}