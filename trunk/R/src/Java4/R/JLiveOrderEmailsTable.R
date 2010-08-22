constructor("JLiveOrderEmailsTable", function(jobj = NULL) {
    extend(JObject(), "JLiveOrderEmailsTable", .jobj = jobj)
})

method("by", "JLiveOrderEmailsTable", enforceRCC = TRUE, function(static, ...) {
    JLiveOrderEmailsTable(jNew("systemdb/metadata/LiveOrderEmailsTable"))
})

method("emails_by_String_String_String", "JLiveOrderEmailsTable", enforceRCC = TRUE, function(this, system = NULL, pv = NULL, market = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "emails", the(system), the(pv), the(market)))
})

method("insert_by_String_String_String_String", "JLiveOrderEmailsTable", enforceRCC = TRUE, function(this, system = NULL, pv = NULL, market = NULL, email = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(system), the(pv), the(market), the(email))
})

method("ORDER_EMAILS", "JLiveOrderEmailsTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JLiveOrderEmailsTable$...ORDER_EMAILS, JLiveOrderEmailsTable(jobj = jField("systemdb/metadata/LiveOrderEmailsTable", "Lsystemdb/metadata/LiveOrderEmailsTable;", "ORDER_EMAILS")), log = FALSE)
})

