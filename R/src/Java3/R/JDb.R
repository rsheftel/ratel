constructor("JDb", function(jobj = NULL) {
    extend(JObject(), "JDb", .jobj = jobj)
})

method("by", "JDb", enforceRCC = TRUE, function(static, ...) {
    JDb(jNew("db/Db"))
})

method("doInSidestepTransaction_by_SidestepThreadResult", "JDb", enforceRCC = TRUE, function(static, transaction = NULL, ...) {
    JObject(jobj = jCall("db/Db", "Ljava/lang/Object;", "doInSidestepTransaction", .jcast(transaction$.jobj, "db.SidestepThreadResult")))
})

method("beInReadOnlyMode_by_boolean", "JDb", enforceRCC = TRUE, function(static, b = NULL, ...) {
    jCall("db/Db", "V", "beInReadOnlyMode", theLogical(b))
})

method("startTransaction", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "startTransaction")
})

method("hasPrimaryKey_by_String_String", "JDb", enforceRCC = TRUE, function(static, dbName = NULL, tableName = NULL, ...) {
    jCall("db/Db", "Z", "hasPrimaryKey", the(dbName), the(tableName))
})

method("identity", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "I", "identity")
})

method("explicitlyCommitted", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "Z", "explicitlyCommitted")
})

method("beInReadOnlyMode", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "beInReadOnlyMode")
})

method("getOutOfNoCommitTestMode", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "getOutOfNoCommitTestMode")
})

method("beInNoCommitTestMode", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "beInNoCommitTestMode")
})

method("setQueryTimeout_by_int", "JDb", enforceRCC = TRUE, function(static, timeoutSecs = NULL, ...) {
    jCall("db/Db", "I", "setQueryTimeout", theInteger(timeoutSecs))
})

method("execute_by_String", "JDb", enforceRCC = TRUE, function(static, sql = NULL, ...) {
    jCall("db/Db", "V", "execute", the(sql))
})

method("startTransactionIgnoringReadonly", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "startTransactionIgnoringReadonly")
})

method("reallyRollback", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "reallyRollback")
})

method("rollback", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "rollback")
})

method("inTransaction", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "Z", "inTransaction")
})

method("reallyCommit", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "reallyCommit")
})

method("commit", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "V", "commit")
})

method("fakeSql_by_Select", "JDb", enforceRCC = TRUE, function(static, select = NULL, ...) {
    jCall("db/Db", "Ljava/lang/String;", "fakeSql", .jcast(select$.jobj, "db.Select"))
})

method("tableExists_by_String_String", "JDb", enforceRCC = TRUE, function(static, dbName = NULL, tableName = NULL, ...) {
    jCall("db/Db", "Z", "tableExists", the(dbName), the(tableName))
})

method("string_by_String", "JDb", enforceRCC = TRUE, function(static, sql = NULL, ...) {
    jCall("db/Db", "Ljava/lang/String;", "string", the(sql))
})

method("currentDb", "JDb", enforceRCC = TRUE, function(static, ...) {
    jCall("db/Db", "Ljava/lang/String;", "currentDb")
})

