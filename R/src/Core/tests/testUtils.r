cat("\n\nTest cases for Utils module\n\n")

library("Core")

testArrayApply <- function() {
    target <- structure(c("a d g 1", "b d g 2", "c d g 3", "a e g 4", "b e g 5",
        "c e g 6", "a f g 7", "b f g 8", "c f g 9", "a d h 10", "b d h 11",
        "c d h 12", "a e h 13", "b e h 14", "c e h 15", "a f h 16", "b f h 17",
        "c f h 18", "a d i 19", "b d i 20", "c d i 21", "a e i 22", "b e i 23",
        "c e i 24", "a f i 25", "b f i 26", "c f i 27"), .Dim = c(3L,
        3L, 3L), .Dimnames = list(c("a", "b", "c"), c("d", "e", "f"), c("g", "h", "i")))

    a <- array(1:27, c(3,3,3), list(c("a", "b", "c"), c("d", "e", "f"), c("g", "h", "i")))
    checkEquals(target, array.apply(a, function(a, b, c, d) paste(a,b,c, d)))
}

testNoWarnings <- function() { 
    checkSame("asdf", noWarnings({ "asdf" }))
}

testStrings <- function() {
    checkSame("987.65", strings(c(987.65)))
    checkSame("987.65", strings(c(a=987.65)))
}

testAssert <- function() {
    assert(TRUE)
    shouldBomb(assert(FALSE))
    
    assert(1 + 1 == 2, "addition should work")
    shouldBomb(assert(2 + 2 == 5), "addition should work")
}

testIsWindows <- function() {
    checkTrue(length(isWindows()) == 1)
    checkTrue(isWindows() || !isWindows())
}

testRunCmd <- function() {
    ##@bdescr
    ## test cases for RunCmd function
    ##@edescr

    if(Sys.getenv("OS") != "Windows_NT") {
        runCmd("/bin/true")
        shouldBomb(runCmd("/bin/false"))
        shouldBomb(runCmd("/usr/bin/this_executable_does_not_exist"))
    }
}

testPathCreatesCorrectPaths <- function() { 
    checkSame(path("foo", "bar"), "foo/bar")
    checkSame(path("/foo"), "/foo")
    checkSame(path("/foo/bar", "baz", "ball", "yerp"), "/foo/bar/baz/ball/yerp")
}

testCheckNull <- function() {
    shouldBombMatching(checkNull(list("a", "b")), ":should be null, but was: (a, b)")
    shouldBombMatching(checkNull(list()), ":should be null, but was: ()")
    checkNull(NULL)
    checkNull(list(0))
}

testFail <- function() {
    shouldBombMatching(fail(), "failed")
    shouldBombMatching(fail("some"), "some")
    shouldBombMatching(fail("the dingle", " was doofed"), "the dingle was doofed")
    shouldBombMatching(fail("a", "b"), "ab")
    shouldBombMatching(fail(list("a", "b")), "a, b")
}

testFailIf <- function() {
    failIf(FALSE)
    failUnless(TRUE)
    shouldBombMatching(failIf(TRUE), "failed")
    shouldBombMatching(failUnless(FALSE), "condition was not met")
    shouldBombMatching(failUnless(FALSE, "the dingle", " was doofed"), "the dingle was doofed")
    shouldBombMatching(failIf(TRUE, "a", "b"), "ab")
    shouldBombMatching(failIf(TRUE, list("a", "b")), "a, b")
}

testCheckFileExists <- function() {
    dir <- createTempDir("foo")
    shouldBombMatching(
        checkFileExists(dir, "bar"), 
        squish(":files do not exist: bar<CR>in ", dir)
    )
    file.create(squish(dir, "/bar"))
    file.create(squish(dir, "/baz"))
    checkFileExists(dir, "bar")
    checkFileExists(dir, c("bar", "baz"))
    shouldBombMatching(
        checkFileExists(dir, c("bar", "baz", "bash", "yahoo")),
        squish(":files do not exist: bash, yahoo<CR>in ", dir) 
    )
    on.exit(function() recreateSessionTempDir())
}


