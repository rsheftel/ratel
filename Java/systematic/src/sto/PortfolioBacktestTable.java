package sto;

import java.util.*;

import systemdb.metadata.SystemDetailsTable.*;
import db.*;
import db.clause.*;
import db.tables.SystemDB.*;
import static systemdb.metadata.SystemDetailsTable.*;
import static util.Errors.*;

public class PortfolioBacktestTable extends PortfolioBacktestBase {

    private static final long serialVersionUID = 1L;
    public static final PortfolioBacktestTable PORTFOLIO_BACKTEST = new PortfolioBacktestTable();
    
    public PortfolioBacktestTable() {
        super("port_backtest");
    }

    public void insert(SystemDetails details, Portfolio portfolio, WeightedMsiv msiv) {
        insert(
            msiv.cell(C_MSIV_NAME), 
            msiv.cell(C_WEIGHT), 
            C_STODIR.with(details.stoDir()), 
            C_STOID.with(details.stoId()),
            portfolio.cell(C_PORTFOLIONAME)
        );
    }

    public List<String> names(int systemId) {
        return C_PORTFOLIONAME.distinct(matches(systemId));
    }

    private Clause matches(int systemId) {
        return DETAILS.details(systemId).matches(C_STODIR, C_STOID);
    }

    public Portfolio portfolio(int systemId, String name) {
        List<Row> rows = rows(C_PORTFOLIONAME.is(name).and(matches(systemId)));
        bombIf(rows.isEmpty(), "no rows exist in PortfolioBacktest for " + systemId + " " + name);
        Portfolio result = new Portfolio(name);
        for(Row msiv : rows)
            result.add(new WeightedMsiv(msiv.value(C_MSIV_NAME), msiv.value(C_WEIGHT)));
        return result;
    }
}
