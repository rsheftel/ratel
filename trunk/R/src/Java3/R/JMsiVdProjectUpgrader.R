constructor("JMsiVdProjectUpgrader", function(jobj = NULL) {
    extend(JObject(), "JMsiVdProjectUpgrader", .jobj = jobj)
})

method("by", "JMsiVdProjectUpgrader", enforceRCC = TRUE, function(static, ...) {
    JMsiVdProjectUpgrader(jNew("util/MsiVdProjectUpgrader"))
})

method("main_by_StringArray", "JMsiVdProjectUpgrader", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("util/MsiVdProjectUpgrader", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("UPGRADE_CODE", "JMsiVdProjectUpgrader", enforceRCC = FALSE, function(static, ...) {
    lazy(JMsiVdProjectUpgrader$...UPGRADE_CODE, jField("util/MsiVdProjectUpgrader", "Ljava/lang/String;", "UPGRADE_CODE"), log = FALSE)
})

