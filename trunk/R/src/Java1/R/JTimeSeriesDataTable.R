constructor("JTimeSeriesDataTable", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesDataTable", .jobj = jobj)
})

method("by", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, ...) {
    JTimeSeriesDataTable(jNew("tsdb/TimeSeriesDataTable"))
})

method("by_String", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, alias = NULL, ...) {
    JTimeSeriesDataTable(jNew("tsdb/TimeSeriesDataTable", the(alias)))
})

method("deletePoint_by_DataSource_TimeSeries_Date", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, series = NULL, date = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "deletePoint", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(series$.jobj, "tsdb.TimeSeries"), .jcast(date$.jobj, "java.util.Date"))
})

method("addIndicesAndKeys_by_TimeSeriesDataTable", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, data = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "addIndicesAndKeys", .jcast(data$.jobj, "tsdb.TimeSeriesDataTable"))
})

method("createIfNeeded_by_String", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JTimeSeriesDataTable(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TimeSeriesDataTable;", "createIfNeeded", the(name)))
})

method("createPrimaryKey", "JTimeSeriesDataTable", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "createPrimaryKey")
})

method("populateSeriesLookupTable_by_Range_DataSource_AttributeValues", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, range = NULL, source = NULL, values = NULL, ...) {
    JSeriesIdsBase(jobj = jCall("tsdb/TimeSeriesDataTable", "Ldb/temptables/TSDB/SeriesIdsBase;", "populateSeriesLookupTable", .jcast(range$.jobj, "util.Range"), .jcast(source$.jobj, "tsdb.DataSource"), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("purgeAllData_by_TimeSeries", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, series = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "purgeAllData", .jcast(series$.jobj, "tsdb.TimeSeries"))
})

method("hasData_by_int", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, id = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "Z", "hasData", theInteger(id))
})

method("copy_by_SelectOne_Range_DataSource_DataSource_int", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesLookup = NULL, range = NULL, from = NULL, to = NULL, hour = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "copy", .jcast(seriesLookup$.jobj, "db.SelectOne"), .jcast(range$.jobj, "util.Range"), .jcast(from$.jobj, "tsdb.DataSource"), .jcast(to$.jobj, "tsdb.DataSource"), theInteger(hour))
})

method("copy_by_SelectOne_Range_DataSource_DataSource", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesLookup = NULL, range = NULL, from = NULL, to = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "copy", .jcast(seriesLookup$.jobj, "db.SelectOne"), .jcast(range$.jobj, "util.Range"), .jcast(from$.jobj, "tsdb.DataSource"), .jcast(to$.jobj, "tsdb.DataSource"))
})

method("purge_by_SelectOne_DataSource_Range", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesLookup = NULL, source = NULL, range = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "purge", .jcast(seriesLookup$.jobj, "db.SelectOne"), .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range"))
})

method("seriesNotPopulated_by_SelectOne_DataSource_Range", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesLookup = NULL, source = NULL, range = NULL, ...) {
    JList(jobj = jCall("tsdb/TimeSeriesDataTable", "Ljava/util/List;", "seriesNotPopulated", .jcast(seriesLookup$.jobj, "db.SelectOne"), .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range")))
})

method("allExist_by_SelectOne_DataSource_Range", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesLookup = NULL, source = NULL, range = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "Z", "allExist", .jcast(seriesLookup$.jobj, "db.SelectOne"), .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range"))
})

method("existsNonStatic_by_SelectOne_DataSource_Range", "JTimeSeriesDataTable", enforceRCC = TRUE, function(this, seriesLookup = NULL, source = NULL, range = NULL, ...) {
    jCall(this$.jobj, "Z", "existsNonStatic", .jcast(seriesLookup$.jobj, "db.SelectOne"), .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range"))
})

method("anyExist_by_SelectOne_DataSource_Range", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesLookup = NULL, source = NULL, range = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "Z", "anyExist", .jcast(seriesLookup$.jobj, "db.SelectOne"), .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range"))
})

method("purge_by_SeriesSource", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, ss = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "purge", .jcast(ss$.jobj, "tsdb.SeriesSource"))
})