testCheckLength <- function() {
    a <- 1:3
    checkLength(a, 3)
    shouldBombMatching(checkLength(a, 4), "expected.*4.*got 3")

    shouldBombMatching(checkLength(NULL, 0), "can't check length of NULL")
    shouldBombMatching(checkLength(NULL, 1), "can't check length of NULL")
    shouldBombMatching(checkLength(a, NULL), "expectedLength is NULL")
    shouldBombMatching(checkLength(5, c(1,2,3)), "the only element of ")

    checkLength(character(0), 0)

    checkLength(list(1,2,3), 3)
}

testCheckNull <- function() {
    checkNull(NULL)
    shouldBomb(checkNull("a"))
}

testRequireAllMatchFirst <- function() {
    requireAllMatchFirst(sum, list(c(5, 5), c(2, 8), c(3, 3, 4)), "sums must match")
    shouldBomb(requireAllMatchFirst(sum, list(c(5, 5), c(2, 8), c(3, 4)), "sums must match"))

    requireAllMatchFirst(names, list(c(a = 5, b = 5), c(a = 2, b = 8), c(a = 3, b = 3)), "names must match")
    shouldBomb(requireAllMatchFirst(names, list(c(a = 5, b = 5), c(a = 2, b = 8), c(a = 3, b = 3, c = 1)), "names must match"))
}

testGetopt <- function() {
    arg.list <- getopt("ab:c", c())
    checkTrue(!arg.list$a)
    checkTrue(is.null(arg.list$b))
    checkTrue(!arg.list$c)

    arg.list <- getopt("ab:c", c("-c"))
    checkTrue(!arg.list$a)
    checkTrue(is.null(arg.list$b))
    checkTrue(arg.list$c)

    arg.list <- getopt("ab:c", c("-c", "-b", "test", "-a"))
    checkTrue(arg.list$a)
    checkEquals(arg.list$b, "test")
    checkTrue(arg.list$c)

    arg.list <- getopt("ab:c", c("-c", "-e", "-b", "test", "-a"))
    checkTrue(!arg.list$a)
    checkTrue(is.null(arg.list$b))
    checkTrue(arg.list$c)
    checkEquals(arg.list$.argv, c("-e", "-b", "test", "-a"))

    shouldBomb(getopt(":abc", c("-c", "-e", "-b", "test", "-a")))
    shouldBomb(getopt("1ab", c("-c", "-e", "-b", "test", "-a")))
}

testCheckSameErrorForThingsThatLookSimilar <- function() { 
    a <- 1:5
    b <- 1:5
    attributes(a) <- list(x = 1, y = 2)
    attributes(b) <- list(x = 1, y = 2)
    checkSame(a, b)

    attributes(a) <- list(a = 1, y = 2)
    checkSameLooking(a, b)
    shouldBombMatching(checkSame(a, b), "a, y did not match attribute names:x, y")

    attributes(a) <- list(y = 2, x = 1)
    checkSame(a, b)

    attributes(a) <- list(x = 2, y = 2)
    checkSameLooking(a, b)
    shouldBombMatching(checkSame(a, b), "attr x = 2 did not match attr x = 1")
}

testCheckSameErrorMessage <- function() {
    shouldBombMatching(checkSame(1, 2), "1 did not match 2")
    shouldBombMatching(checkSame("a", "b"), "a did not match b")
    shouldBombMatching(checkSame(1:3, c(1:2,4)), "1, 2, 3<CR>\tdid not match<CR>1, 2, 4")
    shouldBombMatching(checkSame(letters[1:3], letters[c(1:2,4)]), 'a, b, c<CR>\tdid not match<CR>a, b, d')
    shouldBombMatching(checkSame(list(1,2,3), list(1,2,4)), ':(1, 2, 3)<CR>\tdid not match<CR>(1, 2, 4)')
    shouldBombMatching(checkSame(
        list("a", "b", "c"), list("a", "b", "d")
    ), ':(a, b, c)<CR>\tdid not match<CR>(a, b, d)')
    a <- SimpleTestClass(1)
    b <- SimpleTestClass(2)
    c <- SimpleTestClass(1, "a")
    checkSame(as.character(a), "STC1")
    shouldBombMatching(checkSame(a, b), "STC1 did not match STC2")
    shouldBombMatching(checkSame(a, c), "STC1 did not match STC1a")
    checkSame(a, a)
    shouldBombMatching(checkSame(list(a, a), list(a, b)), ":(STC1, STC1)<CR>\tdid not match<CR>(STC1, STC2)")
    dateA <- as.POSIXct("2007/12/07")
    dateB <- as.POSIXct("2007/12/08 15:00:00")
    shouldBombMatching(checkSame(dateA, dateB), "2007-12-07 did not match 2007-12-08 15:00:00")
    shouldBombMatching(checkSame(
        rep("a", 5), rep("a", 6)
    ), "<CR>a, a, a, a, a<CR>\tdid not match<CR>a, a, a, a, a, a<CR><CR>")

    a <- 1:4
    b <- a
    names(a) <- letters[a]
    shouldBombMatching(
        checkSame(a, b), 
        "a, b, c, d<CR>1, 2, 3, 4<CR>\tdid not match<CR>1, 2, 3, 4"
    )
    names(b) <- letters[b+4]
    shouldBombMatching(
        checkSame(a, b), 
        "a, b, c, d<CR>1, 2, 3, 4<CR>\tdid not match<CR>e, f, g, h<CR>1, 2, 3, 4"
    )

    shouldBombMatching(
        checkSame('with trailing space ', 'with trailing space'),
        ':"with trailing space " did not match with trailing space'
    )
}

