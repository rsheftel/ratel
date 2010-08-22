constructor("JContractCurrent", function(jobj = NULL) {
    extend(JObject(), "JContractCurrent", .jobj = jobj)
})

method("by_String_String", "JContractCurrent", enforceRCC = TRUE, function(static, name = NULL, yellowKey = NULL, ...) {
    JContractCurrent(jNew("futures/ContractCurrent", the(name), the(yellowKey)))
})

method("equals_by_Object", "JContractCurrent", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JContractCurrent", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("options", "JContractCurrent", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "options"))
})

method("option_by_String", "JContractCurrent", enforceRCC = TRUE, function(this, optionName = NULL, ...) {
    JOptionCurrent(jobj = jCall(this$.jobj, "Lfutures/OptionCurrent;", "option", the(optionName)))
})

method("optionJob_by_BloombergField", "JContractCurrent", enforceRCC = TRUE, function(this, field = NULL, ...) {
    JBloombergJob(jobj = jCall(this$.jobj, "Lfutures/BloombergJob;", "optionJob", .jcast(field$.jobj, "futures.BloombergField")))
})

method("job_by_BloombergField", "JContractCurrent", enforceRCC = TRUE, function(this, field = NULL, ...) {
    JBloombergJob(jobj = jCall(this$.jobj, "Lfutures/BloombergJob;", "job", .jcast(field$.jobj, "futures.BloombergField")))
})

method("contracts", "JContractCurrent", enforceRCC = TRUE, function(static, ...) {
    JList(jobj = jCall("futures/ContractCurrent", "Ljava/util/List;", "contracts"))
})

method("series_by_FuturesTicker_String", "JContractCurrent", enforceRCC = TRUE, function(static, ticker = NULL, seriesSuffix = NULL, ...) {
    JTimeSeries(jobj = jCall("futures/ContractCurrent", "Ltsdb/TimeSeries;", "series", .jcast(ticker$.jobj, "futures.FuturesTicker"), the(seriesSuffix)))
})

method("jobEntries_by_Date_BloombergField", "JContractCurrent", enforceRCC = TRUE, function(this, asOf = NULL, field = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "jobEntries", .jcast(asOf$.jobj, "java.util.Date"), .jcast(field$.jobj, "futures.BloombergField")))
})

method("toString", "JContractCurrent", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("fields", "JContractCurrent", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "fields"))
})

