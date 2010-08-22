constructor("Map", function(keyType = NULL, valType = NULL) {
    this <- extend(RObject(), "Map", 
        .data = new.env(hash=TRUE, parent=emptyenv()), 
        .keys = new.env(hash=TRUE, parent=emptyenv()), 
        .keyType = keyType, 
        .valType = valType
    )
    constructorNeeds(this, keyType="character", valType="character")
    this
})

method("length", "Map", function(this, ...) {
    length(this$.data)
})

method("set", "Map", function(this, key, val, ...) {
    needs(key=this$.keyType, val=this$.valType)
    assign(as.character(key), val, env=this$.data)
    assign(as.character(key), key, env=this$.keys)
})

method("fetch", "Map", function(this, key, ...) {
    needs(key=this$.keyType)
    this$requireKey(key)
    get(as.character(key), env=this$.data)
})

method("requireKey", "Map", function(this, key, ...) {
    assert(this$has(key), squish(
        "\nKey '", as.character(key), "' was not found in map.\nKeys available in map:\n", 
        paste("\t", ls(this$.data), collapse="\n")
    )) 
})

method("has", "Map", function(this, key, ...) {
    needs(key=this$.keyType)
    exists(as.character(key), env=this$.data)
})

method("remove", "Map", function(this, key, ...) {
    needs(key=this$.keyType)
    this$requireKey(key)
    remove(list=as.character(key), envir=this$.data)
    remove(list=as.character(key), envir=this$.keys)
})

method("keys", "Map", function(this, ...) {
    keykeys <- ls(this$.keys, all.names = TRUE)
    lapply(keykeys, get, envir = this$.keys)
})

method("values", "Map", function(this, ...) {
    lapply(this$keys(), this$fetch)
})

method("setAll", "Map", function(this, keys, vals, ...) {
    assert(length(keys) == length(vals))
    mapply(function(key, val) this$set(key, val), keys, vals)
})

method("fetchAll", "Map", function(this, keys, ...) {
    this$requireKeys(keys)
    lapply(keys, function(key) this$fetch(key))
})

method("requireKeys", "Map", function(this, keys, ...) {
    lapply(keys, function(key) this$requireKey(key))
})

method("from", "Map", function(class, keys, vals, ...) {
    assert(length(keys) >= 1)
    assert(length(vals) >= 1)

    result <- Map(class(keys[[1]])[[1]], class(vals[[1]])[[1]])
    result$setAll(keys, vals)
    result
})

method("equals", "Map", function(a, b, ...) {
    if(!equals.RObject(a, b)) return(FALSE)
    isTRUE(all.equal(a$keys(), b$keys())) && isTRUE(all.equal(a$values(), b$values()))
})

method("as.character", "Map", function(this, ...) {
    squish("Map:\n", paste(sapply(this$keys(), as.character), "\t", sapply(this$values(), as.character), collapse = "\n"))
})

method("emptyCopy", "Map", function(this, ...) {
    Map(this$.keyType, this$.valType)
})

method("copy", "Map", function(this, ...) {
    new <- this$emptyCopy()
    new$setAll(this$keys(), this$values())
    new
})

method("isType", "Map", function(val, type, ...) {
    if(substr(type,1,3) != "Map") return(FALSE)
    inner.type <- innerType(type)
    if(is.null(inner.type)) return(TRUE)
    match.index <- regexpr(",", inner.type)
    if (match.index == -1) throw("couldn't find key/val separator")
    keyType <- substr(inner.type, 1, match.index - 1)
    valType <- substr(inner.type, match.index + 2, nchar(inner.type)+1)
    val$.keyType == keyType && val$.valType == valType
})
