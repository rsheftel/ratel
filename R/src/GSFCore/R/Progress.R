
constructor("Progress", function() {
    this <- extend(RObject(), "Progress", 
        .counts = rep(0, 256),
        .everys = rep(1, 256)
    )
    names(this$.counts) <- ASCII
    names(this$.everys) <- ASCII
    this
})

method("start", "Progress", function(class, symbol, every = 1, ...) {
    failUnless(nchar(symbol) == 1, "symbol must be one ascii character")
    Progress$.counts[symbol] <- 0
    Progress$.everys[symbol] <- every
})

method("on", "Progress", function(class, ...) {
    Sys.setenv(NO_PROGRESS_DOTS="")
})

method("off", "Progress", function(class, ...) {
    Sys.setenv(NO_PROGRESS_DOTS=1)
})

method("isOff", "Progress", function(class, ...) {
    Sys.getenv("NO_PROGRESS_DOTS") != ""
})

noProgress <- function(expr) {
    wasOff <- Progress$isOff()
    Progress$off()
    result <- expr
    if (!wasOff) Progress$on()
    result
}

progress <- function(symbol) { 
    if(Progress$isOff()) return()
    count <- Progress$.counts[symbol] + 1
    every <- Progress$.everys[symbol]
    if (count %% (every * 10) == 0) cat(count %/% (every * 10))
    else if (every == 1) cat(symbol)
    else if (count %% every == 0)
        cat(symbol) 
    Progress$.counts[symbol] = count
}

