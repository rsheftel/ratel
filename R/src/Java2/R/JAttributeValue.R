constructor("JAttributeValue", function(jobj = NULL) {
    extend(JObject(), "JAttributeValue", .jobj = jobj)
})

method("compareTo_by_Object", "JAttributeValue", enforceRCC = TRUE, function(this, x0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(x0$.jobj, "java.lang.Object"))
})

method("main_by_StringArray", "JAttributeValue", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("tsdb/AttributeValue", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("append_by_List", "JAttributeValue", enforceRCC = TRUE, function(this, toBeAdded = NULL, ...) {
    jCall(this$.jobj, "V", "append", .jcast(toBeAdded$.jobj, "java.util.List"))
})

method("names", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "names"))
})

method("tsamFilter", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "tsamFilter"))
})

method("value_by_Column", "JAttributeValue", enforceRCC = TRUE, function(this, c = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "value", .jcast(c$.jobj, "db.Column")))
})

method("compareTo_by_AttributeValue", "JAttributeValue", enforceRCC = TRUE, function(this, o = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(o$.jobj, "tsdb.AttributeValue"))
})

method("name", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("toString", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("equals_by_Object", "JAttributeValue", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("fromIds_by_Integer_Integer", "JAttributeValue", enforceRCC = TRUE, function(static, attributeId = NULL, valueId = NULL, ...) {
    JAttributeValue(jobj = jCall("tsdb/AttributeValue", "Ltsdb/AttributeValue;", "fromIds", .jcast(attributeId$.jobj, "java.lang.Integer"), .jcast(valueId$.jobj, "java.lang.Integer")))
})

method("exists", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "exists")
})

method("create_by_CellArray", "JAttributeValue", enforceRCC = TRUE, function(this, extra = NULL, ...) {
    jCall(this$.jobj, "V", "create",  {
        if(inherits(extra, "jarrayRef")) {
            extra <- .jevalArray(extra)
        }
        jArray(lapply(extra, function(x) {
            x$.jobj
        }), "db/Cell")
    })
})

method("createIfNeeded", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "createIfNeeded"))
})

method("createdIfNecessary_by_Attribute_String_CellArray", "JAttributeValue", enforceRCC = TRUE, function(static, attribute = NULL, representation = NULL, extra = NULL, ...) {
    JAttributeValue(jobj = jCall("tsdb/AttributeValue", "Ltsdb/AttributeValue;", "createdIfNecessary", .jcast(attribute$.jobj, "tsdb.Attribute"), the(representation),  {
        if(inherits(extra, "jarrayRef")) {
            extra <- .jevalArray(extra)
        }
        jArray(lapply(extra, function(x) {
            x$.jobj
        }), "db/Cell")
    }))
})

method("create", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "create")
})

method("attribute", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    JAttribute(jobj = jCall(this$.jobj, "Ltsdb/Attribute;", "attribute"))
})

method("matches_by_IntColumn_IntColumn", "JAttributeValue", enforceRCC = TRUE, function(this, attributeId = NULL, valueId = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(attributeId$.jobj, "db.columns.IntColumn"), .jcast(valueId$.jobj, "db.columns.IntColumn")))
})

method("id", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "id")
})

method("ids", "JAttributeValue", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "ids"))
})

method("SPREAD", "JAttributeValue", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttributeValue$...SPREAD, JAttributeValue(jobj = jField("tsdb/AttributeValue", "Ltsdb/AttributeValue;", "SPREAD")), log = FALSE)
})

