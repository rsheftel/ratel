constructor("JAttribute", function(jobj = NULL) {
    extend(JObject(), "JAttribute", .jobj = jobj)
})

method("compareTo_by_Object", "JAttribute", enforceRCC = TRUE, function(this, x0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(x0$.jobj, "java.lang.Object"))
})

method("main_by_StringArray", "JAttribute", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("tsdb/Attribute", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("extra_by_AttributeValue", "JAttribute", enforceRCC = TRUE, function(this, value = NULL, ...) {
    JRow(jobj = jCall(this$.jobj, "Ldb/Row;", "extra", .jcast(value$.jobj, "tsdb.AttributeValue")))
})

method("compareTo_by_Attribute", "JAttribute", enforceRCC = TRUE, function(this, o = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(o$.jobj, "tsdb.Attribute"))
})

method("name", "JAttribute", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("value_by_Integer", "JAttribute", enforceRCC = TRUE, function(this, id = NULL, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "value", .jcast(id$.jobj, "java.lang.Integer")))
})

method("value_by_TimeSeries", "JAttribute", enforceRCC = TRUE, function(this, series = NULL, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "value", .jcast(series$.jobj, "tsdb.TimeSeries")))
})

method("value_by_StringArray", "JAttribute", enforceRCC = TRUE, function(this, values = NULL, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "value", jArray(values, "[Ljava/lang/String;")))
})

method("value_by_Date", "JAttribute", enforceRCC = TRUE, function(this, d = NULL, ...) {
    JAttributeValue(jobj = jCall(this$.jobj, "Ltsdb/AttributeValue;", "value", .jcast(d$.jobj, "java.util.Date")))
})

method("attribute_by_Integer", "JAttribute", enforceRCC = TRUE, function(static, attributeId = NULL, ...) {
    JAttribute(jobj = jCall("tsdb/Attribute", "Ltsdb/Attribute;", "attribute", .jcast(attributeId$.jobj, "java.lang.Integer")))
})

method("valueName_by_Integer", "JAttribute", enforceRCC = TRUE, function(this, valueId = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "valueName", .jcast(valueId$.jobj, "java.lang.Integer"))
})

method("toString", "JAttribute", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("equals_by_Object", "JAttribute", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JAttribute", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("valuesExist_by_List", "JAttribute", enforceRCC = TRUE, function(this, possibleValues = NULL, ...) {
    jCall(this$.jobj, "Z", "valuesExist", .jcast(possibleValues$.jobj, "java.util.List"))
})

method("createValues_by_List_CellArray", "JAttribute", enforceRCC = TRUE, function(this, newValues = NULL, extra = NULL, ...) {
    jCall(this$.jobj, "V", "createValues", .jcast(newValues$.jobj, "java.util.List"),  {
        if(inherits(extra, "jarrayRef")) {
            extra <- .jevalArray(extra)
        }
        jArray(lapply(extra, function(x) {
            x$.jobj
        }), "db/Cell")
    })
})

method("matches_by_Column", "JAttribute", enforceRCC = TRUE, function(this, attributeId = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(attributeId$.jobj, "db.Column")))
})

method("cacheAllValues", "JAttribute", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "cacheAllValues")
})

method("valueIds_by_List", "JAttribute", enforceRCC = TRUE, function(this, names = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "valueIds", .jcast(names$.jobj, "java.util.List")))
})

method("id", "JAttribute", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "id")
})

method("attribute_by_String", "JAttribute", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JAttribute(jobj = jCall("tsdb/Attribute", "Ltsdb/Attribute;", "attribute", the(name)))
})

method("CDS_STRIKE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...CDS_STRIKE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "CDS_STRIKE")), log = FALSE)
})

method("METRIC", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...METRIC, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "METRIC")), log = FALSE)
})

method("PV", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...PV, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "PV")), log = FALSE)
})

method("SIV", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...SIV, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "SIV")), log = FALSE)
})

method("SYSTEM_ID", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...SYSTEM_ID, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "SYSTEM_ID")), log = FALSE)
})

