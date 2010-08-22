constructor("Server", function(port = NULL, connection = NULL, outputConnection = NULL) {
    this <- extend(RObject(), "Server", 
        .port = port, 
        .connection = connection, 
        .outputConnection = outputConnection
    )
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, port = "numeric", connection = "connection?", outputConnection = "connection?")
    failIf(!is.null(connection) && is.null(outputConnection), "if connection supplied, outputConnection cannot be null")
    this
})

method("runOne", "Server", function(this, lines = this$readCmds(), ...) {
    cmd <- asLines(lines)
    eval(parse(text = cmd), .GlobalEnv)
})

method("run", "Server", function(this, ...) {
    if(!this$connected())
        this$open()
    while(this$connected())
        tryCatch({
            res <- this$runOne()
            if(this$connected())
                serialize(res, this$.outputConnection)
        }, error=function(e) {
            print(squish("error occurred ", e))
            serialize(e, this$.outputConnection)
        })
})

method("connected", "Server", function(this, ...) {
    !is.null(this$.connection)
})

method("connection", "Server", function(this, ...) {
    failUnless(this$connected(), "connection is not open")
    this$.connection
})


method("readCmds", "Server", function(this, ...) {
    connection <- this$connection()
    line <- readLines(connection, 1)
    while(length(line) != 1) {
        Sys.sleep(1)
        line <- readLines(connection, 1)
    }

    if (line == "quit") {
       this$close()
       return()
    }

    if (line == "assign") {
        l <- unserialize(this$connection())
        attach(l)
        return("TRUE")
    }

    lines <- c()
    while (length(line) == 1 && line != "end") {
        lines <- c(lines, line)
        line <- readLines(connection , 1)
    }
    if (squish(last(lines)) == "") lines <- lines[-length(lines)]
    lines
})

method("open", "Server", function(this, ...) {
    failIf(this$connected(), "connection must be closed before it can be reopened")
    this$.connection <- socketConnection(port = this$.port, server = TRUE, blocking=FALSE)
    this$.outputConnection <- this$.connection
})

method("close", "Server", function(this, ...) {
    close(this$connection())
    this$.connection <- NULL
})
