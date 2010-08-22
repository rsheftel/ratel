constructor("JPortfolio", function(jobj = NULL) {
    extend(JObject(), "JPortfolio", .jobj = jobj)
})

method("by_String", "JPortfolio", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JPortfolio(jNew("sto/Portfolio", the(name)))
})

method("by_String_List", "JPortfolio", enforceRCC = TRUE, function(static, name = NULL, msivs = NULL, ...) {
    JPortfolio(jNew("sto/Portfolio", the(name), .jcast(msivs$.jobj, "java.util.List")))
})

method("by_QFile", "JPortfolio", enforceRCC = TRUE, function(static, file = NULL, ...) {
    JPortfolio(jNew("sto/Portfolio", .jcast(file$.jobj, "file.QFile")))
})

method("add_by_WeightedMsiv", "JPortfolio", enforceRCC = TRUE, function(this, msiv = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(msiv$.jobj, "sto.WeightedMsiv"))
})

method("portfolio_by_int_String", "JPortfolio", enforceRCC = TRUE, function(static, systemId = NULL, name = NULL, ...) {
    JPortfolio(jobj = jCall("sto/Portfolio", "Lsto/Portfolio;", "portfolio", theInteger(systemId), the(name)))
})

method("portfolios_by_int", "JPortfolio", enforceRCC = TRUE, function(static, systemId = NULL, ...) {
    JList(jobj = jCall("sto/Portfolio", "Ljava/util/List;", "portfolios", theInteger(systemId)))
})

method("cell_by_NvarcharColumn", "JPortfolio", enforceRCC = TRUE, function(this, nameCol = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "cell", .jcast(nameCol$.jobj, "db.columns.NvarcharColumn")))
})

method("store_by_int", "JPortfolio", enforceRCC = TRUE, function(this, systemId = NULL, ...) {
    jCall(this$.jobj, "V", "store", theInteger(systemId))
})

method("main_by_StringArray", "JPortfolio", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("sto/Portfolio", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("combineCurves_by_Curves_int", "JPortfolio", enforceRCC = TRUE, function(this, curves = NULL, nParallel = NULL, ...) {
    jCall(this$.jobj, "V", "combineCurves", .jcast(curves$.jobj, "sto.Curves"), theInteger(nParallel))
})

method("msivs", "JPortfolio", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "msivs"))
})

method("siv", "JPortfolio", enforceRCC = TRUE, function(this, ...) {
    JSiv(jobj = jCall(this$.jobj, "Lsystemdb/metadata/Siv;", "siv"))
})

method("name", "JPortfolio", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("equals_by_Object", "JPortfolio", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JPortfolio", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("toString", "JPortfolio", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

