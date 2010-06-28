constructor("RObject", function() {
    extend(Object(), "RObject")
})

method("fields", "RObject", function(this, ...) {
    fields <- getFields(this, private = TRUE)
    fields[-grep("^\\.\\.\\.", fields)]
})

method("equals", "RObject", function(a, b, ...) {
    if(!identical(class(a), class(b))) return(FALSE)
    if(!identical(fields(a), fields(b))) return(FALSE)

    for(f in fields(a)) {
        if (inherits(a[[f]], "RObject")) {
            if (!equals(a[[f]], b[[f]])) return(FALSE)
        } else if(!isTRUE(all.equal(a[[f]], b[[f]])))
            return(FALSE)

    }
        

    TRUE
})

all.equal.Object <- function(target, current, ...) {
    ifelse(equals(target, current, ...), TRUE, squish(as.character(target), " != ", as.character(current)))
}

method("identity", "RObject", function(this, ...) {
    as.character.Object(this)
})

method("requireIdentical", "RObject", function(this, other, ...) {
    checkSame(this$identity(), other$identity())
})


method("checkIdentical", "RObject", function(this, b, ...) {
    checkSame(this$identity(), b$identity())
})