testJoin <- function() { 
    checkSame("ab", join("", c("a", "b")))
    checkSame("a|b", join("|", list("a", "b")))
    checkSame("a,,b,,", join(",,", list("a", "b", "")))
    checkSame("", join("", c()))
    checkSame("", join(",", c()))
    checkSame("", join(":||:", c(NULL, NULL)))
}

testSquish <- function() {
    a <- letters[1:5]
    checkEquals("abcde", squish(a))
    checkEquals("", squish(NULL))
    checkEquals("abcdeabcde", squish(c(a, a)))
    a <- t(a)
    checkEquals("abcde", squish(a))
    checkEquals("abcdeabcde", squish(a, t(a)))
    checkEquals("asdf", squish("a", "s", "d", "f"))
    checkEquals("asdf", squish(c("a", "s", "d", "f")))
    checkEquals("12345678910", squish(1:10))
    checkEquals("1234567891034", squish(1:10, 3:4))
    checkEquals("1234567891034", squish(c(1:10, 3:4)))
    checkEquals("1234567891034", squish(1:10, list(3,4)))
    checkEquals("12345678910ab34", squish(1:10, "a", "b", list(3,4)))
    checkEquals("12345678910ab3412", squish(1:10, "a", "b", list(list(3,4), list(1,2))))
}

testCheckShape <- function() {
    obj <- array(1:8, c(2,4), list(c("row1", "row2"), c("col1", "col2", "col3", "col4")))

    checkShape(obj, rows = 2, cols = 4)
    checkShape(obj, rows = 2)
    checkShape(obj, cols = 4)
    checkShape(obj, rownames = c("row1", "row2"), colnames = c("col1", "col2", "col3", "col4"))
    checkShape(obj, rownames = c("row1", "row2"))
    checkShape(obj, rownames = c("row1", "row2"), cols = 4)
    checkShape(obj, colnames = c("col1", "col2", "col3", "col4"))
    checkShape(obj, rows = 2, colnames = c("col1", "col2", "col3", "col4"))
    shouldBomb(checkShape(obj, rows = 1))
    shouldBomb(checkShape(obj, cols = 3))
    shouldBomb(checkShape(obj, rownames = c("a", "b")))
    shouldBomb(checkShape(obj, colnames = c("a", "b", "c", "d")))
    shouldBomb(checkShape(obj, rownames = c("a")))
    shouldBomb(checkShape(obj, colnames = c("col1")))
    shouldBomb(checkShape(obj, colnames = c("row1", "row2")))
    shouldBomb(checkShape(obj, rownames = c("col1", "col2", "col3", "col4")))
    shouldBomb(checkShape(obj))

    obj <- zoo(obj, order.by = c(3,7))
    checkShape(obj, rows = 2, cols = 4)
    checkShape(obj, index = c(3,7), colnames = c("col1", "col2", "col3", "col4"))
    checkShape(obj, index = c(3,7))
    shouldBomb(checkShape(obj, index = c(3)))
    shouldBomb(checkShape(obj, index = c("a", "c")))
    shouldBomb(checkShape(obj, index = c()))

    obj <- array(character(0), c(0,0))
    checkShape(obj, rows = 0, cols = 0)

    obj <- array(1:8, c(2,4))
    checkShape(obj, rows = 2, cols = 4)
    checkNull(rownames(obj))
    checkNull(colnames(obj))
    shouldBomb(checkShape(obj, rownames = c("a", "b")))
    shouldBomb(checkShape(obj, colnames = c("a", "b", "c", "d")))


}

