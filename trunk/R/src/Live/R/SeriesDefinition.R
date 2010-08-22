constructor("SeriesDefinition", function(template = NULL, record = NULL, field = NULL, ...) {
    this <- extend(RObject(), "SeriesDefinition", .template = template, .record = record, .field = field)
    constructorNeeds(this, template="character", record="character", field="character")
    this
})

method("as.character", "SeriesDefinition", function(this, ...) {
    paste(this$.template, this$.record, this$.field, sep=":||:")
})

method("withValues", "SeriesDefinition", function(class, strings, ...) {
    needs(strings="character")
    result <- Map("SeriesDefinition", "SeriesValue")
    partss <- strsplit(strings, ':||:', fixed=TRUE)
    for(parts in partss) {
        changed <- as.logical(fifth(parts))
        def <- SeriesDefinition(first(parts), second(parts), third(parts))
        value <- ifElse(def$isTimestamp(), noWarnings(as.POSIXct(fourth(parts))), noWarnings(as.numeric(fourth(parts))))
        failIf(is.na(changed) || is.na(value), "value in ", parts, " could not be parsed")
        seriesValue <- SeriesValue(value, changed)
        result$set(def, seriesValue)
    }
    result
})

method("valueString", "SeriesDefinition", function(this, value, ...) {
    needs(value="numeric")
    valueString <- format(value, scientific=FALSE, nsmall=12, trim=TRUE)
    squish(as.character(this), ':||:', valueString)
})
    
method("errorString", "SeriesDefinition", function(this, msg, ...) {
    needs(msg="character")
    failIf(nchar(msg) > 14, "error string limited to 14 characters")
    squish(as.character(this), ':||:ERROR:', msg)
})

method("from", "SeriesDefinition", function(static, template, record, fields, ...) {
    needs(template = "character", record = "character", fields = "character|list(character)")
    sapply(fields, function(f) SeriesDefinition(template, record, f), simplify=FALSE) 
})

method("isTimestamp", "SeriesDefinition", function(this, ...) {
     matches("timestamp", tolower(this$.field))
})

method("now", "SeriesDefinition", function(this, ...) {
    squish(as.character(this), ':||:', format(Sys.time(), "%Y/%m/%d %H:%M:%S"))
})

# used only in tests - changes one element of a string like 'a:||:b:||:foobar:||:
method("changePart", "SeriesDefinition", function(static, record, partIndex, newValue, ...) { 
    record = unlist(record)
    needs(record = "character", partIndex = "numeric", newValue = "character")
    parts <- the(strsplit(record, ':||:', fixed=TRUE))
    parts[[partIndex]] <- newValue
    join(':||:', parts)
})
