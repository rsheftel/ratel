constructor("JProtoBar", function(jobj = NULL) {
    extend(JObject(), "JProtoBar", .jobj = jobj)
})

method("by_Bar", "JProtoBar", enforceRCC = TRUE, function(static, bar = NULL, ...) {
    JProtoBar(jNew("systemdb/data/bars/ProtoBar", .jcast(bar$.jobj, "systemdb.data.Bar")))
})

method("by", "JProtoBar", enforceRCC = TRUE, function(static, ...) {
    JProtoBar(jNew("systemdb/data/bars/ProtoBar"))
})

method("date", "JProtoBar", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "date"))
})

method("updateHLCV_by_Bar", "JProtoBar", enforceRCC = TRUE, function(this, bar = NULL, ...) {
    jCall(this$.jobj, "V", "updateHLCV", .jcast(bar$.jobj, "systemdb.data.Bar"))
})

method("update_by_Bar", "JProtoBar", enforceRCC = TRUE, function(this, bar = NULL, ...) {
    jCall(this$.jobj, "V", "update", .jcast(bar$.jobj, "systemdb.data.Bar"))
})

method("setDate_by_Date", "JProtoBar", enforceRCC = TRUE, function(this, newDate = NULL, ...) {
    jCall(this$.jobj, "V", "setDate", .jcast(newDate$.jobj, "java.util.Date"))
})

method("fromClose_by_double", "JProtoBar", enforceRCC = TRUE, function(this, priorClose = NULL, ...) {
    JBar(jobj = jCall(this$.jobj, "Lsystemdb/data/Bar;", "fromClose", theNumeric(priorClose)))
})

method("toString", "JProtoBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("asBar", "JProtoBar", enforceRCC = TRUE, function(this, ...) {
    JBar(jobj = jCall(this$.jobj, "Lsystemdb/data/Bar;", "asBar"))
})

method("addTo_by_String_double", "JProtoBar", enforceRCC = TRUE, function(this, quoteType = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "addTo", the(quoteType), theNumeric(value))
})