method("BASE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...BASE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "BASE")), log = FALSE)
})

method("UNDERLYING", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...UNDERLYING, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "UNDERLYING")), log = FALSE)
})

method("INTERVAL", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...INTERVAL, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "INTERVAL")), log = FALSE)
})

method("MARKET", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...MARKET, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "MARKET")), log = FALSE)
})

method("TRANSFORMATION_OUTPUT", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...TRANSFORMATION_OUTPUT, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "TRANSFORMATION_OUTPUT")), log = FALSE)
})

method("TRANSFORMATION", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...TRANSFORMATION, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "TRANSFORMATION")), log = FALSE)
})

method("COUPON", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...COUPON, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "COUPON")), log = FALSE)
})

method("SETTLE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...SETTLE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "SETTLE")), log = FALSE)
})

method("PROGRAM", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...PROGRAM, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "PROGRAM")), log = FALSE)
})

method("INDEX_VERSION", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...INDEX_VERSION, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "INDEX_VERSION")), log = FALSE)
})

method("INDEX_SERIES", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...INDEX_SERIES, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "INDEX_SERIES")), log = FALSE)
})

method("OPTION_CONTRACT", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...OPTION_CONTRACT, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "OPTION_CONTRACT")), log = FALSE)
})

method("OPTION_MONTH_LETTER", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...OPTION_MONTH_LETTER, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "OPTION_MONTH_LETTER")), log = FALSE)
})

method("OPTION_MONTH", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...OPTION_MONTH, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "OPTION_MONTH")), log = FALSE)
})

method("OPTION_YEAR", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...OPTION_YEAR, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "OPTION_YEAR")), log = FALSE)
})

method("OPTION_TYPE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...OPTION_TYPE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "OPTION_TYPE")), log = FALSE)
})

method("DOC_CLAUSE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...DOC_CLAUSE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "DOC_CLAUSE")), log = FALSE)
})

method("FUTURE_MONTH_LETTER", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...FUTURE_MONTH_LETTER, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "FUTURE_MONTH_LETTER")), log = FALSE)
})

method("FUTURE_YEAR", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...FUTURE_YEAR, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "FUTURE_YEAR")), log = FALSE)
})

method("FUTURE_MONTH", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...FUTURE_MONTH, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "FUTURE_MONTH")), log = FALSE)
})

method("CONTRACT", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...CONTRACT, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "CONTRACT")), log = FALSE)
})

method("QUOTE_CONVENTION", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...QUOTE_CONVENTION, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "QUOTE_CONVENTION")), log = FALSE)
})

method("QUOTE_SIDE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...QUOTE_SIDE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "QUOTE_SIDE")), log = FALSE)
})

method("QUOTE_TYPE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...QUOTE_TYPE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "QUOTE_TYPE")), log = FALSE)
})

method("SECURITY_ID", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...SECURITY_ID, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "SECURITY_ID")), log = FALSE)
})

method("FINANCIAL_CENTER", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...FINANCIAL_CENTER, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "FINANCIAL_CENTER")), log = FALSE)
})

method("INSTRUMENT", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...INSTRUMENT, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "INSTRUMENT")), log = FALSE)
})

method("STRIKE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...STRIKE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "STRIKE")), log = FALSE)
})

method("EXPIRY_DATE", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...EXPIRY_DATE, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "EXPIRY_DATE")), log = FALSE)
})

method("EXPIRY", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...EXPIRY, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "EXPIRY")), log = FALSE)
})

method("TICKER", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...TICKER, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "TICKER")), log = FALSE)
})

method("CCY", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...CCY, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "CCY")), log = FALSE)
})

method("CDS_TICKER", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...CDS_TICKER, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "CDS_TICKER")), log = FALSE)
})

method("TIER", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...TIER, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "TIER")), log = FALSE)
})

method("TENOR", "JAttribute", enforceRCC = FALSE, function(static, ...) {
    lazy(JAttribute$...TENOR, JAttribute(jobj = jField("tsdb/Attribute", "Ltsdb/Attribute;", "TENOR")), log = FALSE)
})

