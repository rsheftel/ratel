constructor("JDailyTSDBBarLoader", function(jobj = NULL) {
    extend(JObject(), "JDailyTSDBBarLoader", .jobj = jobj)
})

method("by_HistoricalDailyData_DataSource_TimeSeries_TimeSeries_TimeSeries_TimeSeries_TimeSeries_TimeSeries_BarSmith", "JDailyTSDBBarLoader", enforceRCC = TRUE, function(static, data = NULL, source = NULL, open = NULL, high = NULL, low = NULL, close = NULL, volume = NULL, openInterest = NULL, smith = NULL, ...) {
    JDailyTSDBBarLoader(jNew("systemdb/data/bars/DailyTSDBBarLoader", .jcast(data$.jobj, "systemdb.data.HistoricalDailyData"), .jcast(source$.jobj, "tsdb.DataSource"), .jcast(open$.jobj, "tsdb.TimeSeries"), .jcast(high$.jobj, "tsdb.TimeSeries"), .jcast(low$.jobj, "tsdb.TimeSeries"), .jcast(close$.jobj, "tsdb.TimeSeries"), .jcast(volume$.jobj, "tsdb.TimeSeries"), .jcast(openInterest$.jobj, "tsdb.TimeSeries"), .jcast(smith$.jobj, "systemdb.data.bars.BarSmith")))
})

method("count", "JDailyTSDBBarLoader", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "count")
})

method("bars_by_int", "JDailyTSDBBarLoader", enforceRCC = TRUE, function(this, count = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", theInteger(count)))
})

method("bars_by_Range", "JDailyTSDBBarLoader", enforceRCC = TRUE, function(this, range = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", .jcast(range$.jobj, "util.Range")))
})

method("observations_by_Integer", "JDailyTSDBBarLoader", enforceRCC = TRUE, function(this, count = NULL, ...) {
    JTsdbObservations(jobj = jCall(this$.jobj, "Ltsdb/TsdbObservations;", "observations", .jcast(count$.jobj, "java.lang.Integer")))
})

