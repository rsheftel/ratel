library("STO")


checkTicks <- function(min, max, result) {
    checkSame(calcTickMarks(c(min,max)), result)
}

testTickMarks <- function() {
    checkTicks(1, 10, c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
    checkTicks(1, 20, c(1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20))
    checkTicks(0.2, 0.8, c(0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8))
    checkTicks(-10, -1, c(-10, -9, -8, -7, -6, -5, -4, -3, -2, -1))
    checkTicks(-5700699.8, 5603419.954, 
        c(-5700699.8, -5e6, -4e6, -3e6, -2e6, -1e6, 0, 1e6, 2e6, 3e6, 4e6, 5e6, 5603419.954)
    )
    checkTicks(-49999, 49999, c(-49999, -4e4, -3e4, -2e4, -1e4, 0, 1e4, 2e4, 3e4, 4e4, 49999))
    checkTicks(49000, 50000, c(49000, 49100, 49200, 49300, 49400, 49500, 49600, 49700, 49800, 49900, 50000))
    checkTicks(0, 5, c(0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5))
}
