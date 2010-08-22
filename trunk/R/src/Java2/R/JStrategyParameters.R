constructor("JStrategyParameters", function(jobj = NULL) {
    extend(JObject(), "JStrategyParameters", .jobj = jobj)
})

method("by", "JStrategyParameters", enforceRCC = TRUE, function(static, ...) {
    JStrategyParameters(jNew("systemdb/metadata/StrategyParameters"))
})

method("sizingParameter_by_String", "JStrategyParameters", enforceRCC = TRUE, function(this, system = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "sizingParameter", the(system))
})

method("names_by_String", "JStrategyParameters", enforceRCC = TRUE, function(this, system = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "names", the(system)))
})

method("NAMES", "JStrategyParameters", enforceRCC = FALSE, function(static, ...) {
    lazy(JStrategyParameters$...NAMES, JStrategyParameters(jobj = jField("systemdb/metadata/StrategyParameters", "Lsystemdb/metadata/StrategyParameters;", "NAMES")), log = FALSE)
})