testCheckSame <- function() {
    checkSame(1, 1)
    shouldBomb(checkSame(1, 0))
}

testCheckInherits <- function() {
    obj <- SimpleTestClass(123)
    checkInherits(obj, c("SimpleTestClass", "Object"))
    checkInherits(obj, "SimpleTestClass")
    shouldBomb(checkInherits(obj, c("SimpleTestClass", "data.frame")))
    shouldBomb(checkInherits(obj, "data.frame"))

    obj <- SimplerTestClass()
    checkInherits(obj, c("SimplerTestClass", "SimpleTestClass", "Object"))
    checkInherits(obj, c("SimplerTestClass", "Object"))
    checkInherits(obj, c("SimpleTestClass", "Object"))
    checkInherits(obj, "SimpleTestClass")
    checkInherits(obj, "SimplerTestClass")
    shouldBomb(checkInherits(obj, c("SimpleTestClass", "data.frame")))
    shouldBomb(checkInherits(obj, "data.frame"))

    checkInherits(data.frame(), "data.frame")
    checkInherits(list(), "list")
    checkInherits(TRUE, "logical")

    shouldBomb(checkInherits(data.frame(), "numeric"))
    shouldBomb(checkInherits(list(), "character"))
    shouldBomb(checkInherits(TRUE, "factor"))
}

testCheckAssertFalse <- function() {
    assertFalse(FALSE)
    shouldBomb(assertFalse(TRUE))

}

testNulls <- function() {
    checkTrue(all(nulls(NULL, NULL, NULL)))
    checkSame(nulls(NULL, 1, NULL), c(TRUE, FALSE, TRUE))
    checkFalse(any(nulls(1, 1, 1)))

}

testCheckMatches <- function() { 
    checkMatches("foo", "o$")
    checkMatches("foo", ":foo")
    checkMatches("foo", "!bar")
    shouldBombMatching(checkMatches("foo", "^o"), ":EXPECTED to match<CR>^o<CR>BUT GOT<CR>foo")
    shouldBombMatching(checkMatches("foo", "^o"), "!DEFORMED AS")
    shouldBombMatching(checkMatches("foo\nbar", "^o"), "DEFORMED AS<CR>foo<CR>bar<CR>")
}

testListify <- function() { 
    checkSame(list(5), listify(list(5)))
    checkSame(list(5), listify(5))
}

testCheckNotEquals <- function() {
    checkNotEquals(1, 2)
    checkNotEquals(NotSimple2(5), NotSimple2(6))
    shouldBomb(checkNotEquals(1, 1))
    shouldBomb(checkNotEquals(SimpleTestClass(5), SimpleTestClass(5)))
}

testAsLines <- function() {
    checkSame("1\n2\n3\n", asLines(1:3))
    checkSame("1\n", asLines(1))
    checkSame("", asLines(NULL))
}

testCommaSep <- function() {
    checkSame("1, 2, 3", commaSep(1:3))
    checkSame("1", commaSep(1))
    checkSame("", commaSep(NULL))
    checkSame("STC1, STC2", commaSep(list(SimpleTestClass(1), SimpleTestClass(2))))
}

testThe <- function() { 
    checkSame(5, the(zoo(1:5)[5]))
    checkSame(5, the(array(5, c(1, 2, 3))[1,1,1]))
    checkSame(5, the(array(5, c(1,1,1))))
    checkSame(5, the(list(5)))
    checkSame(5, the(c(5)))
    shouldBomb(the(zoo(1:2)))
    checkSame(5, the(zoo(5)))
    shouldBomb(the(array(c(5, 7), c(1, 2, 3))))
}

testBombMissing <- function() { 
    checkSame(5, bombMissing(5))
    checkSame(1:3, bombMissing(1:3))
    shouldBomb(bombMissing(NULL))
    shouldBomb(bombMissing(NA))
    shouldBomb(bombMissing(list(1, 2, NULL)))
    checkSame(list(1, 2, 3), bombMissing(list(1, 2, 3)))
    shouldBomb(bombMissing(list(1, 2, NA)))
    checkSame(array(1:4, c(2, 2)), bombMissing(array(1:4, c(2, 2))))
    shouldBomb(bombMissing(array(c(1:4, NA, NA), c(2, 3))))

}

