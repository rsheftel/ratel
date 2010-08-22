constructor("JGroups", function(jobj = NULL) {
    extend(JObject(), "JGroups", .jobj = jobj)
})

method("by", "JGroups", enforceRCC = TRUE, function(static, ...) {
    JGroups(jNew("systemdb/portfolio/Groups"))
})

method("load_by_Tag_String", "JGroups", enforceRCC = TRUE, function(this, group = NULL, groupPrefix = NULL, ...) {
    JGroup(jobj = jCall(this$.jobj, "Lsystemdb/portfolio/Group;", "load", .jcast(group$.jobj, "util.Tag"), the(groupPrefix)))
})

method("main_by_StringArray", "JGroups", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("systemdb/portfolio/Groups", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("members_by_String", "JGroups", enforceRCC = TRUE, function(this, group = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "members", the(group)))
})

method("group_by_Row", "JGroups", enforceRCC = TRUE, function(this, row = NULL, ...) {
    JGroup(jobj = jCall(this$.jobj, "Lsystemdb/portfolio/Group;", "group", .jcast(row$.jobj, "db.Row")))
})

method("insert_by_String", "JGroups", enforceRCC = TRUE, function(this, group = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(group))
})

method("rWeighting_by_Group", "JGroups", enforceRCC = TRUE, function(this, group = NULL, ...) {
    JWeightedMsivPvFiles(jobj = jCall(this$.jobj, "Lsystemdb/portfolio/WeightedMsivPvFiles;", "rWeighting", .jcast(group$.jobj, "systemdb.portfolio.Group")))
})

method("rWeighting_by_String", "JGroups", enforceRCC = TRUE, function(this, group = NULL, ...) {
    JWeightedMsivPvFiles(jobj = jCall(this$.jobj, "Lsystemdb/portfolio/WeightedMsivPvFiles;", "rWeighting", the(group)))
})

method("liveMarkets_by_String", "JGroups", enforceRCC = TRUE, function(this, group = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "liveMarkets", the(group)))
})

method("weighting_by_Group", "JGroups", enforceRCC = TRUE, function(this, group = NULL, ...) {
    JMap(jobj = jCall(this$.jobj, "Ljava/util/Map;", "weighting", .jcast(group$.jobj, "systemdb.portfolio.Group")))
})

method("weighting_by_String", "JGroups", enforceRCC = TRUE, function(this, group = NULL, ...) {
    JMap(jobj = jCall(this$.jobj, "Ljava/util/Map;", "weighting", the(group)))
})

method("forName_by_String", "JGroups", enforceRCC = TRUE, function(this, group = NULL, ...) {
    JGroup(jobj = jCall(this$.jobj, "Lsystemdb/portfolio/Group;", "forName", the(group)))
})

method("insert_by_String_String_double", "JGroups", enforceRCC = TRUE, function(this, group = NULL, memberGroup = NULL, weight = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(group), the(memberGroup), theNumeric(weight))
})

method("GROUPS", "JGroups", enforceRCC = FALSE, function(static, ...) {
    lazy(JGroups$...GROUPS, JGroups(jobj = jField("systemdb/portfolio/Groups", "Lsystemdb/portfolio/Groups;", "GROUPS")), log = FALSE)
})

