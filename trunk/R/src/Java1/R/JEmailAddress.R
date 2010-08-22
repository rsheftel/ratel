constructor("JEmailAddress", function(jobj = NULL) {
    extend(JObject(), "JEmailAddress", .jobj = jobj)
})

method("by_String", "JEmailAddress", enforceRCC = TRUE, function(static, address = NULL, ...) {
    JEmailAddress(jNew("mail/EmailAddress", the(address)))
})

method("address", "JEmailAddress", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "address")
})

method("in_by_StringColumn", "JEmailAddress", enforceRCC = TRUE, function(this, c = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "in", .jcast(c$.jobj, "db.columns.StringColumn")))
})

method("addTo_by_MimeMessage", "JEmailAddress", enforceRCC = TRUE, function(this, msg = NULL, ...) {
    jCall(this$.jobj, "V", "addTo", .jcast(msg$.jobj, "javax.mail.internet.MimeMessage"))
})

method("toString", "JEmailAddress", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("expanded_by_String", "JEmailAddress", enforceRCC = TRUE, function(static, name = NULL, ...) {
    jCall("mail/EmailAddress", "Ljava/lang/String;", "expanded", the(name))
})

method("NOBODY", "JEmailAddress", enforceRCC = FALSE, function(static, ...) {
    lazy(JEmailAddress$...NOBODY, JEmailAddress(jobj = jField("mail/EmailAddress", "Lmail/EmailAddress;", "NOBODY")), log = FALSE)
})

method("US", "JEmailAddress", enforceRCC = FALSE, function(static, ...) {
    lazy(JEmailAddress$...US, JEmailAddress(jobj = jField("mail/EmailAddress", "Lmail/EmailAddress;", "US")), log = FALSE)
})

method("TEAM", "JEmailAddress", enforceRCC = FALSE, function(static, ...) {
    lazy(JEmailAddress$...TEAM, JEmailAddress(jobj = jField("mail/EmailAddress", "Lmail/EmailAddress;", "TEAM")), log = FALSE)
})

