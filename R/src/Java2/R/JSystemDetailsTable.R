constructor("JSystemDetailsTable", function(jobj = NULL) {
    extend(JObject(), "JSystemDetailsTable", .jobj = jobj)
})

method("by", "JSystemDetailsTable", enforceRCC = TRUE, function(static, ...) {
    JSystemDetailsTable(jNew("systemdb/metadata/SystemDetailsTable"))
})

method("delete_by_Siv_Pv", "JSystemDetailsTable", enforceRCC = TRUE, function(this, testSiv = NULL, testPv = NULL, ...) {
    jCall(this$.jobj, "V", "delete", .jcast(testSiv$.jobj, "systemdb.metadata.Siv"), .jcast(testPv$.jobj, "systemdb.metadata.Pv"))
})

method("details_by_String_String", "JSystemDetailsTable", enforceRCC = TRUE, function(this, stoDir = NULL, stoId = NULL, ...) {
    JSystemDetails(jobj = jCall(this$.jobj, "Lsystemdb/metadata/SystemDetailsTable/SystemDetails;", "details", the(stoDir), the(stoId)))
})

method("id_by_Siv_String", "JSystemDetailsTable", enforceRCC = TRUE, function(this, siv = NULL, stoId = NULL, ...) {
    jCall(this$.jobj, "I", "id", .jcast(siv$.jobj, "systemdb.metadata.Siv"), the(stoId))
})

method("isValid_by_int", "JSystemDetailsTable", enforceRCC = TRUE, function(this, id = NULL, ...) {
    jCall(this$.jobj, "Z", "isValid", theInteger(id))
})

method("stoExists_by_Siv_String", "JSystemDetailsTable", enforceRCC = TRUE, function(this, siv = NULL, stoId = NULL, ...) {
    jCall(this$.jobj, "Z", "stoExists", .jcast(siv$.jobj, "systemdb.metadata.Siv"), the(stoId))
})

method("liveDetails_by_Siv_Pv", "JSystemDetailsTable", enforceRCC = TRUE, function(this, siv = NULL, pv = NULL, ...) {
    JSystemDetails(jobj = jCall(this$.jobj, "Lsystemdb/metadata/SystemDetailsTable/SystemDetails;", "liveDetails", .jcast(siv$.jobj, "systemdb.metadata.Siv"), .jcast(pv$.jobj, "systemdb.metadata.Pv")))
})

method("details_by_Clause", "JSystemDetailsTable", enforceRCC = TRUE, function(this, detailMatches = NULL, ...) {
    JSystemDetails(jobj = jCall(this$.jobj, "Lsystemdb/metadata/SystemDetailsTable/SystemDetails;", "details", .jcast(detailMatches$.jobj, "db.clause.Clause")))
})

method("insert_by_Siv_Pv_boolean", "JSystemDetailsTable", enforceRCC = TRUE, function(this, siv = NULL, pv = NULL, runInNativeCurrency = NULL, ...) {
    jCall(this$.jobj, "I", "insert", .jcast(siv$.jobj, "systemdb.metadata.Siv"), .jcast(pv$.jobj, "systemdb.metadata.Pv"), theLogical(runInNativeCurrency))
})

method("sivMatches_by_Siv", "JSystemDetailsTable", enforceRCC = TRUE, function(this, siv = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "sivMatches", .jcast(siv$.jobj, "systemdb.metadata.Siv")))
})

method("pvMatches_by_Pv", "JSystemDetailsTable", enforceRCC = TRUE, function(this, pv = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "pvMatches", .jcast(pv$.jobj, "systemdb.metadata.Pv")))
})

method("liveExists_by_Siv_Pv", "JSystemDetailsTable", enforceRCC = TRUE, function(this, siv = NULL, pv = NULL, ...) {
    jCall(this$.jobj, "Z", "liveExists", .jcast(siv$.jobj, "systemdb.metadata.Siv"), .jcast(pv$.jobj, "systemdb.metadata.Pv"))
})

method("details_by_int", "JSystemDetailsTable", enforceRCC = TRUE, function(this, systemId = NULL, ...) {
    JSystemDetails(jobj = jCall(this$.jobj, "Lsystemdb/metadata/SystemDetailsTable/SystemDetails;", "details", theInteger(systemId)))
})

method("siv_by_int", "JSystemDetailsTable", enforceRCC = TRUE, function(static, systemId = NULL, ...) {
    JSiv(jobj = jCall("systemdb/metadata/SystemDetailsTable", "Lsystemdb/metadata/Siv;", "siv", theInteger(systemId)))
})

method("allAvailableStoIds", "JSystemDetailsTable", enforceRCC = TRUE, function(static, ...) {
    JList(jobj = jCall("systemdb/metadata/SystemDetailsTable", "Ljava/util/List;", "allAvailableStoIds"))
})

method("liveSystem_by_int", "JSystemDetailsTable", enforceRCC = TRUE, function(static, systemId = NULL, ...) {
    JLiveSystem(jobj = jCall("systemdb/metadata/SystemDetailsTable", "Lsystemdb/metadata/LiveSystem;", "liveSystem", theInteger(systemId)))
})

method("insert_by_String_String_String_String_String_String", "JSystemDetailsTable", enforceRCC = TRUE, function(this, system = NULL, version = NULL, interval = NULL, pvName = NULL, stoDir = NULL, stoId = NULL, ...) {
    jCall(this$.jobj, "I", "insert", the(system), the(version), the(interval), the(pvName), the(stoDir), the(stoId))
})

method("insert_by_Siv_Pv_String_String_boolean", "JSystemDetailsTable", enforceRCC = TRUE, function(this, siv = NULL, pv = NULL, stoDir = NULL, stoId = NULL, runInNativeCurrency = NULL, ...) {
    jCall(this$.jobj, "I", "insert", .jcast(siv$.jobj, "systemdb.metadata.Siv"), .jcast(pv$.jobj, "systemdb.metadata.Pv"), the(stoDir), the(stoId), theLogical(runInNativeCurrency))
})

method("DETAILS", "JSystemDetailsTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystemDetailsTable$...DETAILS, JSystemDetailsTable(jobj = jField("systemdb/metadata/SystemDetailsTable", "Lsystemdb/metadata/SystemDetailsTable;", "DETAILS")), log = FALSE)
})

