constructor("JMsivLiveHistory", function(jobj = NULL) {
    extend(JObject(), "JMsivLiveHistory", .jobj = jobj)
})

method("by", "JMsivLiveHistory", enforceRCC = TRUE, function(static, ...) {
    JMsivLiveHistory(jNew("systemdb/metadata/MsivLiveHistory"))
})

method("insert_by_LiveSystem_String_Date_Date", "JMsivLiveHistory", enforceRCC = TRUE, function(this, liveSystem = NULL, marketName = NULL, start = NULL, end = NULL, ...) {
    jCall(this$.jobj, "V", "insert", .jcast(liveSystem$.jobj, "systemdb.metadata.LiveSystem"), the(marketName), .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date"))
})

method("liveSystems", "JMsivLiveHistory", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "liveSystems"))
})

method("systems", "JMsivLiveHistory", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "systems"))
})

method("pvsForMsiv_by_MsivRow", "JMsivLiveHistory", enforceRCC = TRUE, function(this, msiv = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "pvsForMsiv", .jcast(msiv$.jobj, "systemdb.metadata.MsivTable.MsivRow")))
})

method("markets_by_Siv_Pv", "JMsivLiveHistory", enforceRCC = TRUE, function(this, siv = NULL, pv = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "markets", .jcast(siv$.jobj, "systemdb.metadata.Siv"), .jcast(pv$.jobj, "systemdb.metadata.Pv")))
})

method("pvs_by_Siv", "JMsivLiveHistory", enforceRCC = TRUE, function(this, siv = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "pvs", .jcast(siv$.jobj, "systemdb.metadata.Siv")))
})

method("TEST_SIV", "JMsivLiveHistory", enforceRCC = FALSE, function(static, ...) {
    lazy(JMsivLiveHistory$...TEST_SIV, JSiv(jobj = jField("systemdb/metadata/MsivLiveHistory", "Lsystemdb/metadata/Siv;", "TEST_SIV")), log = FALSE)
})

method("LIVE", "JMsivLiveHistory", enforceRCC = FALSE, function(static, ...) {
    lazy(JMsivLiveHistory$...LIVE, JMsivLiveHistory(jobj = jField("systemdb/metadata/MsivLiveHistory", "Lsystemdb/metadata/MsivLiveHistory;", "LIVE")), log = FALSE)
})

