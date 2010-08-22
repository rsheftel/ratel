library("GSFCore")

testArrayConstructor <- function() {
    row.indices <- list(Object(), Object(), Object())
    col.indices <- list(Object(), Object(), Object())
    foo <- Array("Object", "Object", "zoo", row.indices, col.indices)
    checkInherits(foo, "Array")
    checkShape(foo, rownames=row.indices, colnames=col.indices)

    val1 <- zoo(1:4)
    foo$set(row.indices[[1]], col.indices[[3]], val1)
    checkSame(val1, foo$fetch(row.indices[[1]], col.indices[[3]]))

    shouldBomb(foo$fetch(Object(), Object()))
}

testCharacterCharacterArray <- function() {
    rows <- c("I am a row", "I am another row")
    cols <- c("I am a column", "I am another column")
    val1 <- 1234
    foo <- Array("character", "character", "numeric", rows, cols)
    foo$set(rows[[2]], cols[[2]], val1)
    checkSame(val1, foo$fetch(rows[[2]], cols[[2]]))
}

testArraySetRowColumn <- function() {
    rows <- c("I am a row", "I am another row")
    cols <- c("I am a column", "I am another column")
    foo <- Array("character", "character", "numeric", rows, cols)
    foo$setRow(rows[[2]], c(1,2))
    checkSameLooking(foo$fetchRow(rows[[2]]), c(1,2))
    checkTrue(foo$hasRow(rows[[2]]))
    checkFalse(foo$hasRow("I am not a row"))
    shouldBomb(foo$setRow(rows[[2]], c(1,2,3)))
    foo$setRow(rows[[1]], 1)
    checkSameLooking(foo$fetchRow(rows[[1]]), c(1,1))

    foo$setColumn(cols[[2]], c(1,2))
    checkSameLooking(foo$fetchColumn(cols[[2]]), c(1,2))
    shouldBomb(foo$setColumn(cols[[2]], c(1,2,3)))
    checkTrue(foo$hasColumn(cols[[2]]))
    checkFalse(foo$hasColumn("I am not a column"))
    foo$setColumn(cols[[1]], 1)
    checkSameLooking(foo$fetchColumn(cols[[1]]), c(1,1))

}

testArrayCheckEquals <- function() {
    rows <- c("I am a row", "I am another row")
    cols <- c("I am a column", "I am another column")
    val1 <- 1234
    foo <- Array("character", "character", "numeric", rows, cols)
    bar <- Array("character", "character", "numeric", rows, cols)

    checkSame(foo, bar)
    shouldBomb(checkSame(foo, Array("character", "character", "numeric", "different rows", cols)))
    shouldBomb(checkSame(foo, Array("character", "character", "numeric", rows, "different cols")))

    foo$set(rows[[2]], cols[[2]], val1)
    bar$set(rows[[2]], cols[[2]], val1)
    checkSame(foo, bar)
    bar$set(rows[[1]], cols[[1]], 8765)
    shouldBomb(checkSame(foo, bar))
}

testArrayIsType <- function() {
    checkIsType(Array("numeric", "numeric", "Object", 1, 2), "Array")
    checkIsNotType(Array("numeric", "numeric", "Object", 1, 2), "Array(character, numeric, Object)")
    checkIsNotType(Array("numeric", "numeric", "Object", 1, 2), "Array(numeric, character, Object)")
    checkIsNotType(Array("numeric", "numeric", "Object", 1, 2), "Array(numeric, numeric, numeric)")
    checkIsType(Array("numeric", "numeric", "Object", 1, 2), "Array(numeric, numeric, Object)")
    checkIsType(Array("numeric", "character", "list(numeric)", 1, "a"), "Array(numeric, character, list(numeric))")
}

testSubscriptOutOfBoundsGoodErrorMessage <- function() { 
    a <- Array("character", "character", "character", c(letters[1:4]), c(letters[5:8]))
    a$setRow("a", letters[11:14])
    checkSame(a$fetch("a", "e"), letters[11])
    shouldBombMatching(a$fetch("e", "e"), ":subscript(e) out of row bounds:<CR>a, b, c, d")
    shouldBombMatching(a$fetch("a", "z"), ":subscript(z) out of col bounds:<CR>e, f, g, h")
}

testArrayEmptyRowIndices <- function() { 
    a <- Array("character", "character", "character", c(letters[1:4]), c(letters[5:8]))
    checkSame(letters[1:4], a$emptyRowIndices())
    checkLength(a$populatedRowIndices(), 0)
    a$setRow("a", letters[11:14])
    checkSame(letters[2:4], a$emptyRowIndices())
    checkSame(letters[1], a$populatedRowIndices())

    objs <- list(Object(), Object(), Object(), Object())
    a <- Array("Object", "character", "Object", objs, c(letters[5:8]))
    checkSame(objs, a$emptyRowIndices())
    checkLength(a$populatedRowIndices(), 0)
    a$setRow(first(objs), objs)
    checkSame(objs[2:4], a$emptyRowIndices())
    checkSame(objs[1], a$populatedRowIndices())
}




