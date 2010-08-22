constructor("JSymbolConfigFile", function(jobj = NULL) {
    extend(JObject(), "JSymbolConfigFile", .jobj = jobj)
})

method("by_String", "JSymbolConfigFile", enforceRCC = TRUE, function(static, filename = NULL, ...) {
    JSymbolConfigFile(jNew("systemdb/metadata/SymbolConfigFile", the(filename)))
})

method("deleteFolder_by_String", "JSymbolConfigFile", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "V", "deleteFolder", the(name))
})

method("hasFolder_by_String", "JSymbolConfigFile", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Z", "hasFolder", the(name))
})

method("folder_by_String", "JSymbolConfigFile", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JFolder(jobj = jCall(this$.jobj, "Lsystemdb/metadata/SymbolConfigFile/Folder;", "folder", the(name)))
})

method("save", "JSymbolConfigFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "save")
})

method("addFolder_by_String", "JSymbolConfigFile", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JFolder(jobj = jCall(this$.jobj, "Lsystemdb/metadata/SymbolConfigFile/Folder;", "addFolder", the(name)))
})

