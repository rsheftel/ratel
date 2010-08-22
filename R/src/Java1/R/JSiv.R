constructor("JSiv", function(jobj = NULL) {
    extend(JObject(), "JSiv", .jobj = jobj)
})

method("by_String_String_String", "JSiv", enforceRCC = TRUE, function(static, system = NULL, interval = NULL, version = NULL, ...) {
    JSiv(jNew("systemdb/metadata/Siv", the(system), the(interval), the(version)))
})

method("cells_by_NvarcharColumn_NvarcharColumn_NvarcharColumn", "JSiv", enforceRCC = TRUE, function(this, systemCol = NULL, intervalCol = NULL, versionCol = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "cells", .jcast(systemCol$.jobj, "db.columns.NvarcharColumn"), .jcast(intervalCol$.jobj, "db.columns.NvarcharColumn"), .jcast(versionCol$.jobj, "db.columns.NvarcharColumn")))
})

method("hasMarket_by_String", "JSiv", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Z", "hasMarket", the(name))
})

method("with_by_String", "JSiv", enforceRCC = TRUE, function(this, market = NULL, ...) {
    JMsivRow(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MsivTable/MsivRow;", "with", the(market)))
})

method("sivName_by_String", "JSiv", enforceRCC = TRUE, function(this, separator = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "sivName", the(separator))
})

method("fromSivName_by_String_String", "JSiv", enforceRCC = TRUE, function(static, name = NULL, separator = NULL, ...) {
    JSiv(jobj = jCall("systemdb/metadata/Siv", "Lsystemdb/metadata/Siv;", "fromSivName", the(name), the(separator)))
})

method("svimName_by_String_String_String", "JSiv", enforceRCC = TRUE, function(this, separator = NULL, m = NULL, extra = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "svimName", the(separator), the(m), the(extra))
})

method("sviName_by_String", "JSiv", enforceRCC = TRUE, function(this, separator = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "sviName", the(separator))
})

method("topicName", "JSiv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "topicName")
})

method("version", "JSiv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "version")
})

method("interval", "JSiv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "interval")
})

method("system", "JSiv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "system")
})

method("params_by_Pv", "JSiv", enforceRCC = TRUE, function(this, pv = NULL, ...) {
    JMap(jobj = jCall(this$.jobj, "Ljava/util/Map;", "params", .jcast(pv$.jobj, "systemdb.metadata.Pv")))
})

method("qClass", "JSiv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "qClass")
})

method("equals_by_Object", "JSiv", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JSiv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("versionCell_by_StringColumn", "JSiv", enforceRCC = TRUE, function(this, column = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "versionCell", .jcast(column$.jobj, "db.columns.StringColumn")))
})

method("intervalCell_by_StringColumn", "JSiv", enforceRCC = TRUE, function(this, column = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "intervalCell", .jcast(column$.jobj, "db.columns.StringColumn")))
})

method("systemCell_by_StringColumn", "JSiv", enforceRCC = TRUE, function(this, column = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "systemCell", .jcast(column$.jobj, "db.columns.StringColumn")))
})

method("liveSystems", "JSiv", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "liveSystems"))
})

method("matches_by_StringColumn_StringColumn_StringColumn", "JSiv", enforceRCC = TRUE, function(this, systemCol = NULL, intervalCol = NULL, versionCol = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(systemCol$.jobj, "db.columns.StringColumn"), .jcast(intervalCol$.jobj, "db.columns.StringColumn"), .jcast(versionCol$.jobj, "db.columns.StringColumn")))
})

method("name", "JSiv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("toString", "JSiv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

