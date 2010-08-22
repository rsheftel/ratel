constructor("JDataSource", function(jobj = NULL) {
    extend(JObject(), "JDataSource", .jobj = jobj)
})

method("by_String", "JDataSource", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JDataSource(jNew("tsdb/DataSource", the(name)))
})

method("allSources", "JDataSource", enforceRCC = TRUE, function(static, ...) {
    JList(jobj = jCall("tsdb/DataSource", "Ljava/util/List;", "allSources"))
})

method("with_by_String", "JDataSource", enforceRCC = TRUE, function(this, seriesName = NULL, ...) {
    JSeriesSource(jobj = jCall(this$.jobj, "Ltsdb/SeriesSource;", "with", the(seriesName)))
})

method("with_by_TimeSeries", "JDataSource", enforceRCC = TRUE, function(this, series = NULL, ...) {
    JSeriesSource(jobj = jCall(this$.jobj, "Ltsdb/SeriesSource;", "with", .jcast(series$.jobj, "tsdb.TimeSeries")))
})

method("is_by_IntColumn", "JDataSource", enforceRCC = TRUE, function(this, sourceId = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "is", .jcast(sourceId$.jobj, "db.columns.IntColumn")))
})

method("toString", "JDataSource", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("id", "JDataSource", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "id")
})

method("observations_by_TimeSeries_Range", "JDataSource", enforceRCC = TRUE, function(this, series = NULL, range = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(series$.jobj, "tsdb.TimeSeries"), .jcast(range$.jobj, "util.Range")))
})

method("observations_by_TimeSeries", "JDataSource", enforceRCC = TRUE, function(this, series = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(series$.jobj, "tsdb.TimeSeries")))
})

method("source_by_String", "JDataSource", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JDataSource(jobj = jCall("tsdb/DataSource", "Ltsdb/DataSource;", "source", the(name)))
})

method("MODEL_JPMORGAN_2008", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...MODEL_JPMORGAN_2008, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "MODEL_JPMORGAN_2008")), log = FALSE)
})

method("LEHMAN", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...LEHMAN, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "LEHMAN")), log = FALSE)
})

method("BLOOMBERG_TEST", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...BLOOMBERG_TEST, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "BLOOMBERG_TEST")), log = FALSE)
})

method("BLOOMBERG_BBAM", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...BLOOMBERG_BBAM, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "BLOOMBERG_BBAM")), log = FALSE)
})

method("BLOOMBERG_CMN3", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...BLOOMBERG_CMN3, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "BLOOMBERG_CMN3")), log = FALSE)
})

method("BLOOMBERG_BBT3", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...BLOOMBERG_BBT3, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "BLOOMBERG_BBT3")), log = FALSE)
})

method("BLOOMBERG", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...BLOOMBERG, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "BLOOMBERG")), log = FALSE)
})

method("JPMORGAN_TEST", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...JPMORGAN_TEST, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "JPMORGAN_TEST")), log = FALSE)
})

method("JPMORGAN", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...JPMORGAN, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "JPMORGAN")), log = FALSE)
})

method("IVYDB", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...IVYDB, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "IVYDB")), log = FALSE)
})

method("YAHOO", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...YAHOO, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "YAHOO")), log = FALSE)
})

method("FINANCIAL_CALENDAR", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...FINANCIAL_CALENDAR, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "FINANCIAL_CALENDAR")), log = FALSE)
})

method("TEST_SOURCE", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...TEST_SOURCE, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "TEST_SOURCE")), log = FALSE)
})

method("INTERNAL_TEST", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...INTERNAL_TEST, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "INTERNAL_TEST")), log = FALSE)
})

method("INTERNAL", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...INTERNAL, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "INTERNAL")), log = FALSE)
})

method("MARKIT_TEST", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...MARKIT_TEST, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "MARKIT_TEST")), log = FALSE)
})

method("MARKIT", "JDataSource", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSource$...MARKIT, JDataSource(jobj = jField("tsdb/DataSource", "Ltsdb/DataSource;", "MARKIT")), log = FALSE)
})

