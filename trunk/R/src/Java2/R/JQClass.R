constructor("JQClass", function(jobj = NULL) {
    extend(JObject(), "JQClass", .jobj = jobj)
})

method("by_Class", "JQClass", enforceRCC = TRUE, function(static, c = NULL, ...) {
    JQClass(jNew("util/QClass", .jcast(c$.jobj, "java.lang.Class")))
})

method("field_by_String", "JQClass", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JQField(jobj = jCall(this$.jobj, "Lutil/QField;", "field", the(name)))
})

method("fields", "JQClass", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "fields"))
})

method("method_by_String", "JQClass", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JQMethod(jobj = jCall(this$.jobj, "Lutil/QMethod;", "method", the(name)))
})

method("methods_by_String", "JQClass", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "methods", the(name)))
})

method("constructors", "JQClass", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "constructors"))
})

method("methods", "JQClass", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "methods"))
})

method("allMethods", "JQClass", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "allMethods"))
})

method("allClassesInPackage_by_Class", "JQClass", enforceRCC = TRUE, function(static, c = NULL, ...) {
    JList(jobj = jCall("util/QClass", "Ljava/util/List;", "allClassesInPackage", .jcast(c$.jobj, "java.lang.Class")))
})

method("allConstructors", "JQClass", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "allConstructors"))
})

