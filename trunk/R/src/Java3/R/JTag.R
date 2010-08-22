constructor("JTag", function(jobj = NULL) {
    extend(JObject(), "JTag", .jobj = jobj)
})

method("by_Element", "JTag", enforceRCC = TRUE, function(static, element = NULL, ...) {
    JTag(jNew("util/Tag", .jcast(element$.jobj, "org.jdom.Element")))
})

method("by_String", "JTag", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JTag(jNew("util/Tag", the(name)))
})

method("date_by_String", "JTag", enforceRCC = TRUE, function(this, string = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "date", the(string)))
})

method("longg_by_String", "JTag", enforceRCC = TRUE, function(this, string = NULL, ...) {
    jCall(this$.jobj, "J", "longg", the(string))
})

method("integer_by_String", "JTag", enforceRCC = TRUE, function(this, string = NULL, ...) {
    jCall(this$.jobj, "I", "integer", the(string))
})

method("removeChildren", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "removeChildren")
})

method("requireEmpty", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "requireEmpty")
})

method("decimal_by_String", "JTag", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "D", "decimal", the(name))
})

method("text_by_String", "JTag", enforceRCC = TRUE, function(this, childName = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "text", the(childName))
})

method("delete", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "delete")
})

method("hasChild", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasChild")
})

method("hasChild_by_String", "JTag", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Z", "hasChild", the(name))
})

method("text", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "text")
})

method("hasChild_by_String_String", "JTag", enforceRCC = TRUE, function(this, name = NULL, text = NULL, ...) {
    jCall(this$.jobj, "Z", "hasChild", the(name), the(text))
})

method("children", "JTag", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "children"))
})

method("children_by_String", "JTag", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "children", the(name)))
})

method("setText_by_String", "JTag", enforceRCC = TRUE, function(this, text = NULL, ...) {
    jCall(this$.jobj, "V", "setText", the(text))
})

method("add_by_String_String", "JTag", enforceRCC = TRUE, function(this, name = NULL, text = NULL, ...) {
    JTag(jobj = jCall(this$.jobj, "Lutil/Tag;", "add", the(name), the(text)))
})

method("child_by_String", "JTag", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JTag(jobj = jCall(this$.jobj, "Lutil/Tag;", "child", the(name)))
})

method("requireName_by_String", "JTag", enforceRCC = TRUE, function(this, expected = NULL, ...) {
    JTag(jobj = jCall(this$.jobj, "Lutil/Tag;", "requireName", the(expected)))
})

method("name", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("equals_by_Object", "JTag", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("parent", "JTag", enforceRCC = TRUE, function(this, ...) {
    JTag(jobj = jCall(this$.jobj, "Lutil/Tag;", "parent"))
})

method("toString", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("tag_by_String", "JTag", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JTag(jobj = jCall("util/Tag", "Lutil/Tag;", "tag", the(name)))
})

method("parse_by_String", "JTag", enforceRCC = TRUE, function(static, xml = NULL, ...) {
    JTag(jobj = jCall("util/Tag", "Lutil/Tag;", "parse", the(xml)))
})

method("longXml", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "longXml")
})

method("xml", "JTag", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "xml")
})

method("add_by_Tag", "JTag", enforceRCC = TRUE, function(this, child = NULL, ...) {
    JTag(jobj = jCall(this$.jobj, "Lutil/Tag;", "add", .jcast(child$.jobj, "util.Tag")))
})

method("add_by_String", "JTag", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JTag(jobj = jCall(this$.jobj, "Lutil/Tag;", "add", the(name)))
})

