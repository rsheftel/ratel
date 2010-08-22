library("GSFCore")

testMapConstructor <- function() {
    foo <- Map("Object", "zoo")
    checkInherits(foo, "Map")

    checkLength(foo, 0)
    key1 <- Object()
    val1 <- zoo(1:4)
    foo$set(key1, val1)
    checkLength(foo, 1)
    checkSame(val1, foo$fetch(key1))
    checkTrue(foo$has(key1))
    checkFalse(foo$has(Object()))

    shouldBomb(foo$has(val1))
    shouldBomb(foo$fetch(Object()))

    key2 <- Object()
    foo$set(key2, zoo(1:3))
    checkLength(foo, 2)
    checkLength(foo$keys(), 2)
    foo$remove(key1)
    checkLength(foo, 1)
    checkLength(foo$keys(), 1)
    checkFalse(foo$has(key1))
    checkTrue(foo$has(key2))
}

testObjectObjectMap <- function() {
    foo <- Map("Object", "Object")
    key1 <- Object()
    val1 <- Object()
    foo$set(key1, val1)
    checkTrue(foo$has(key1))
    checkFalse(foo$has(val1))
}

testCharacterCharacterMap <- function() {
    foo <- Map("character", "character")
    key1 <- "I am a key"
    val1 <- "I am a value"
    foo$set(key1, val1)
    checkSame(val1, foo$fetch(key1))
    checkTrue(foo$has(key1))
    checkFalse(foo$has(val1))
}

testMapOfMapValueIsReference <- function() {
    foo <- Map("character", "Map")
    submap <- Map("numeric", "numeric")
    submap$set(123, 456)
    foo$set("abc", submap)
    submap$set(123, 987)
    checkSame(foo$fetch("abc")$fetch(123), 987)
}

testMapOfZooValueIsCopy <- function() {
    foo <- Map("character", "zoo")
    z <- zoo(1:3)
    foo$set("abc", z)
    z[[2]] <- 5
    checkNotEquals(foo$fetch("abc"), z)
}

testMapKeys <- function() {
    foo <- Map("character", "numeric")
    foo$set("a", 1)
    foo$set("b", 1)
    foo$set("c", 1)
    checkSameSet(foo$keys(), c("a", "b", "c"))

    objects <- list(Object(), Object(), Object())

    foo <- Map("Object", "integer")
    for(i in 1:3)
        foo$set(objects[[i]], i)
    checkSameSet(foo$keys(), objects)
}

testMapValues <- function() {
    foo <- Map("character", "numeric")
    foo$set("a", 1)
    foo$set("b", 1)
    foo$set("c", 1)
    checkSameSet(foo$values(), c(1,1,1))
}

testMapKeysValuesAreParallel <- function() {
    foo <- Map("character", "character")
    for(i in c("a", "asdf", "gfsd", "foo", "bar", "baz", "aapl", "apple", "goo", "jeff", "is", "a", "dork"))
        foo$set(i,i)
    checkSame(foo$keys(), foo$values())
}

testMapSetAll <- function() {
    foo <- Map("character", "numeric")
    foo$setAll(c("a", "b", "c"), c(1,2,3))
    checkSameSet(foo$keys(), c("a", "b", "c"))
    checkSameSet(foo$values(), c(1,2,3))

    shouldBomb(foo$setAll(c("e", "f"), c(1)))
    shouldBomb(foo$setAll(c("f"), c(1, 2)))
     
    foo <- Map$from(1:3, letters[1:3])
    checkSame("b", foo$fetch(2L))
}

testMapFetchAll <- function() {
    foo <- Map("character", "numeric")
    foo$setAll(c("a", "b", "c"), c(1,2,3))

    checkSame(list(1,3), foo$fetchAll(c("a", "c")))
    checkSame(list(3,1), foo$fetchAll(c("c", "a")))
    checkSame(list(3,1), foo$fetchAll(list("c", "a")))
    shouldBombMatching(foo$fetchAll("e"), "not found in map")
}

testMapCheckEquals <- function() {
    foo <- Map$from(1:3, letters[1:3])
    bar <- Map$from(1:3, letters[1:3])
    checkSame(foo, bar)

    baz <- Map$from(2:4, letters[1:3])
    shouldBomb(checkSame(foo, baz))

}

testMapCopy <- function() {
    foo <- Map$from(1:3, letters[1:3])
    bar <- foo$copy()
    checkSame(foo, bar)
    checkFalse(identical(foo, bar))
}

testMapEmptyCopy <- function() {
    foo <- Map$from(1:3, letters[1:3])
    checkSame(foo$emptyCopy(), Map("integer", "character"))
}

# duplicated from testObjectHelpers
checkIsType <- function(val, type) { checkTrue(isType(val, type), squish(substitute(val), " is not of type ", type)) }
checkIsNotType <- function(val, type) { checkFalse(isType(val, type), squish(substitute(val), " IS of type ", type)) }

testMapIsType <- function() {
    checkIsType(Map("numeric", "numeric"), "Map")
    checkIsNotType(Map("numeric", "character"), "Map(numeric, numeric)")
    checkIsNotType(Map("character", "numeric"), "Map(numeric, numeric)")
    checkIsType(Map("numeric", "numeric"), "Map(numeric, numeric)")
    checkIsType(Map("numeric", "SimpleTestClass"), "Map(numeric, SimpleTestClass)")
    checkIsNotType(Map("numeric", "SimpleTestClass"), "Map(numeric, NonexistentClass)")
    checkIsType(Map("numeric", "list(numeric)"), "Map(numeric, list(numeric))")
}





