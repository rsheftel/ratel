constructor("JGroupLeaf", function(jobj = NULL) {
    extend(JObject(), "JGroupLeaf", .jobj = jobj)
})

method("by_MsivPv_double", "JGroupLeaf", enforceRCC = TRUE, function(static, msivpv = NULL, weight = NULL, ...) {
    JGroupLeaf(jNew("systemdb/portfolio/GroupLeaf", .jcast(msivpv$.jobj, "systemdb.metadata.MsivPv"), theNumeric(weight)))
})

method("equals_by_Object", "JGroupLeaf", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JGroupLeaf", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("addTo_by_Tag", "JGroupLeaf", enforceRCC = TRUE, function(this, parent = NULL, ...) {
    jCall(this$.jobj, "V", "addTo", .jcast(parent$.jobj, "util.Tag"))
})

method("name", "JGroupLeaf", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("addWeights_by_Map_double", "JGroupLeaf", enforceRCC = TRUE, function(this, weights = NULL, scale = NULL, ...) {
    jCall(this$.jobj, "V", "addWeights", .jcast(weights$.jobj, "java.util.Map"), theNumeric(scale))
})

