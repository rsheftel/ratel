constructor("JWeightedMsivPvFiles", function(jobj = NULL) {
    extend(JObject(), "JWeightedMsivPvFiles", .jobj = jobj)
})

method("by_Map", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(static, weighting = NULL, ...) {
    JWeightedMsivPvFiles(jNew("systemdb/portfolio/WeightedMsivPvFiles", .jcast(weighting$.jobj, "java.util.Map")))
})

method("msivs", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "msivs")
})

method("markets", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "markets")
})

method("weights", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[D", "weights")
})

method("filenames", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "filenames")
})

method("pvs", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "pvs")
})

method("intervals", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "intervals")
})

method("versions", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "versions")
})

method("systems", "JWeightedMsivPvFiles", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "systems")
})

