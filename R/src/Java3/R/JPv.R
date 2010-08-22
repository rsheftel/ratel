constructor("JPv", function(jobj = NULL) {
    extend(JObject(), "JPv", .jobj = jobj)
})

method("by_String", "JPv", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JPv(jNew("systemdb/metadata/Pv", the(name)))
})

method("compareTo_by_Object", "JPv", enforceRCC = TRUE, function(this, x0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(x0$.jobj, "java.lang.Object"))
})

method("equals_by_Object", "JPv", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("cell_by_StringColumn", "JPv", enforceRCC = TRUE, function(this, col = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "cell", .jcast(col$.jobj, "db.columns.StringColumn")))
})

method("matches_by_StringColumn", "JPv", enforceRCC = TRUE, function(this, col = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(col$.jobj, "db.columns.StringColumn")))
})

method("toString", "JPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("name", "JPv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("compareTo_by_Pv", "JPv", enforceRCC = TRUE, function(this, o = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(o$.jobj, "systemdb.metadata.Pv"))
})

