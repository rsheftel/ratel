library("STO")

siv <- SIV("foo", "daily", "1")
msivs <- siv$m(c("baz", "blech", "yelp"))

testPortfolioConstructor <- function() {
    port <- Portfolio("combo", msivs)
    checkInherits(port, "Portfolio")

    port <- Portfolio("combo", msivs, c(1,2,3))
    checkInherits(port, "Portfolio")
}

testPortfolioMismatchedLengths <- function() {
    shouldBomb(Portfolio("combo", msivs, c(1,2)))
}

testPortfolioHasRawData <- function() { 
    checkFalse(Portfolio("combo", msivs)$hasRawData())

}

testPortfolioPick <- function() {
    port <- Portfolio("combo", msivs)
    zoos <- list(
        zoo(c(1,2,3)),
        zoo(c(2,3,4)),
        zoo(c(3,4,5))
    )
    toBeScaled <- Map$from(msivs, zoos)
    getZoo <- function(msiv) toBeScaled$fetch(msiv)

    picked <- port$pick(getZoo, "zoo")
    checkIsType(picked, "Map(MSIV, zoo)")
    checkSame(toBeScaled, picked)

    oneAndThree <- Portfolio("combo", msivs[c(1,3)])
    picked <- oneAndThree$pick(getZoo, "zoo")
    expected <- toBeScaled$copy()
    expected$remove(msivs[[2]])
    checkSame(expected, picked)

    oneTwoThree <- Portfolio("combo", msivs, c(1,2,3))
    picked <- oneTwoThree$pick(getZoo, "zoo")
    checkSame(toBeScaled, picked)
}

testPortfolioScale <- function() {
    port <- Portfolio("combo", msivs)
    zoos <- list(
        zoo(c(1,2,3)),
        zoo(c(2,3,4)),
        zoo(c(3,4,5))
    )
    toBeScaled <- Map$from(msivs, zoos)
    getZoo <- function(msiv) toBeScaled$fetch(msiv)

    scaled.zoos <- port$scale(getZoo, "zoo")
    checkIsType(scaled.zoos, "Map(MSIV, zoo)")
    checkSame(toBeScaled, scaled.zoos)

    oneAndThree <- Portfolio("combo", msivs[c(1,3)])
    scaled.zoos <- oneAndThree$scale(getZoo, "zoo")
    expected <- toBeScaled$copy()
    expected$remove(msivs[[2]])
    checkSame(expected, scaled.zoos)

    oneTwoThree <- Portfolio("combo", msivs, c(1,2,3))
    scaled.zoos <- oneTwoThree$scale(getZoo, "zoo")
    expected <- list(
        zoo(c(1,2,3)),
        zoo(c(4,6,8)),
        zoo(c(9,12,15))
    )
    checkSame(Map$from(msivs, expected), scaled.zoos)
}

siv <- SIV("foo", "daily", "1")
msivs <- siv$m(c("baz", "blech", "yelp"))

testPortfolioWriteCSV <- function() {
    port <- Portfolio("combo", msivs, c(1,2,3))

    port$writeCSV(textConnection("testData", open = "w", local = TRUE))
    checkSame(testData, c('"foo_1_daily_baz",1', '"foo_1_daily_blech",2', '"foo_1_daily_yelp",3'))
    checkSame(Portfolio$constructFromFilename("combo", textConnection(testData)), port)
}

testPortfolioCannotContainItself <- function() {
    port <- Portfolio("combo", msivs, c(1,2,3))
    msivs <- appendSlowly(msivs, port)
    shouldBombMatching(Portfolio("combo", msivs), "cannot include a Portfolio in itself")
}

testToCode <- function() {
    port <- Portfolio("combo", msivs, c(1,2,3))
    checkSame(port, eval(parse(text=port$toCode())))
}
