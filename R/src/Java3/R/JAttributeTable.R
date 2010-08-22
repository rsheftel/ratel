constructor("JAttributeTable", function(jobj = NULL) {
    extend(JObject(), "JAttributeTable", .jobj = jobj)
})

method("by_String", "JAttributeTable", enforceRCC = TRUE, function(static, alias = NULL, ...) {
    JAttributeTable(jNew("tsdb/AttributeTable", the(alias)))
})

method("by", "JAttributeTable", enforceRCC = TRUE, function(static, ...) {
    JAttributeTable(jNew("tsdb/AttributeTable"))
})

method("allDefinitions", "JAttributeTable", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "allDefinitions"))
})

method("definition_by_String", "JAttributeTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JDefinition(jobj = jCall(this$.jobj, "Ltsdb/AttributeTable/Definition;", "definition", the(name)))
})

method("ATTRIBUTE", "JAttributeTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttributeTable$...ATTRIBUTE, JAttributeTable(jobj = jField("tsdb/AttributeTable", "Ltsdb/AttributeTable;", "ATTRIBUTE")), log = FALSE)
})

