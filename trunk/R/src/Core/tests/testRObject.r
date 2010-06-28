library("Core")

testRObjectConstructor <- function() {
    obj <- SimpleTestClass(123)
    checkInherits(obj, c("SimpleTestClass", "RObject", "Object"))
}

testCheckEquals <- function() {
    obj <- SimpleTestClass(123)
    checkEquals(obj, obj)
    obj2 <- SimpleTestClass(123)
    checkEquals(obj, obj2)
    obj2 <- SimpleTestClass(234)
    checkNotEquals(obj, obj2)
}

testObjectIdentity <- function() { 
    o1 <- RObject()
    o2 <- RObject()
    o3 <- o1
    
    checkSame(o1, o2)
    checkSame(o1, o3)
    checkIdentical(o1, o3)
    shouldBombMatching(checkIdentical(o1, o2), "did not match")
    o1$requireIdentical(o3)
    shouldBombMatching(o1$requireIdentical(o2), "did not match")
    o1$requireIdentical(o1)
    o3$requireIdentical(o1)
}

testEquals <- function() {
    obj <- SimpleTestClass(123)
    checkTrue(equals(obj, obj))
    obj2 <- SimpleTestClass(123)
    checkTrue(equals(obj, obj2))
    obj2 <- SimpleTestClass(234)
    checkFalse(equals(obj, obj2))
}

testEqualsWithNestedClass <- function() { 
    obj <- NotSimpleTestClass(123)
    obj.same <- NotSimpleTestClass(123)
    obj.diff <- NotSimpleTestClass(124)

    checkTrue(equals(obj, obj.same))
    checkFalse(equals(obj, obj.diff))

    obj <- NotSimple2(123)
    obj.same <- NotSimple2(123)
    obj.diff <- NotSimple2(124)

    checkTrue(equals(obj, obj.same))
    checkFalse(equals(obj, obj.diff))
}

testEqualsClassMismatch <- function() {
    checkFalse(equals(RObject(), Object()))
}
