library("STO")

source(system.file("testHelper.r", package = "STO"))

siv <- SIV("foo", "bar", "1")

testMSIVWithListResult <- function() {
    msiv <- siv$m(c("baz", "blech"))
    checkEquals(length(msiv), 2, "msiv not length 2!")
    checkTrue(inherits(msiv[[1]], "MSIV"), "msiv[1] is not an MSIV")
}

testMSIVWithMultiArgs <- function() {
    msiv <- siv$m("baz", "blech")
    checkEquals(length(msiv), 2, "msiv not length 2!")
    checkTrue(inherits(msiv[[1]], "MSIV"), "msiv[1] is not an MSIV")
}

testMSIVWithSingleResult <- function() {
    msiv <- siv$m("baz")
    checkTrue(inherits(msiv[[1]], "MSIV"))
}

testMSIVWithNullArgumentBombs <- function() {
    DEACTIVATED("temporarily had to make arguments optional")
    shouldBomb(MSIV())
}

testMSIVWithMisOrderedArgumentsBombs <- function() { 
    shouldBomb(MSIV('market', siv))
}

testMSIVFileName <- function() {
    msiv <- SIV("foo", "daily", "1")$m("baz")[[1]]
    fileName <- "foo_1_daily_baz.csv"
    checkEquals(fileName, msiv$fileName(".csv"))
    checkEquals("foo_1_daily_baz", msiv$fileName())
}

testMSIVConstructFromFilename <- function() {
    target.msivs <- unlist(list(SIV("foo", "daily", "1")$m(c("baz", "bar")), SIV("foo", "daily", "2")$m("blech")), recursive = FALSE)
    msivs <- MSIV$constructFromFilename(c("foo_1_daily_baz", "foo_1_daily_bar", "foo_2_daily_blech"))
    checkEquals(msivs, target.msivs)
}

testMSIVConstructFromFilenameExtras <- function() {
    target.msivs <- unlist(list(SIV("foo", "daily", "1")$m(c("baz", "bar")), SIV("foo", "daily", "2")$m("blech")), recursive = FALSE)
    msivs <- MSIV$constructFromFilename(c("foo_1_daily_baz.csv", "foo_1_daily_bar.csv", "foo_2_daily_blech.csv"), extra=".csv")
    checkSame(msivs, target.msivs)
}

testMSIVConstructFromFilenameDoesNotEndWithnExtrasBombs <- function() {
    shouldBomb(MSIV$constructFromFilename(c("foo_1_daily_baz.csv", "foo_1_daily_bar.csv", "foo_2_daily_blech"), extra=".csv"))
    shouldBomb(MSIV$constructFromFilename(c("foo_1_daily_baz.csv", "foo_1_daily_bar.csv", "foo_2_daily_blech.xls"), extra=".csv"))
    shouldBomb(MSIV$constructFromFilename(c("foo_1_daily_baz.csv", "foo_1_daily_bar.csv", "foo_2_daily_.csvblech"), extra=".csv"))
}

testMSIVFromDir <- function() {
    checkSame(MSIV$fromDir(dataDir("SimpleMetrics"), ".csv"), simpleMsivs)
}

testMSIVAsCharacter <- function() {
    msiv <- SIV("foo", "daily", "1")$m("baz")[[1]]
    checkEquals("foo_1_daily_baz", as.character(msiv))
}

testMSIVIndicesOfAInB <- function() {
    msivs <- siv$m(c("a", "b", "c"))
    checkSame(c(2), MSIV$indicesOfAInB(MSIV(siv, "b"), msivs))
    checkSame(c(1,3), MSIV$indicesOfAInB(siv$m("a", "c"), msivs))
    checkSame(c(3,1), MSIV$indicesOfAInB(siv$m("c", "a"), msivs))
    shouldBomb(MSIV$indicesOfAInB(siv$m("d", "a"), msivs))

}

testMSIVHasRawData <- function() {
    msiv <- the(siv$m(c("a")))
    checkTrue(msiv$hasRawData())
}

testToCode <- function() {
    msiv <- MSIV(siv, "baz")
    checkSame(msiv, eval(parse(text=msiv$toCode())))
}



