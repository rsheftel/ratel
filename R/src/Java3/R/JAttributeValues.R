constructor("JAttributeValues", function(jobj = NULL) {
    extend(JObject(), "JAttributeValues", .jobj = jobj)
})

method("by", "JAttributeValues", enforceRCC = TRUE, function(static, ...) {
    JAttributeValues(jNew("tsdb/AttributeValues"))
})

method("by_Map", "JAttributeValues", enforceRCC = TRUE, function(static, values = NULL, ...) {
    JAttributeValues(jNew("tsdb/AttributeValues", .jcast(values$.jobj, "java.util.Map")))
})

method("requireMissing_by_Attribute", "JAttributeValues", enforceRCC = TRUE, function(this, attr = NULL, ...) {
    jCall(this$.jobj, "V", "requireMissing", .jcast(attr$.jobj, "tsdb.Attribute"))
})

method("requireHas_by_AttributeValue", "JAttributeValues", enforceRCC = TRUE, function(this, value = NULL, ...) {
    jCall(this$.jobj, "V", "requireHas", .jcast(value$.jobj, "tsdb.AttributeValue"))
})

method("value_by_Attribute", "JAttributeValues", enforceRCC = TRUE, function(this, attribute = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "value", .jcast(attribute$.jobj, "tsdb.Attribute"))
})

method("has_by_Attribute_String", "JAttributeValues", enforceRCC = TRUE, function(this, attr = NULL, value = NULL, ...) {
    jCall(this$.jobj, "Z", "has", .jcast(attr$.jobj, "tsdb.Attribute"), the(value))
})

method("rest", "JAttributeValues", enforceRCC = TRUE, function(this, ...) {
    JAttributeValues(jobj = jCall(this$.jobj, "Ltsdb/AttributeValues;", "rest"))
})

method("first", "JAttributeValues", enforceRCC = TRUE, function(this, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "first"))
})

method("isEmpty", "JAttributeValues", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("addOrAppend_by_AttributeValue", "JAttributeValues", enforceRCC = TRUE, function(this, value = NULL, ...) {
    jCall(this$.jobj, "V", "addOrAppend", .jcast(value$.jobj, "tsdb.AttributeValue"))
})

method("iterator", "JAttributeValues", enforceRCC = TRUE, function(this, ...) {
    JIterator(jobj = jCall(this$.jobj, "Ljava/util/Iterator;", "iterator"))
})

method("equals_by_Object", "JAttributeValues", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JAttributeValues", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("replace_by_AttributeValue", "JAttributeValues", enforceRCC = TRUE, function(this, value = NULL, ...) {
    jCall(this$.jobj, "V", "replace", .jcast(value$.jobj, "tsdb.AttributeValue"))
})

method("remove_by_Attribute", "JAttributeValues", enforceRCC = TRUE, function(this, a = NULL, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "remove", .jcast(a$.jobj, "tsdb.Attribute")))
})

method("size", "JAttributeValues", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

method("get_by_Attribute", "JAttributeValues", enforceRCC = TRUE, function(this, attribute = NULL, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "get", .jcast(attribute$.jobj, "tsdb.Attribute")))
})

method("has_by_Attribute", "JAttributeValues", enforceRCC = TRUE, function(this, attribute = NULL, ...) {
    jCall(this$.jobj, "Z", "has", .jcast(attribute$.jobj, "tsdb.Attribute"))
})

method("join_by_String_List", "JAttributeValues", enforceRCC = TRUE, function(this, delim = NULL, attributes = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "join", the(delim), .jcast(attributes$.jobj, "java.util.List"))
})

method("join_by_String_AttributeArray", "JAttributeValues", enforceRCC = TRUE, function(this, delim = NULL, attributes = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "join", the(delim),  {
        if(inherits(attributes, "jarrayRef")) {
            attributes <- .jevalArray(attributes)
        }
        jArray(lapply(attributes, function(x) {
            x$.jobj
        }), "tsdb/Attribute")
    })
})

method("add_by_AttributeValue", "JAttributeValues", enforceRCC = TRUE, function(this, v = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(v$.jobj, "tsdb.AttributeValue"))
})

method("add_by_AttributeValues", "JAttributeValues", enforceRCC = TRUE, function(this, newValues = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(newValues$.jobj, "tsdb.AttributeValues"))
})

method("attributes", "JAttributeValues", enforceRCC = TRUE, function(this, ...) {
    JSet(jobj = jCall(this$.jobj, "Ljava/util/Set;", "attributes"))
})

method("toString", "JAttributeValues", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("values_by_AttributeValueArray", "JAttributeValues", enforceRCC = TRUE, function(static, values = NULL, ...) {
    JAttributeValues(jobj = jCall("tsdb/AttributeValues", "Ltsdb/AttributeValues;", "values",  {
        if(inherits(values, "jarrayRef")) {
            values <- .jevalArray(values)
        }
        jArray(lapply(values, function(x) {
            x$.jobj
        }), "tsdb/AttributeValue")
    }))
})

