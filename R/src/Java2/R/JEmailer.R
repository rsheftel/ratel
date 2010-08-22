constructor("JEmailer", function(jobj = NULL) {
    extend(JObject(), "JEmailer", .jobj = jobj)
})

method("by", "JEmailer", enforceRCC = TRUE, function(static, ...) {
    JEmailer(jNew("mail/Emailer"))
})

method("current", "JEmailer", enforceRCC = TRUE, function(static, ...) {
    JEmailer(jobj = jCall("mail/Emailer", "Lmail/Emailer;", "current"))
})

