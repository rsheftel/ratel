constructor("JMsivPv", function(jobj = NULL) {
    extend(JObject(), "JMsivPv", .jobj = jobj)
})

method("by_String_String", "JMsivPv", enforceRCC = TRUE, function(static, msiv = NULL, pv = NULL, ...) {
    JMsivPv(jNew("systemdb/metadata/MsivPv", the(msiv), the(pv)))
})

method("by_MsivRow_Pv", "JMsivPv", enforceRCC = TRUE, function(static, msiv = NULL, pv = NULL, ...) {
    JMsivPv(jNew("systemdb/metadata/MsivPv", .jcast(msiv$.jobj, "systemdb.metadata.MsivTable.MsivRow"), .jcast(pv$.jobj, "systemdb.metadata.Pv")))
})

method("compareTo_by_Object", "JMsivPv", enforceRCC = TRUE, function(this, x0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(x0$.jobj, "java.lang.Object"))
})

method("names_by_List", "JMsivPv", enforceRCC = TRUE, function(static, msivpvs = NULL, ...) {
    JList(jobj = jCall("systemdb/metadata/MsivPv", "Ljava/util/List;", "names", .jcast(msivpvs$.jobj, "java.util.List")))
})

method("siv", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    JSiv(jobj = jCall(this$.jobj, "Lsystemdb/metadata/Siv;", "siv"))
})

method("pvCell_by_StringColumn", "JMsivPv", enforceRCC = TRUE, function(this, col = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "pvCell", .jcast(col$.jobj, "db.columns.StringColumn")))
})

method("msivCell_by_StringColumn", "JMsivPv", enforceRCC = TRUE, function(this, col = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "msivCell", .jcast(col$.jobj, "db.columns.StringColumn")))
})

method("msivName", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "msivName")
})

method("liveSystem", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    JLiveSystem(jobj = jCall(this$.jobj, "Lsystemdb/metadata/LiveSystem;", "liveSystem"))
})

method("pv", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    JPv(jobj = jCall(this$.jobj, "Lsystemdb/metadata/Pv;", "pv"))
})

method("name", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("matches_by_Column_Column", "JMsivPv", enforceRCC = TRUE, function(this, msivColumn = NULL, pvColumn = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(msivColumn$.jobj, "db.Column"), .jcast(pvColumn$.jobj, "db.Column")))
})

method("matches_by_Collection_MSIVParameterValuesBase", "JMsivPv", enforceRCC = TRUE, function(static, msivpvs = NULL, params = NULL, ...) {
    JClause(jobj = jCall("systemdb/metadata/MsivPv", "Ldb/clause/Clause;", "matches", .jcast(msivpvs$.jobj, "java.util.Collection"), .jcast(params$.jobj, "db.tables.SystemDB.MSIVParameterValuesBase")))
})

method("market", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "market")
})

method("fileName", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "fileName")
})

method("compareTo_by_MsivPv", "JMsivPv", enforceRCC = TRUE, function(this, o = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(o$.jobj, "systemdb.metadata.MsivPv"))
})

method("toString", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("equals_by_Object", "JMsivPv", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JMsivPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

