constructor("JTestGroups", function(jobj = NULL) {
    extend(JObject(), "JTestGroups", .jobj = jobj)
})

method("by", "JTestGroups", enforceRCC = TRUE, function(static, ...) {
    JTestGroups(jNew("systemdb/portfolio/TestGroups"))
})

method("testWeightedMsivPvFiles", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "testWeightedMsivPvFiles")
})

method("testWeightingLookupWorksWhenNoLeafNodes", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "testWeightingLookupWorksWhenNoLeafNodes")
})

method("testWeightingLookupWorksAtAnyLevel", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "testWeightingLookupWorksAtAnyLevel")
})

method("testCanGenerateMegaGroupWeightings", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "testCanGenerateMegaGroupWeightings")
})

method("testCanGenerateMsivPvWeightings", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "testCanGenerateMsivPvWeightings")
})

method("testCanReadAndWriteGroupToXml", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "testCanReadAndWriteGroupToXml")
})

method("releaseLock", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "releaseLock")
})

method("tearDown", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "tearDown")
})

method("setUp", "JTestGroups", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "setUp")
})

