constructor("JSeriesSource", function(jobj = NULL) {
    extend(JObject(), "JSeriesSource", .jobj = jobj)
})

method("by_String", "JSeriesSource", enforceRCC = TRUE, function(static, value = NULL, ...) {
    JSeriesSource(jNew("tsdb/SeriesSource", the(value)))
})

method("by_TimeSeries_DataSource", "JSeriesSource", enforceRCC = TRUE, function(static, series = NULL, source = NULL, ...) {
    JSeriesSource(jNew("tsdb/SeriesSource", .jcast(series$.jobj, "tsdb.TimeSeries"), .jcast(source$.jobj, "tsdb.DataSource")))
})

method("by_String_String", "JSeriesSource", enforceRCC = TRUE, function(static, seriesName = NULL, sourceName = NULL, ...) {
    JSeriesSource(jNew("tsdb/SeriesSource", the(seriesName), the(sourceName)))
})

method("deletePoint_by_Date", "JSeriesSource", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "V", "deletePoint", .jcast(date$.jobj, "java.util.Date"))
})

method("observationValueBefore_by_Date", "JSeriesSource", enforceRCC = TRUE, function(this, date = NULL, ...) {
    JDouble(jobj = jCall(this$.jobj, "Ljava/lang/Double;", "observationValueBefore", .jcast(date$.jobj, "java.util.Date")))
})

method("hasObservation", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasObservation")
})

method("observationValue_by_String", "JSeriesSource", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "D", "observationValue", the(date))
})

method("count", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "count")
})

method("source", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    JDataSource(jobj = jCall(this$.jobj, "Ltsdb/DataSource;", "source"))
})

method("hasObservationToday_by_Date", "JSeriesSource", enforceRCC = TRUE, function(this, now = NULL, ...) {
    jCall(this$.jobj, "Z", "hasObservationToday", .jcast(now$.jobj, "java.util.Date"))
})

method("write_by_String_double", "JSeriesSource", enforceRCC = TRUE, function(this, date = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "write", the(date), theNumeric(value))
})

method("write_by_Date_double", "JSeriesSource", enforceRCC = TRUE, function(this, date = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "write", .jcast(date$.jobj, "java.util.Date"), theNumeric(value))
})

method("name", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("hasObservation_by_Date", "JSeriesSource", enforceRCC = TRUE, function(this, closingTime = NULL, ...) {
    jCall(this$.jobj, "Z", "hasObservation", .jcast(closingTime$.jobj, "java.util.Date"))
})

method("putInto_by_Row_IntColumn_IntColumn", "JSeriesSource", enforceRCC = TRUE, function(this, valueRow = NULL, ts = NULL, ds = NULL, ...) {
    jCall(this$.jobj, "V", "putInto", .jcast(valueRow$.jobj, "db.Row"), .jcast(ts$.jobj, "db.columns.IntColumn"), .jcast(ds$.jobj, "db.columns.IntColumn"))
})

method("firstObservation", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "firstObservation"))
})

method("latestObservation", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "latestObservation"))
})

method("toString", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("observations_by_Date", "JSeriesSource", enforceRCC = TRUE, function(this, d = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(d$.jobj, "java.util.Date")))
})

method("observationValue_by_Date", "JSeriesSource", enforceRCC = TRUE, function(this, d = NULL, ...) {
    jCall(this$.jobj, "D", "observationValue", .jcast(d$.jobj, "java.util.Date"))
})

method("observations_by_Integer", "JSeriesSource", enforceRCC = TRUE, function(this, count = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(count$.jobj, "java.lang.Integer")))
})

method("observations_by_Range_int", "JSeriesSource", enforceRCC = TRUE, function(this, range = NULL, count = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(range$.jobj, "util.Range"), theInteger(count)))
})

method("observations_by_Range", "JSeriesSource", enforceRCC = TRUE, function(this, range = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(range$.jobj, "util.Range")))
})

method("purge", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "purge")
})

method("series", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    JTimeSeries(jobj = jCall(this$.jobj, "Ltsdb/TimeSeries;", "series"))
})

method("matches_by_IntColumn_IntColumn", "JSeriesSource", enforceRCC = TRUE, function(this, seriesId = NULL, sourceId = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(seriesId$.jobj, "db.columns.IntColumn"), .jcast(sourceId$.jobj, "db.columns.IntColumn")))
})

method("observationsMap", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    JTsdbObservations(jobj = jCall(this$.jobj, "Ltsdb/TsdbObservations;", "observationsMap"))
})

method("write_by_Observations", "JSeriesSource", enforceRCC = TRUE, function(this, observations = NULL, ...) {
    jCall(this$.jobj, "V", "write", .jcast(observations$.jobj, "tsdb.Observations"))
})

method("observations", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations"))
})

method("equals_by_Object", "JSeriesSource", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JSeriesSource", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

