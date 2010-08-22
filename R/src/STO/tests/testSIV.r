library("STO")

testSIV <- function() {
    siv <- SIV("foo", "bar", "1")
    checkTrue(inherits(siv, "SIV"))

    shouldBomb(SIV())
}

testSIVConstructFromFilename <- function() {
    target.sivs <- list(SIV("foo", "daily", "1"), SIV("foo", "daily", "2"))
    sivs <- SIV$constructFromFilename(c("foo_1_daily", "foo_2_daily"))
    checkEquals(sivs, target.sivs)
}

testToCode <- function() {
    siv <- SIV("foo", "bar", "1")
    checkSame(siv, eval(parse(text=siv$toCode())))
}
