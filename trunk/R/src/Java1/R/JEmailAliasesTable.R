constructor("JEmailAliasesTable", function(jobj = NULL) {
    extend(JObject(), "JEmailAliasesTable", .jobj = jobj)
})

method("by", "JEmailAliasesTable", enforceRCC = TRUE, function(static, ...) {
    JEmailAliasesTable(jNew("mail/EmailAliasesTable"))
})

method("munge_by_String", "JEmailAliasesTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "munge", the(name))
})

method("clear", "JEmailAliasesTable", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clear")
})

method("insert_by_String_String", "JEmailAliasesTable", enforceRCC = TRUE, function(this, alias = NULL, recipients = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(alias), the(recipients))
})

method("EMAILS", "JEmailAliasesTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JEmailAliasesTable$...EMAILS, JEmailAliasesTable(jobj = jField("mail/EmailAliasesTable", "Lmail/EmailAliasesTable;", "EMAILS")), log = FALSE)
})

