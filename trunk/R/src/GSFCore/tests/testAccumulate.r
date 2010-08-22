library("GSFCore")

testAccumulate <- function() {
    zoos <- list(zoo(1:3), zoo(2:4), zoo(3:5))
    expected.add <- zoo(c(6, 9, 12))
    checkSame(accumulate('+', zoos), expected.add)

    expected.product <- zoo(c(6, 24, 60))
    checkSame(accumulate('*', zoos), expected.product)
}

testAccumulateSimple <- function() {
    nums <- 1:10
    checkSame(accumulate('+', nums), sum(nums))

    checkSame(accumulate('*', nums), cumprod(nums)[[10]])
    checkSame(accumulate(squish, nums), "12345678910")
}
