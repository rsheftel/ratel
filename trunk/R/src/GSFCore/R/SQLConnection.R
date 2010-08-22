library("R.oo")

isDebugSql <- function() {
    Sys.getenv("DEBUGSQL") != ""
}

debugSql <- function(on = TRUE) {
    value <- ifelse(on, 1, "")
    Sys.setenv(DEBUGSQL = value)
    JLog$debugSql_by_boolean(on)
}

constructor("SQLConnection", function(socketTimeout = 2) {
    extend(RObject(), "SQLConnection", 
        .dbh = NULL,
        .connected = FALSE,
        .currentTimeoutSeconds = socketTimeout,
        .baseTimeoutSeconds = socketTimeout,
        .dbname = NULL
    )
})

method("init", "SQLConnection", function(this, dbname = NULL, ...) {
    # TODO: Change to use config file instead
    if(isWindows()) {
        drv <- JDBC("net.sourceforge.jtds.jdbc.Driver", "T:\\jdbcdriver\\jtds-1.2.jar")
    } else {
        drv <- JDBC("net.sourceforge.jtds.jdbc.Driver", "/tp/jdbcdriver/jtds-1.2.2.jar")
    }

    this$.dbname <- dbname

    db.server <- "sqlprodts"
    db.username <- "sim"
    db.password <- "Sim5878"

    if(Sys.getenv("DBSERVER") != "") {
        db.server <- Sys.getenv("DBSERVER")
    }
    if(Sys.getenv("DBUSERNAME") != "") {
        db.username <- Sys.getenv("DBUSERNAME")
    }
    if(Sys.getenv("DBPASSWORD") != "") {
        db.password <- Sys.getenv("DBPASSWORD")
    }

    errorHandler <- function(e, retriesLeft) {
        if(retriesLeft > 0 && matches("Read timed out", conditionMessage(e))) {
            message(
                "SQLConnection: Time Out Occurred on dbConnect! Retries Left: ",
                retriesLeft, "\n"
            )
            tryCatch(connect(), error = function(e) errorHandler(e, retriesLeft - 1))
        } else {
            throw("Error occured in db query: ", ..., "\n", e)
        }
    }

    connect <- function() {
        this$.dbh <- dbConnect(drv, squish(
            "jdbc:jtds:sqlserver://", 
            db.server, 
            ":2433;prepareSQL=2;socketTimeout=", 
            this$.currentTimeoutSeconds
        ), db.username, db.password)
    }

    tryCatch(connect(), error = function(e) errorHandler(e, 3))

    if(identical(this$.dbh, -1))
        throw("Unable to connect to database. " )
    this$.connected <- TRUE
    if(!is.null(dbname))
        this$query(paste("use ", dbname, sep = ""))
})

method("isConnected", "SQLConnection", function(this, ...) {
    this$.connected
})

method("disconnect", "SQLConnection", function(this, ...) {
    if(!isConnected(this))
        throw("Not connected to database.")

    dbDisconnect(this$.dbh)
    this$.connected <- FALSE
})

method("select", "SQLConnection", function(this, ..., suppress.messages = !isDebugSql()) {
    if(!isConnected(this))
        throw("Not connected to database.")

    if(!suppress.messages)
        message(..., "\n\n")

    autoCommit <- this$getAutoCommit()

    errorHandler <- function(e) {
        if(
            autoCommit &&
            matches("Read timed out", conditionMessage(e)) && 
            this$.currentTimeoutSeconds < 512
        ) {
            this$.currentTimeoutSeconds <- this$.currentTimeoutSeconds * 2
            message(
                "SQLConnection: Time Out Occurred!  Trying again with timeout set to ", 
                this$.currentTimeoutSeconds, "\n"
            )
            this$init(this$.dbname)
            this$select(..., suppress.messages = suppress.messages)
        } else {
            throw("Error occured in db query: ", ..., "\n", e)
        }
    }

    result <- tryCatch(dbGetQuery(this$.dbh, ...), error = errorHandler)

    if(this$.currentTimeoutSeconds != this$.baseTimeoutSeconds) {
        this$disconnect()
        this$.currentTimeoutSeconds <- this$.baseTimeoutSeconds
        this$init(this$.dbname)
    }

    result
})

method("query", "SQLConnection", function(this, ..., suppress.messages = !isDebugSql()) {
    stopifnot(isConnected(this))

    lapply(..., function(...) {
        if(!suppress.messages)
            message(..., "\n")
        tryCatch(dbSendUpdate(this$.dbh, ...), error = function(e) throw("Error occured in db query: ", ..., "\n", e))
    })
})

method("getAutoCommit", "SQLConnection", function(this, ...) {
    if(!isConnected(this))
        throw("Not connected to database.")

    ac <- .jcall(this$.dbh@jc, "Z", "getAutoCommit", check=FALSE)
    handleJavaException("getAutoCommit")
    ac
})

method("setAutoCommit", "SQLConnection", function(this, autoCommit, ...) {
    if(!isConnected(this))
        throw("Not connected to database.")

    .jcall(this$.dbh@jc, "V", "setAutoCommit", autoCommit, check=FALSE)
    handleJavaException("setAutoCommit")
})

method("commit", "SQLConnection", function(this, ...) {
    if(!isConnected(this))
        throw("Not connected to database.")

    dbCommit(this$.dbh)
})

method("rollback", "SQLConnection", function(this, ...) {
    if(!isConnected(this))
        throw("Not connected to database.")

    dbRollback(this$.dbh)
})

method("transaction", "SQLConnection", function(this, func, ...) {
    needs(func="function")
    assert(this$getAutoCommit(), "
        cannot call transaction() if we are not in AutoCommit mode 
        (are you calling transaction() from within a transaction()?)
    ")
    this$setAutoCommit(FALSE)
    

    tryCatch({ func(); this$commit() }, error = function(e) {
        tryCatch({
            this$rollback()
            this$setAutoCommit(TRUE)
        }, error = function(x) {})
        throw(e)
    })
    this$setAutoCommit(TRUE)
})

