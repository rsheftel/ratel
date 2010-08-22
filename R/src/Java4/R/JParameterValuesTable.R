constructor("JParameterValuesTable", function(jobj = NULL) {
    extend(JObject(), "JParameterValuesTable", .jobj = jobj)
})

method("by_String", "JParameterValuesTable", enforceRCC = TRUE, function(static, alias = NULL, ...) {
    JParameterValuesTable(jNew("systemdb/metadata/ParameterValuesTable", the(alias)))
})

method("by", "JParameterValuesTable", enforceRCC = TRUE, function(static, ...) {
    JParameterValuesTable(jNew("systemdb/metadata/ParameterValuesTable"))
})

method("param_by_String_String_String", "JParameterValuesTable", enforceRCC = TRUE, function(this, system = NULL, pv = NULL, parameterName = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "param", the(system), the(pv), the(parameterName))
})

method("param_by_String_Pv_String", "JParameterValuesTable", enforceRCC = TRUE, function(this, system = NULL, pv = NULL, parameterName = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "param", the(system), .jcast(pv$.jobj, "systemdb.metadata.Pv"), the(parameterName))
})

method("insert_by_String_String_String_String_Date", "JParameterValuesTable", enforceRCC = TRUE, function(this, system = NULL, pvName = NULL, paramName = NULL, paramValue = NULL, asOf = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(system), the(pvName), the(paramName), the(paramValue), .jcast(asOf$.jobj, "java.util.Date"))
})

method("insert_by_String_String_String_String", "JParameterValuesTable", enforceRCC = TRUE, function(this, system = NULL, pvName = NULL, paramName = NULL, paramValue = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(system), the(pvName), the(paramName), the(paramValue))
})

method("params_by_String_Pv", "JParameterValuesTable", enforceRCC = TRUE, function(this, system = NULL, pv = NULL, ...) {
    JMap(jobj = jCall(this$.jobj, "Ljava/util/Map;", "params", the(system), .jcast(pv$.jobj, "systemdb.metadata.Pv")))
})

method("VALUES", "JParameterValuesTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JParameterValuesTable$...VALUES, JParameterValuesTable(jobj = jField("systemdb/metadata/ParameterValuesTable", "Lsystemdb/metadata/ParameterValuesTable;", "VALUES")), log = FALSE)
})

