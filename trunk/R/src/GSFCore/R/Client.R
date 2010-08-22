constructor("Client", function(host = NULL, port = NULL, connection = NULL) {
    this <- extend(RObject(), "Client", .host = host, .port = port, .connection = connection, .readOnlyConnection = FALSE)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, host = "character", port = "numeric", connection = "connection?")
    if(!is.null(this$.connection)) this$.readOnlyConnection <- TRUE
    this
})

method("run", "Client", function(this, expr, ...) {
    if(!this$connected())
        this$open()
    writeLines(deparse(substitute(expr)), this$connection())
    writeLines("end", this$connection())
    if(!this$.readOnlyConnection)
        unserialize(this$connection())
# todo - check for error and throw() rather than just return the error object
})

method("assign", "Client", function(this, ...) {
    if(!this$connected())
        this$open()
    l <- list(...)
    writeLines("assign", this$connection())
    serialize(l, this$connection())
    if(!this$.readOnlyConnection)
        unserialize(this$connection())
})

method("quit", "Client", function(this, ...) {
    failUnless(this$connected(), "connection is not open")
    writeLines("quit", this$connection())
    this$close()
})

method("connected", "Client", function(this, ...) {
    !is.null(this$.connection)
})

method("connection", "Client", function(this, ...) {
    failUnless(this$connected(), "connection is not open")
    this$.connection
})


method("open", "Client", function(this, ...) {
    failIf(this$connected(), "connection must be closed before it can be reopened")
    this$.connection <- socketConnection(host = this$.host, port = this$.port, server = FALSE, blocking=TRUE)
})

method("close", "Client", function(this, ...) {
    close(this$connection())
    this$.connection <- NULL
})
