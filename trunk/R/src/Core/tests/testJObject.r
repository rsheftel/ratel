library(Core)

.jinit()

testJObject <- function() {
	obj <- JObject()
	checkInherits(obj, c("JObject", "RObject"))
}

testJNew <- function() {
    obj <- jNew("java/lang/Double", 1.0)
    shouldBombMatching(jNew("jva/lan/Dble", 1.0), "java.lang.NoClassDefFoundError")
}

testJCall <- function() {
	version <- jCall("java/lang/System", "S", "getProperty", "java.version")
	shouldBombMatching(jCall("java/lang/System", "V", "getProperty", "java.version"), "method getProperty with signature")
	shouldBombMatching(jCall("java/lang/System", "S", "getProperty", .jnull("java/lang/String")), "key can't be null")
	shouldBombMatching(jCall("java/lang/System", "S", "getPropery", "java.version"), " not found")
}

testJField <- function() {
	sysout <- jField("java/lang/System", "Ljava/io/PrintStream;", "out")
	shouldBombMatching(jField("java/lang/System", "Ljava/io/PrintStream;", "ot"), "field ot not found")
	shouldBombMatching(jField("java/lang/Sysem", "Ljava/io/PrintStream;", "out"), "NoClassDefFoundError: java.lang.Sysem")
	shouldBombMatching(jField("java/lang/System", "Ljava/io/PrintSream;", "out"), "field out not found")
}

testJArray <- function() {
	a <- jArray(seq(1,3,0.5), "D")
	# TODO: Find a failing test case.  I could not get this to bomb.  --eknell
}

#jcast, jarray
#change generator to use these