method("firstObservation_by_SeriesSource", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, ss = NULL, ...) {
    JObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/Observations;", "firstObservation", .jcast(ss$.jobj, "tsdb.SeriesSource")))
})

method("latestObservation_by_SeriesSource", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, ss = NULL, ...) {
    JObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/Observations;", "latestObservation", .jcast(ss$.jobj, "tsdb.SeriesSource")))
})

method("writeUsingTempWithCommits_by_List", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, rows = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "writeUsingTempWithCommits", .jcast(rows$.jobj, "java.util.List"))
})

method("writeFromR_by_StringArray_StringArray_longArray_doubleArray", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesNames = NULL, sourceNames = NULL, timesMillis = NULL, values = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "writeFromR", jArray(seriesNames, "[Ljava/lang/String;"), jArray(sourceNames, "[Ljava/lang/String;"), jArray(.jlong(timesMillis), "[J"), jArray(as.numeric(values), "[D"))
})

method("observationRows_by_int_int_Observations", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesId = NULL, sourceId = NULL, observations = NULL, ...) {
    JList(jobj = jCall("tsdb/TimeSeriesDataTable", "Ljava/util/List;", "observationRows", theInteger(seriesId), theInteger(sourceId), .jcast(observations$.jobj, "tsdb.Observations")))
})

method("observationRow_by_int_int_Observations", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesId = NULL, sourceId = NULL, observations = NULL, ...) {
    JRow(jobj = jCall("tsdb/TimeSeriesDataTable", "Ldb/Row;", "observationRow", theInteger(seriesId), theInteger(sourceId), .jcast(observations$.jobj, "tsdb.Observations")))
})

method("main_by_StringArray", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("checkTables", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "checkTables")
})

method("write_by_int_int_Observations", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, seriesId = NULL, sourceId = NULL, observations = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "V", "write", theInteger(seriesId), theInteger(sourceId), .jcast(observations$.jobj, "tsdb.Observations"))
})

method("observationsMap_by_DataSource_int_AttributeValues", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, count = NULL, values = NULL, ...) {
    JTsdbObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TsdbObservations;", "observationsMap", .jcast(source$.jobj, "tsdb.DataSource"), theInteger(count), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("observationsMap_by_DataSource_int_TimeSeriesArray", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, count = NULL, serieses = NULL, ...) {
    JTsdbObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TsdbObservations;", "observationsMap", .jcast(source$.jobj, "tsdb.DataSource"), theInteger(count),  {
        if(inherits(serieses, "jarrayRef")) {
            serieses <- .jevalArray(serieses)
        }
        jArray(lapply(serieses, function(x) {
            x$.jobj
        }), "tsdb/TimeSeries")
    }))
})

method("observationsMap_by_Range_TimeSeriesArray", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, range = NULL, serieses = NULL, ...) {
    JTsdbObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TsdbObservations;", "observationsMap", .jcast(range$.jobj, "util.Range"),  {
        if(inherits(serieses, "jarrayRef")) {
            serieses <- .jevalArray(serieses)
        }
        jArray(lapply(serieses, function(x) {
            x$.jobj
        }), "tsdb/TimeSeries")
    }))
})

method("observationsMap_by_Range_AttributeValues", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, range = NULL, values = NULL, ...) {
    JTsdbObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TsdbObservations;", "observationsMap", .jcast(range$.jobj, "util.Range"), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("observationsMap_by_DataSource_Range_TimeSeriesArray", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, range = NULL, serieses = NULL, ...) {
    JTsdbObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TsdbObservations;", "observationsMap", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range"),  {
        if(inherits(serieses, "jarrayRef")) {
            serieses <- .jevalArray(serieses)
        }
        jArray(lapply(serieses, function(x) {
            x$.jobj
        }), "tsdb/TimeSeries")
    }))
})

method("observationsMap_by_DataSource_TimeSeriesArray", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, serieses = NULL, ...) {
    JTsdbObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TsdbObservations;", "observationsMap", .jcast(source$.jobj, "tsdb.DataSource"),  {
        if(inherits(serieses, "jarrayRef")) {
            serieses <- .jevalArray(serieses)
        }
        jArray(lapply(serieses, function(x) {
            x$.jobj
        }), "tsdb/TimeSeries")
    }))
})

