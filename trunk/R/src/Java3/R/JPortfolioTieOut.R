constructor("JPortfolioTieOut", function(jobj = NULL) {
    extend(JObject(), "JPortfolioTieOut", .jobj = jobj)
})

method("by_String_String", "JPortfolioTieOut", enforceRCC = TRUE, function(static, oldFileName = NULL, newFileName = NULL, ...) {
    JPortfolioTieOut(jNew("systemdb/portfolio/PortfolioTieOut", the(oldFileName), the(newFileName)))
})

method("runReport_by_StringArray", "JPortfolioTieOut", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("systemdb/portfolio/PortfolioTieOut", "I", "runReport", jArray(args, "[Ljava/lang/String;"))
})

method("main_by_StringArray", "JPortfolioTieOut", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("systemdb/portfolio/PortfolioTieOut", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

