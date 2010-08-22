constructor("JMockEmailer", function(jobj = NULL) {
    extend(JObject(), "JMockEmailer", .jobj = jobj)
})

method("by", "JMockEmailer", enforceRCC = TRUE, function(static, ...) {
    JMockEmailer(jNew("mail/MockEmailer"))
})

method("reset", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "reset")
})

method("sentMime", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    JMimeMessage(jobj = jCall(this$.jobj, "Ljavax/mail/internet/MimeMessage;", "sentMime"))
})

method("first", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    JSent(jobj = jCall(this$.jobj, "Lmail/MockEmailer/Sent;", "first"))
})

method("message", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "message")
})

method("requireSent_by_int", "JMockEmailer", enforceRCC = TRUE, function(this, expected = NULL, ...) {
    jCall(this$.jobj, "V", "requireSent", theInteger(expected))
})

method("requireEmpty", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "requireEmpty")
})

method("clear", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clear")
})

method("sent", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    JSent(jobj = jCall(this$.jobj, "Lmail/MockEmailer/Sent;", "sent"))
})

method("allowMessages", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "allowMessages")
})

method("disallowMessages", "JMockEmailer", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "disallowMessages")
})

method("send_by_MimeMessage", "JMockEmailer", enforceRCC = TRUE, function(this, msg = NULL, ...) {
    jCall(this$.jobj, "V", "send", .jcast(msg$.jobj, "javax.mail.internet.MimeMessage"))
})

