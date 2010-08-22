constructor("JSelectMultiple", function(jobj = NULL) {
    extend(JObject(), "JSelectMultiple", .jobj = jobj)
})

method("by_List_Clause_boolean", "JSelectMultiple", enforceRCC = TRUE, function(static, columns = NULL, match = NULL, distinct = NULL, ...) {
    JSelectMultiple(jNew("db/SelectMultiple", .jcast(columns$.jobj, "java.util.List"), .jcast(match$.jobj, "db.clause.Clause"), theLogical(distinct)))
})

method("by_List_Clause", "JSelectMultiple", enforceRCC = TRUE, function(static, columns = NULL, match = NULL, ...) {
    JSelectMultiple(jNew("db/SelectMultiple", .jcast(columns$.jobj, "java.util.List"), .jcast(match$.jobj, "db.clause.Clause")))
})

method("asMap_by_Column_Column", "JSelectMultiple", enforceRCC = TRUE, function(this, key = NULL, value = NULL, ...) {
    JMap(jobj = jCall(this$.jobj, "Ljava/util/Map;", "asMap", .jcast(key$.jobj, "db.Column"), .jcast(value$.jobj, "db.Column")))
})

method("asCsvText", "JSelectMultiple", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "asCsvText")
})

method("asCsv", "JSelectMultiple", enforceRCC = TRUE, function(this, ...) {
    JCsv(jobj = jCall(this$.jobj, "Lfile/Csv;", "asCsv"))
})

method("add_by_Column", "JSelectMultiple", enforceRCC = TRUE, function(this, c = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(c$.jobj, "db.Column"))
})

method("columns", "JSelectMultiple", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "columns"))
})

