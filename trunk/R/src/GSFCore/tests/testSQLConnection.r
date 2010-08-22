cat("\n\nTest cases for SQLConnection object\n\n")

library("GSFCore")

testInit <- function() {
    conn <- SQLConnection()

    checkTrue( is(conn, "SQLConnection") )
    checkTrue(!conn$isConnected())

    conn$init( )
    checkTrue(conn$isConnected())
}

testSelect <- function() {
    conn <- initSQLConnection()

    query.results = conn$select("SELECT 1 + 1")
    checkTrue(is.data.frame(query.results))
    checkTrue(length(query.results) == 1)
    checkTrue(query.results[[1,1]] == 2)
}

testQuery <- function() {
    conn <- initSQLConnection()

    conn$query("CREATE TABLE #temp1 (col1 INTEGER, col2 VARCHAR(255))")
    conn$query("INSERT INTO #temp1 VALUES (3, 'abcde')")
    conn$query("INSERT INTO #temp1 VALUES (312, 'zyxwv')")
    conn$query("INSERT INTO #temp1 VALUES (69, 'eric rocks')")
    query.results = conn$select("SELECT * from #temp1")
    checkEquals(sort(query.results[,1]),  c(3, 69, 312))

    conn$query("DELETE FROM #temp1")
    conn$query(paste("INSERT INTO #temp1 VALUES (", c(2,4,6,8), ", 'test')", sep = ""))
    query.results = conn$select("SELECT * from #temp1")
    checkEquals(sort(query.results[,1]),  c(2,4,6,8))
}

testBadSelectException <- function() {
    conn <- initSQLConnection()
    shouldBomb(conn$select("SELCT 1+1"))
}

testBadQueryException <- function() {
    conn <- initSQLConnection()
    shouldBomb(conn$query("CRETA TABLE #temp1 (col1 INTEGER, col2 VARCHAR(255))"))
}

testDisconnect <- function() {
    conn <- initSQLConnection()
    conn$disconnect() 
    checkTrue(!conn$isConnected())
}

initSQLConnection <- function() {
    (conn <- SQLConnection())$init()
    conn
}

testCommitRollback <- function() {
    conn <- initSQLConnection()
    conn$setAutoCommit(FALSE)
    on.exit(conn$setAutoCommit(TRUE))
    conn$query("CREATE TABLE #temp1 (col1 INTEGER, col2 VARCHAR(255))")
    conn$query("INSERT INTO #temp1 VALUES (3, 'abcde')")
    checkSame(the(conn$select("SELECT col1 FROM #temp1")), 3)
    conn$rollback()
    checkTrue(conn$isConnected())
    shouldBombMatching(conn$select("SELECT col1 FROM #temp1"), "Invalid object name '#temp1'")
    checkSame(the(conn$select("SELECT 1+2")), 3)
    
    # now set autocommit TRUE and show its behavior is still working.
}

testTransactionSuccess <- function() {
    conn <- initSQLConnection()
    queries <- function() {
        conn$query("CREATE TABLE #temp1 (col1 INTEGER, col2 VARCHAR(255))")
        conn$query("INSERT INTO #temp1 VALUES (3, 'abcde')")
        checkSame(the(conn$select("SELECT col1 FROM #temp1")), 3)
    }

    conn$transaction(queries)
    checkTrue(conn$getAutoCommit())
    checkLength(conn$select("SELECT col1 FROM #temp1"), 1)
}

testTransactionFailure <- function() {
    conn <- initSQLConnection()
    errorMidQueries <- function() {
        conn$query("CREATE TABLE #temp1 (col1 INTEGER, col2 VARCHAR(255))")
        conn$query("INSERT INTO #temp1 VALUES (3, 'abcde')")
        checkSame(the(conn$select("SELECT col1 FROM #temp1")), 3)
        throw("I am not an error")
    }

    shouldBombMatching(conn$transaction(errorMidQueries), "I am not an error")
    checkTrue(conn$getAutoCommit())
    shouldBombMatching(conn$select("SELECT col1 FROM #temp1"), "Invalid object name '#temp1'")

    shouldBombMatching(errorMidQueries(), "I am not an error")
    checkLength(conn$select("SELECT col1 FROM #temp1"), 1)
}

noop <- function() {}

testTransactionBombsIfNotInAutoCommitMode <- function() {
    conn <- initSQLConnection()
    conn$setAutoCommit(FALSE)
    on.exit(function() conn$setAutoCommit(TRUE))
    shouldBombMatching(conn$transaction(noop), "not in AutoCommit mode")
}

testNestedTransactionBombs <- function() {
    conn <- initSQLConnection()
    shouldBombMatching(conn$transaction(function() { 
        conn$transaction(noop)
    }), "within.*transaction")
}

testSelectTimeOutError <- function() {
    conn <- initSQLConnection()
    checkSame(the(conn$select("select 1 + 2")), 3)
    Sys.setenv(RJDBC_THROW_TIMEOUT=1)
    on.exit(function() { Sys.setenv(RJDBC_THROW_TIMEOUT="") })
    shouldBombMatching(
        dbGetQuery(conn$.dbh, "select 1 + 1"), 
        ":ResultSet::next failed (I/O Error: Read timed out)"
    )
    checkSame(as.numeric(Sys.getenv("RJDBC_THROW_TIMEOUT")), 0)
    shouldBombMatching(
        conn$select("select 1 + 2"), 
        "Invalid state, the Connection object is closed."
    )

    conn$init()
    Sys.setenv(RJDBC_THROW_TIMEOUT=1)
    checkSame(the(conn$select("select 1 + 4")), 5)

    
    Sys.setenv(RJDBC_THROW_TIMEOUT=1)
    shouldBombMatching(
        conn$transaction(function() conn$select("select 1 + 5")), 
        ":ResultSet::next failed (I/O Error: Read timed out)"
    )
}
