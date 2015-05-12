package systemdb.metadata;

import static systemdb.metadata.BloombergTagsTable.*;
import static systemdb.metadata.MsivLiveHistory.*;
import static systemdb.metadata.ParameterValuesTable.*;
import static systemdb.metadata.SystemDetailsTable.*;
import static systemdb.metadata.SystemTable.*;
import static tsdb.Attribute.*;
import static tsdb.AttributeValues.*;
import static tsdb.TimeSeriesTable.*;
import static util.Dates.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import systemdb.metadata.SystemDetailsTable.*;
import tsdb.*;
import util.*;
import file.*;

public class LiveSystem {

    private final Siv siv;
    private final Pv pv;
    private SystemDetails details;

    public LiveSystem(Siv siv, Pv pv) {
        this.siv = siv;
        this.pv = pv;
    }

    @Override public String toString() {
        return siv + " " + pv;
    }
    
    public String topicName(String prefix, String suffix) {
        return join(".", prefix, siv.topicName(), pv.name(), suffix); 
    }
    
    public String tradeFileName() {
        return asLong(now()) + "_" + siv.system() + "_" + pv.name() + ".csv";
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pv == null) ? 0 : pv.hashCode());
        result = prime * result + ((siv == null) ? 0 : siv.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final LiveSystem other = (LiveSystem) obj;
        if (pv == null) {
            if (other.pv != null) return false;
        } else if (!pv.equals(other.pv)) return false;
        if (siv == null) {
            if (other.siv != null) return false;
        } else if (!siv.equals(other.siv)) return false;
        return true;
    }

    public SystemDetails details() {
        if (details == null)
            details = DETAILS.liveDetails(siv, pv);
        return details;
    }
    
    public void clearDetailsCache() {
        details = null;
    }

    public QFile file(QDirectory path, String suffix) {
        return path.file(name() + suffix);
    }

    public String name() {
        return siv.name() + "-" + pv.name();
    }

    public Tag repXml(QDirectory path) {
        return repFile(path).xml();
    }

    public QFile repFile(QDirectory path) {
        return file(path, ".rep");
    }

    public int id() {
        return details().id();
    }

    public Map<String, String> parameters() {
        return siv.params(pv);
    }

    public boolean hasDetails() {
        return DETAILS.liveExists(siv, pv);
    }

    public int populateDetailsIfNeeded(boolean runInNativeCurrency) {
        if (hasDetails()) return id();
        return DETAILS.insert(siv, pv, runInNativeCurrency);
    }
    
    public void populateTagIfNeeded(String tag, boolean autoExecute) {
        TAGS.insertIfNeeded(id(), tag, autoExecute);
    }
    
    public void setAutoExecuteTrades(boolean autoExecute) {
        TAGS.setAutoExecuteTrades(id(), autoExecute);
    }
    
    public void setQClassName(String name) {
        SYSTEM.C_QCLASSNAME.updateAll(SYSTEM.C_NAME.is(siv.system()), name);
    }

    public List<Market> markets() {
        return LIVE.markets(siv, pv);
    }
    
    public List<MsivPv> liveMarkets() {
        List<MsivPv> result = empty();
        for (Market market : markets()) 
            result.add(liveMarket(market.name()));
        return result;
    }
    
    public void addLiveMarket(String name, String startDate, String endDate) { 
        LIVE.insert(this, name, dateMaybe(startDate), dateMaybe(endDate));
    }
    
    public void addLiveMarket(String name, Date start, Date end) { 
        LIVE.insert(this, name, start, end);
    }
    
    public void removeAllLiveMarkets() {
        for (MsivPv msivPv : liveMarkets())
            LIVE.deleteAll(msivPv.matches(LIVE.C_MSIV_NAME, LIVE.C_PV_NAME));
    }

    public void insertParameter(String name, String value) {
        VALUES.insert(siv.system(), pv.name(), name, value);
    }

    public String bloombergTag() {
        return TAGS.tag(id());
    }
    
    public boolean autoExecuteTrades() {
        return TAGS.autoExecuteTrades(id());
    }
    
    public Siv siv() { 
        return siv;
    }
    
    public String systemName() {
        return siv().system();
    }
    
    public Pv pv() { 
        return pv;
    }
    
    public MsivPv liveMarket(String market) {
        return siv.with(market).with(pv);
    }

    // another place to find this method, it is a likely place to look for this
    public static LiveSystem liveSystem(int systemId) {
        return SystemDetailsTable.liveSystem(systemId);
    }
    
    public static boolean isAutoExecute(int systemId) {
        return liveSystem(systemId).autoExecuteTrades();
    }
    
    public TimeSeries series(String metric) {
        String seriesName = name() + "_" + metric;
        AttributeValues values = values(
            SYSTEM_ID.value(id()), 
            SIV.value(siv().name()).createIfNeeded(), 
            PV.value(pv().name()).createIfNeeded(), 
            METRIC.value(metric).createIfNeeded()
        );
        if(!TIME_SERIES.exists(seriesName))
            TIME_SERIES.create(seriesName, values);
        return TimeSeries.series(seriesName);
    }

    public String fileName(String marketName) {
        return join("_", siv.sviName("_"), pv.name(), marketName);
    }

}
