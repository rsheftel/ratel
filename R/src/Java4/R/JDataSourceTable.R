constructor("JDataSourceTable", function(jobj = NULL) {
    extend(JObject(), "JDataSourceTable", .jobj = jobj)
})

method("by", "JDataSourceTable", enforceRCC = TRUE, function(static, ...) {
    JDataSourceTable(jNew("tsdb/DataSourceTable"))
})

method("idMatches_by_IntColumn_String", "JDataSourceTable", enforceRCC = TRUE, function(this, other = NULL, name = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "idMatches", .jcast(other$.jobj, "db.columns.IntColumn"), the(name)))
})

method("id_by_String", "JDataSourceTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "I", "id", the(name))
})

method("DATA_SOURCE", "JDataSourceTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JDataSourceTable$...DATA_SOURCE, JDataSourceTable(jobj = jField("tsdb/DataSourceTable", "Ltsdb/DataSourceTable;", "DATA_SOURCE")), log = FALSE)
})

