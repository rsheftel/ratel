constructor("JCopyCurvesFromSTO", function(jobj = NULL) {
    extend(JObject(), "JCopyCurvesFromSTO", .jobj = jobj)
})

method("by", "JCopyCurvesFromSTO", enforceRCC = TRUE, function(static, ...) {
    JCopyCurvesFromSTO(jNew("systemdb/qworkbench/CopyCurvesFromSTO"))
})

method("main_by_StringArray", "JCopyCurvesFromSTO", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("systemdb/qworkbench/CopyCurvesFromSTO", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

