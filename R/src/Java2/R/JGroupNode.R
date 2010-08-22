constructor("JGroupNode", function(jobj = NULL) {
    extend(JObject(), "JGroupNode", .jobj = jobj)
})

method("by_String_double", "JGroupNode", enforceRCC = TRUE, function(static, group = NULL, weight = NULL, ...) {
    JGroupNode(jNew("systemdb/portfolio/GroupNode", the(group), theNumeric(weight)))
})

method("equals_by_Object", "JGroupNode", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JGroupNode", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("addTo_by_Tag", "JGroupNode", enforceRCC = TRUE, function(this, parent = NULL, ...) {
    jCall(this$.jobj, "V", "addTo", .jcast(parent$.jobj, "util.Tag"))
})

method("name", "JGroupNode", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("addWeights_by_Map_double", "JGroupNode", enforceRCC = TRUE, function(this, weights = NULL, scale = NULL, ...) {
    jCall(this$.jobj, "V", "addWeights", .jcast(weights$.jobj, "java.util.Map"), theNumeric(scale))
})

