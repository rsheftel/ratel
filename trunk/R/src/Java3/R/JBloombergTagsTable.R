constructor("JBloombergTagsTable", function(jobj = NULL) {
    extend(JObject(), "JBloombergTagsTable", .jobj = jobj)
})

method("by", "JBloombergTagsTable", enforceRCC = TRUE, function(static, ...) {
    JBloombergTagsTable(jNew("systemdb/metadata/BloombergTagsTable"))
})

method("setAutoExecuteTrades_by_int_boolean", "JBloombergTagsTable", enforceRCC = TRUE, function(this, id = NULL, autoExecute = NULL, ...) {
    jCall(this$.jobj, "V", "setAutoExecuteTrades", theInteger(id), theLogical(autoExecute))
})

method("insertIfNeeded_by_int_String_boolean", "JBloombergTagsTable", enforceRCC = TRUE, function(this, id = NULL, tag = NULL, autoExecute = NULL, ...) {
    jCall(this$.jobj, "V", "insertIfNeeded", theInteger(id), the(tag), theLogical(autoExecute))
})

method("anyAutoExecute_by_String", "JBloombergTagsTable", enforceRCC = TRUE, function(this, system = NULL, ...) {
    jCall(this$.jobj, "Z", "anyAutoExecute", the(system))
})

method("autoExecuteTrades_by_int", "JBloombergTagsTable", enforceRCC = TRUE, function(this, id = NULL, ...) {
    jCall(this$.jobj, "Z", "autoExecuteTrades", theInteger(id))
})

method("tag_by_int", "JBloombergTagsTable", enforceRCC = TRUE, function(this, systemId = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "tag", theInteger(systemId))
})

method("TAGS", "JBloombergTagsTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JBloombergTagsTable$...TAGS, JBloombergTagsTable(jobj = jField("systemdb/metadata/BloombergTagsTable", "Lsystemdb/metadata/BloombergTagsTable;", "TAGS")), log = FALSE)
})