testCheckSameSet <- function() {
    checkSameSet(c("a", "c", "b"), c("c", "b", "a"))
    shouldBomb(checkSameSet(c("a"), c("a", "b", "c")))
    shouldBomb(checkSameSet(NULL, 1))
    shouldBomb(checkSameSet(1, NULL))
}

testCheckSameSetWorksMultiValues <- function() {
    DEACTIVATED("not implemented yet")
    shouldBomb(checkSameSet(c(1,1,2), c(2,2,1)))
}

failing.test <- function() {
    the(1:3)
}

testShouldBombWithSpecificErrorMessage <- function() { 
    shouldBombMatching(the(1:3), "the only element of 3")
    shouldBombMatching(failing.test(), "the only element")
    shouldBombMatching(shouldBombMatching(the(1:3), "the only element of 4"), "incorrect error message")
    shouldBomb(shouldBombMatching(checkSame(5, 5), "doesntmatter"))
    shouldBombMatching(throw("this\nis\nan error"), "this.*error")
    shouldBombMatching(throw("this\nis\nan error"), "this<CR>is<CR>an error")
    invalidRegex <- "_()*_)(**&^*%" 
    shouldBombMatching(
        shouldBombMatching(throw(invalidRegex), invalidRegex),
        "invalid regular expression"
    )
    shouldBombMatching(throw(invalidRegex), squish(":", invalidRegex))
    shouldBombMatching(
        shouldBombMatching(throw(invalidRegex), squish(":!", invalidRegex)),
        "NOT LITERALLY.*"
    )
    shouldBombMatching(
        shouldBombMatching(throw(invalidRegex), squish("!:", invalidRegex)),
        "NOT LITERALLY.*"
    )
}

testMatches <- function() { 
    checkTrue(matches("foo", "foobar"))
    checkTrue(matches("^foo", "foobar"))
    checkTrue(matches("^foo", "foobarfoo"))
    checkTrue(matches("foo$", "foobarfoo"))
    checkTrue(matches("^:", ":foobarfoo"))
    checkTrue(matches("foo", "foobarfoo"))
    checkFalse(matches("^foo$", "foobarfoo"))
    checkTrue(matches("", "asd"))
    checkTrue(matches("^\\s", " asd"))
    checkTrue(matches("\\s$", "asd "))
    checkTrue(matches("\\s$", "asd\t"))
    checkFalse(matches("\\s$", "asd"))
    checkFalse(matches("bar", "foo"))
    shouldBomb(matches(c("a", "b"), "anything"))  # too many patterns
    shouldBomb(matches("anything", c("a", "b")))  # too many targets
    shouldBomb(matches(NULL, "a"))  # no pattern
    shouldBomb(matches("a", NULL))  # no target
    shouldBomb()  # no target
    shouldBombMatching(matches(
        "())(*&*(%*&^$*&^%*&^%*&^%a", "())(*&*(%*&^$*&^%*&^%*&^%a"
    ), "invalid regular expression")
    checkTrue(matches("())(*&*(%*&^$*&^%*&^%*&^%a", "())(*&*(%*&^$*&^%*&^%*&^%a", TRUE))
}

testRejectDuplicates <- function() { 
    rejectDuplicates(c(1,2,3,4))
    shouldBombMatching(rejectDuplicates(c(1,2,3,2)), "duplicate item: 2<CR>")
    rejectDuplicates(array(1:4, c(2, 2)))
    rejectDuplicates(integer(0))
    rejectDuplicates(list(1,2,3,4))
    shouldBombMatching(rejectDuplicates(list(1,2,2,4)), "duplicate item: 2<CR>")
}


testIfElseDoesExpectedBehaviour <- function() { 
    checkSame(ifElse(TRUE, 1, 0), 1)
    checkSame(ifElse(FALSE, 1, 0), 0)

    shouldBombMatching(ifElse(c(T,T), NULL, 1), "expected length of isYes was 1 but got 2")
    shouldBombMatching(ifElse("TRUE", 1, 0), "isYes is not logical is character")

    
}
