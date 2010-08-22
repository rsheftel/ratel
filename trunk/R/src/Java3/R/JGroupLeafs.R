constructor("JGroupLeafs", function(jobj = NULL) {
    extend(JObject(), "JGroupLeafs", .jobj = jobj)
})

method("by", "JGroupLeafs", enforceRCC = TRUE, function(static, ...) {
    JGroupLeafs(jNew("systemdb/portfolio/GroupLeafs"))
})

method("delete_by_String", "JGroupLeafs", enforceRCC = TRUE, function(this, group = NULL, ...) {
    jCall(this$.jobj, "V", "delete", the(group))
})

method("group_by_Row", "JGroupLeafs", enforceRCC = TRUE, function(this, row = NULL, ...) {
    JGroup(jobj = jCall(this$.jobj, "Lsystemdb/portfolio/Group;", "group", .jcast(row$.jobj, "db.Row")))
})

method("weighting_by_String_double", "JGroupLeafs", enforceRCC = TRUE, function(this, group = NULL, weighting = NULL, ...) {
    JMap(jobj = jCall(this$.jobj, "Ljava/util/Map;", "weighting", the(group), theNumeric(weighting)))
})

method("insert_by_String_MsivPv_double", "JGroupLeafs", enforceRCC = TRUE, function(this, group = NULL, msivpv = NULL, weight = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(group), .jcast(msivpv$.jobj, "systemdb.metadata.MsivPv"), theNumeric(weight))
})

method("LEAFS", "JGroupLeafs", enforceRCC = FALSE, function(static, ...) {
    lazy(JGroupLeafs$...LEAFS, JGroupLeafs(jobj = jField("systemdb/portfolio/GroupLeafs", "Lsystemdb/portfolio/GroupLeafs;", "LEAFS")), log = FALSE)
})

