constructor("JRealEmailer", function(jobj = NULL) {
    extend(JObject(), "JRealEmailer", .jobj = jobj)
})

method("by", "JRealEmailer", enforceRCC = TRUE, function(static, ...) {
    JRealEmailer(jNew("mail/RealEmailer"))
})

method("use", "JRealEmailer", enforceRCC = TRUE, function(static, ...) {
    jCall("mail/RealEmailer", "V", "use")
})

method("send_by_MimeMessage", "JRealEmailer", enforceRCC = TRUE, function(this, msg = NULL, ...) {
    jCall(this$.jobj, "V", "send", .jcast(msg$.jobj, "javax.mail.internet.MimeMessage"))
})