method("observationsMap_by_DataSource_Range_AttributeValues", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, range = NULL, values = NULL, ...) {
    JTsdbObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TsdbObservations;", "observationsMap", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range"), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("observationValueBefore_by_DataSource_TimeSeries_Date", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, series = NULL, date = NULL, ...) {
    JDouble(jobj = jCall("tsdb/TimeSeriesDataTable", "Ljava/lang/Double;", "observationValueBefore", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(series$.jobj, "tsdb.TimeSeries"), .jcast(date$.jobj, "java.util.Date")))
})

method("observationsMap_by_DataSource_AttributeValues", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, values = NULL, ...) {
    JTsdbObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TsdbObservations;", "observationsMap", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("observations_by_DataSource_Range_int_TimeSeries", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, range = NULL, count = NULL, series = NULL, ...) {
    JObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range"), theInteger(count), .jcast(series$.jobj, "tsdb.TimeSeries")))
})

method("observations_by_DataSource_Integer_TimeSeries", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, count = NULL, series = NULL, ...) {
    JObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(count$.jobj, "java.lang.Integer"), .jcast(series$.jobj, "tsdb.TimeSeries")))
})

method("observations_by_DataSource_Range_TimeSeries", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, dateRange = NULL, series = NULL, ...) {
    JObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(dateRange$.jobj, "util.Range"), .jcast(series$.jobj, "tsdb.TimeSeries")))
})

method("observationsCount_by_DataSource_TimeSeries", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, series = NULL, ...) {
    jCall("tsdb/TimeSeriesDataTable", "I", "observationsCount", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(series$.jobj, "tsdb.TimeSeries"))
})

method("observations_by_DataSource_TimeSeries", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, series = NULL, ...) {
    JObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(series$.jobj, "tsdb.TimeSeries")))
})

method("observations_by_DataSource_Range_AttributeValues", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, dateRange = NULL, values = NULL, ...) {
    JObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(dateRange$.jobj, "util.Range"), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("observations_by_DataSource_AttributeValues", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, source = NULL, values = NULL, ...) {
    JObservations(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("withName_by_String", "JTimeSeriesDataTable", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JTimeSeriesDataTable(jobj = jCall("tsdb/TimeSeriesDataTable", "Ltsdb/TimeSeriesDataTable;", "withName", the(name)))
})

method("C_OBSERVATION_VALUE", "JTimeSeriesDataTable", enforceRCC = FALSE, function(this, ...) {
    lazy(JTimeSeriesDataTable$...C_OBSERVATION_VALUE, JFloatColumn(jobj = jField(this$.jobj, "Ldb/columns/FloatColumn;", "C_OBSERVATION_VALUE")), log = FALSE)
})

method("C_OBSERVATION_TIME", "JTimeSeriesDataTable", enforceRCC = FALSE, function(this, ...) {
    lazy(JTimeSeriesDataTable$...C_OBSERVATION_TIME, JDatetimeColumn(jobj = jField(this$.jobj, "Ldb/columns/DatetimeColumn;", "C_OBSERVATION_TIME")), log = FALSE)
})

method("C_DATA_SOURCE_ID", "JTimeSeriesDataTable", enforceRCC = FALSE, function(this, ...) {
    lazy(JTimeSeriesDataTable$...C_DATA_SOURCE_ID, JIntColumn(jobj = jField(this$.jobj, "Ldb/columns/IntColumn;", "C_DATA_SOURCE_ID")), log = FALSE)
})

method("C_TIME_SERIES_ID", "JTimeSeriesDataTable", enforceRCC = FALSE, function(this, ...) {
    lazy(JTimeSeriesDataTable$...C_TIME_SERIES_ID, JIntColumn(jobj = jField(this$.jobj, "Ldb/columns/IntColumn;", "C_TIME_SERIES_ID")), log = FALSE)
})

