constructor("JEmail", function(jobj = NULL) {
    extend(JObject(), "JEmail", .jobj = jobj)
})

method("by_Emailer_String_String", "JEmail", enforceRCC = TRUE, function(static, emailer = NULL, subject = NULL, message = NULL, ...) {
    JEmail(jNew("mail/Email", .jcast(emailer$.jobj, "mail.Emailer"), the(subject), the(message)))
})

method("main_by_StringArray", "JEmail", enforceRCC = TRUE, function(static, j_in = NULL, ...) {
    jCall("mail/Email", "V", "main", jArray(j_in, "[Ljava/lang/String;"))
})

method("requireValidAddress_by_String_String", "JEmail", enforceRCC = TRUE, function(static, string = NULL, usage = NULL, ...) {
    jCall("mail/Email", "V", "requireValidAddress", the(string), the(usage))
})

method("attach_by_QFile", "JEmail", enforceRCC = TRUE, function(this, file = NULL, ...) {
    jCall(this$.jobj, "V", "attach", .jcast(file$.jobj, "file.QFile"))
})

method("content", "JEmail", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "content")
})

method("sendTo_by_String", "JEmail", enforceRCC = TRUE, function(this, failureAddresses = NULL, ...) {
    jCall(this$.jobj, "V", "sendTo", the(failureAddresses))
})

method("hasContent", "JEmail", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasContent")
})

method("append_by_char", "JEmail", enforceRCC = TRUE, function(this, c = NULL, ...) {
    jCall(this$.jobj, "V", "append", fail("char parameters not handled by R/Java convertPrimitive!"))
})

method("append_by_String", "JEmail", enforceRCC = TRUE, function(this, text = NULL, ...) {
    jCall(this$.jobj, "V", "append", the(text))
})

method("sendTo_by_EmailAddress", "JEmail", enforceRCC = TRUE, function(this, address = NULL, ...) {
    jCall(this$.jobj, "V", "sendTo", .jcast(address$.jobj, "mail.EmailAddress"))
})

method("trade_by_String_String", "JEmail", enforceRCC = TRUE, function(static, subject = NULL, message = NULL, ...) {
    JEmail(jobj = jCall("mail/Email", "Lmail/Email;", "trade", the(subject), the(message)))
})

method("notification_by_String_String", "JEmail", enforceRCC = TRUE, function(static, subject = NULL, message = NULL, ...) {
    JEmail(jobj = jCall("mail/Email", "Lmail/Email;", "notification", the(subject), the(message)))
})

method("problem_by_String_String", "JEmail", enforceRCC = TRUE, function(static, subject = NULL, message = NULL, ...) {
    JEmail(jobj = jCall("mail/Email", "Lmail/Email;", "problem", the(subject), the(message)))
})

