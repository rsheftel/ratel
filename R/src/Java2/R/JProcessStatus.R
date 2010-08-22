constructor("JProcessStatus", function(jobj = NULL) {
    extend(JObject(), "JProcessStatus", .jobj = jobj)
})

method("valueOf_by_String", "JProcessStatus", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JProcessStatus(jobj = jCall("util/ProcessStatus", "Lutil/ProcessStatus;", "valueOf", the(name)))
})

method("values", "JProcessStatus", enforceRCC = TRUE, function(static, ...) {
    lapply(jCall("util/ProcessStatus", "[Lutil/ProcessStatus;", "values"), JProcessStatus)
})

method("FAILED", "JProcessStatus", enforceRCC = FALSE, function(static, ...) {
    lazy(JProcessStatus$...FAILED, JProcessStatus(jobj = jField("util/ProcessStatus", "Lutil/ProcessStatus;", "FAILED")), log = FALSE)
})

method("SUCCEEDED", "JProcessStatus", enforceRCC = FALSE, function(static, ...) {
    lazy(JProcessStatus$...SUCCEEDED, JProcessStatus(jobj = jField("util/ProcessStatus", "Lutil/ProcessStatus;", "SUCCEEDED")), log = FALSE)
})

