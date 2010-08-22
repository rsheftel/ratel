constructor("JOptionType", function(jobj = NULL) {
    extend(JObject(), "JOptionType", .jobj = jobj)
})

method("fromFlag_by_String", "JOptionType", enforceRCC = TRUE, function(static, value = NULL, ...) {
    JOptionType(jobj = jCall("tsdb/OptionType", "Ltsdb/OptionType;", "fromFlag", the(value)))
})

method("value", "JOptionType", enforceRCC = TRUE, function(this, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "value"))
})

method("is_by_CharColumn", "JOptionType", enforceRCC = TRUE, function(this, c = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "is", .jcast(c$.jobj, "db.columns.CharColumn")))
})

method("valueOf_by_String", "JOptionType", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JOptionType(jobj = jCall("tsdb/OptionType", "Ltsdb/OptionType;", "valueOf", the(name)))
})

method("values", "JOptionType", enforceRCC = TRUE, function(static, ...) {
    lapply(jCall("tsdb/OptionType", "[Ltsdb/OptionType;", "values"), JOptionType)
})

method("PUT", "JOptionType", enforceRCC = FALSE, function(static, ...) {
    lazy(JOptionType$...PUT, JOptionType(jobj = jField("tsdb/OptionType", "Ltsdb/OptionType;", "PUT")), log = FALSE)
})

method("CALL", "JOptionType", enforceRCC = FALSE, function(static, ...) {
    lazy(JOptionType$...CALL, JOptionType(jobj = jField("tsdb/OptionType", "Ltsdb/OptionType;", "CALL")), log = FALSE)
})

