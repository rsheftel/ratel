constructor("JSystemTable", function(jobj = NULL) {
    extend(JObject(), "JSystemTable", .jobj = jobj)
})

method("by", "JSystemTable", enforceRCC = TRUE, function(static, ...) {
    JSystemTable(jNew("systemdb/metadata/SystemTable"))
})

method("qClass_by_String", "JSystemTable", enforceRCC = TRUE, function(this, system = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "qClass", the(system))
})

method("insert_by_String_String", "JSystemTable", enforceRCC = TRUE, function(this, system = NULL, qClass = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(system), the(qClass))
})

method("SYSTEM", "JSystemTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystemTable$...SYSTEM, JSystemTable(jobj = jField("systemdb/metadata/SystemTable", "Lsystemdb/metadata/SystemTable;", "SYSTEM")), log = FALSE)
})

