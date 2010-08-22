constructor("JLiveSystem", function(jobj = NULL) {
    extend(JObject(), "JLiveSystem", .jobj = jobj)
})

method("by_Siv_Pv", "JLiveSystem", enforceRCC = TRUE, function(static, siv = NULL, pv = NULL, ...) {
    JLiveSystem(jNew("systemdb/metadata/LiveSystem", .jcast(siv$.jobj, "systemdb.metadata.Siv"), .jcast(pv$.jobj, "systemdb.metadata.Pv")))
})

method("fileName_by_String", "JLiveSystem", enforceRCC = TRUE, function(this, marketName = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "fileName", the(marketName))
})

method("series_by_String", "JLiveSystem", enforceRCC = TRUE, function(this, metric = NULL, ...) {
    JTimeSeries(jobj = jCall(this$.jobj, "Ltsdb/TimeSeries;", "series", the(metric)))
})

method("isAutoExecute_by_int", "JLiveSystem", enforceRCC = TRUE, function(static, systemId = NULL, ...) {
    jCall("systemdb/metadata/LiveSystem", "Z", "isAutoExecute", theInteger(systemId))
})

method("liveSystem_by_int", "JLiveSystem", enforceRCC = TRUE, function(static, systemId = NULL, ...) {
    JLiveSystem(jobj = jCall("systemdb/metadata/LiveSystem", "Lsystemdb/metadata/LiveSystem;", "liveSystem", theInteger(systemId)))
})

method("liveMarket_by_String", "JLiveSystem", enforceRCC = TRUE, function(this, market = NULL, ...) {
    JMsivPv(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MsivPv;", "liveMarket", the(market)))
})

method("pv", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    JPv(jobj = jCall(this$.jobj, "Lsystemdb/metadata/Pv;", "pv"))
})

method("systemName", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "systemName")
})

method("siv", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    JSiv(jobj = jCall(this$.jobj, "Lsystemdb/metadata/Siv;", "siv"))
})

method("autoExecuteTrades", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "autoExecuteTrades")
})

method("bloombergTag", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "bloombergTag")
})

method("insertParameter_by_String_String", "JLiveSystem", enforceRCC = TRUE, function(this, name = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "insertParameter", the(name), the(value))
})

method("removeAllLiveMarkets", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "removeAllLiveMarkets")
})

method("addLiveMarket_by_String_Date_Date", "JLiveSystem", enforceRCC = TRUE, function(this, name = NULL, start = NULL, end = NULL, ...) {
    jCall(this$.jobj, "V", "addLiveMarket", the(name), .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date"))
})

method("addLiveMarket_by_String_String_String", "JLiveSystem", enforceRCC = TRUE, function(this, name = NULL, startDate = NULL, endDate = NULL, ...) {
    jCall(this$.jobj, "V", "addLiveMarket", the(name), the(startDate), the(endDate))
})

method("liveMarkets", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "liveMarkets"))
})

method("markets", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "markets"))
})

method("setQClassName_by_String", "JLiveSystem", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "V", "setQClassName", the(name))
})

method("setAutoExecuteTrades_by_boolean", "JLiveSystem", enforceRCC = TRUE, function(this, autoExecute = NULL, ...) {
    jCall(this$.jobj, "V", "setAutoExecuteTrades", theLogical(autoExecute))
})

method("populateTagIfNeeded_by_String_boolean", "JLiveSystem", enforceRCC = TRUE, function(this, tag = NULL, autoExecute = NULL, ...) {
    jCall(this$.jobj, "V", "populateTagIfNeeded", the(tag), theLogical(autoExecute))
})

method("populateDetailsIfNeeded_by_boolean", "JLiveSystem", enforceRCC = TRUE, function(this, runInNativeCurrency = NULL, ...) {
    jCall(this$.jobj, "I", "populateDetailsIfNeeded", theLogical(runInNativeCurrency))
})

method("hasDetails", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasDetails")
})

method("parameters", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    JMap(jobj = jCall(this$.jobj, "Ljava/util/Map;", "parameters"))
})

method("id", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "id")
})

method("repFile_by_QDirectory", "JLiveSystem", enforceRCC = TRUE, function(this, path = NULL, ...) {
    JQFile(jobj = jCall(this$.jobj, "Lfile/QFile;", "repFile", .jcast(path$.jobj, "file.QDirectory")))
})

method("repXml_by_QDirectory", "JLiveSystem", enforceRCC = TRUE, function(this, path = NULL, ...) {
    JTag(jobj = jCall(this$.jobj, "Lutil/Tag;", "repXml", .jcast(path$.jobj, "file.QDirectory")))
})

method("name", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("file_by_QDirectory_String", "JLiveSystem", enforceRCC = TRUE, function(this, path = NULL, suffix = NULL, ...) {
    JQFile(jobj = jCall(this$.jobj, "Lfile/QFile;", "file", .jcast(path$.jobj, "file.QDirectory"), the(suffix)))
})

method("clearDetailsCache", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clearDetailsCache")
})

method("details", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    JSystemDetails(jobj = jCall(this$.jobj, "Lsystemdb/metadata/SystemDetailsTable/SystemDetails;", "details"))
})

method("equals_by_Object", "JLiveSystem", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("tradeFileName", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "tradeFileName")
})

method("topicName_by_String_String", "JLiveSystem", enforceRCC = TRUE, function(this, prefix = NULL, suffix = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "topicName", the(prefix), the(suffix))
})

method("toString", "JLiveSystem", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

