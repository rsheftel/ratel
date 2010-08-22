constructor("JGroup", function(jobj = NULL) {
    extend(JObject(), "JGroup", .jobj = jobj)
})

method("by_double", "JGroup", enforceRCC = TRUE, function(static, weight = NULL, ...) {
    JGroup(jNew("systemdb/portfolio/Group", theNumeric(weight)))
})

method("asXml_by_Group", "JGroup", enforceRCC = TRUE, function(static, group = NULL, ...) {
    JTag(jobj = jCall("systemdb/portfolio/Group", "Lutil/Tag;", "asXml", .jcast(group$.jobj, "systemdb.portfolio.Group")))
})

method("addTo_by_Tag", "JGroup", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "addTo", .jcast(j_arg0$.jobj, "util.Tag"))
})

method("toString", "JGroup", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("members", "JGroup", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "members"))
})

method("weight", "JGroup", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "weight")
})

method("weighting", "JGroup", enforceRCC = TRUE, function(this, ...) {
    JMap(jobj = jCall(this$.jobj, "Ljava/util/Map;", "weighting"))
})

method("name", "JGroup", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("addWeights_by_Map_double", "JGroup", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "V", "addWeights", .jcast(j_arg0$.jobj, "java.util.Map"), theNumeric(j_arg1))
})

method("group_by_Row", "JGroup", enforceRCC = TRUE, function(static, row = NULL, ...) {
    JGroup(jobj = jCall("systemdb/portfolio/Group", "Lsystemdb/portfolio/Group;", "group", .jcast(row$.jobj, "db.Row")))
})

