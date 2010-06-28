library("Core")

needsOne <- function(a = 1, b = NULL) {
    needs(a = "numeric", b = "character?")
}

testNeeds <- function() {
    needsOne(42, "3")
    needsOne(42)
    shouldBomb(needsOne("3"), "needsOne requires first parameter to be numeric")
    shouldBomb(needsOne(42, 16), "needsOne requires second paramter to be NULL or character")
    shouldBomb(needsOne(NULL, "foo"), "needsOne requires first to be nonnull.")
}

needsComplicatedMap <- function(foo) { 
    needs(foo = "Map(numeric, list(Map(character, numeric)))")
}

#testNeedsComplicatedMap <- function() { 
#    outerMap <- Map("numeric", "list(Map(character, numeric))")
#    theList <- list(NULL)
#    theList[[1]] <- Map("character", "numeric") # don't grow arrays like this except in small (2-3 element) cases
#    theList[[1]]$set("asdf", 7)
#    outerMap$set(19, theList)
#    needsComplicatedMap(outerMap)
#    shouldBomb(needsComplicatedMap(Map("numeric", "numeric")))
#}

testNeedsMultipleTypes <- function() {
    needsMultipleTypes <- function(a = NULL, b = NULL) {
        needs(a = "character|numeric", b = "character|numeric?")
    }

    needsMultipleTypes(42)
    needsMultipleTypes("foo")
    needsMultipleTypes("42")
    needsMultipleTypes(42, 42)
    needsMultipleTypes(42, "foo")
    shouldBomb(needsMultipleTypes())
    shouldBomb(needsMultipleTypes(TRUE))
    shouldBomb(needsMultipleTypes(42, TRUE))
    shouldBomb(needsMultipleTypes(TRUE, 42))
    shouldBomb(needsMultipleTypes(TRUE, TRUE))
}

testIsTypeListBar <- function() {
    checkIsType(SimpleTestClass(4), "SimpleTestClass|list(SimpleTestClass)")
    checkIsType(list(SimpleTestClass(4)), "SimpleTestClass|list(SimpleTestClass)")
}

testNeedsObjectTypeMismatchErrorMessage <- function() {
    broken <- function(a) { 
        needs(a = "SimpleTestClass") 
    }
    shouldBombMatching(broken(4), "a is not SimpleTestClass is numeric")
    shouldBombMatching(broken(RObject()), ":a is not SimpleTestClass is RObject, Object")
    shouldBombMatching(broken(4), "!Assertion failed")
}

checkIsNotType <- function(val, type) { checkFalse(isType(val, type), squish(substitute(val), " IS of type ", type)) }
testIsType <- function() {
    checkIsType(5, "numeric")
    checkIsNotType(5, "character")
    checkIsType(list(5), "list")
    checkIsNotType(list(5), "character")
    checkIsType(list(5), "list(numeric)")
    checkIsNotType(list(5), "list(character)")
    checkIsType(list(list(5)), "list(list(numeric))")
    checkIsType(list(), "list(list(numeric))")
    checkIsType(list(list()), "list(list(numeric))")
    checkIsType(list(list(SimpleTestClass(1))), "list(list(Object))")
    checkIsType(list(list(SimpleTestClass(1))), "list(list(SimpleTestClass))")
    checkIsType(array(5), "array(numeric)")
    checkIsType(matrix(5), "matrix(numeric)")
    m <- array(list(NULL), c(2, 2))
    m[[1, 1]] = list(5)
    checkIsType(m, "matrix(list(numeric))")
    checkIsType(list(m), "list(matrix(list(numeric)))")
}

testConstructorNeeds <- function() {
    SimpleTestClass(42, "3")
    SimpleTestClass(42)
    shouldBomb(SimpleTestClass(), "SimpleTestClass requires a to be non-NULL and numeric, except when called as static constructor")
    shouldBomb(SimpleTestClass("3"), "SimpleTestClass requires first parameter to be numeric")
    shouldBomb(SimpleTestClass(42, 16), "SimpleTestClass requires second paramter to be NULL or character")
    shouldBomb(SimpleTestClass(NULL, "foo"), "SimpleTestClass requires first to be nonnull.")
}

testNeedsTracing <- function() {
    output <- capture.output( {
        Sys.setenv("DEBUG_NEEDS"=1)
        SimpleTestClass(42, "3")
        SimpleTestClass(42)
        Sys.setenv("DEBUG_NEEDS"="")
    })
    checkSame("needs: numeric -> 42 character? -> 3 ", first(output))
    checkSame("needs: numeric -> 42 character? ->  ", output[[2]])

}
