constructor("JFinancialCenterTable", function(jobj = NULL) {
    extend(JObject(), "JFinancialCenterTable", .jobj = jobj)
})

method("by", "JFinancialCenterTable", enforceRCC = TRUE, function(static, ...) {
    JFinancialCenterTable(jNew("tsdb/FinancialCenterTable"))
})

method("name_by_int", "JFinancialCenterTable", enforceRCC = TRUE, function(this, id = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name", theInteger(id))
})

method("insert_by_int_int_String", "JFinancialCenterTable", enforceRCC = TRUE, function(this, i = NULL, id = NULL, name = NULL, ...) {
    jCall(this$.jobj, "V", "insert", theInteger(i), theInteger(id), the(name))
})

method("CENTER", "JFinancialCenterTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JFinancialCenterTable$...CENTER, JFinancialCenterTable(jobj = jField("tsdb/FinancialCenterTable", "Ltsdb/FinancialCenterTable;", "CENTER")), log = FALSE)
})

