library(GSFCore)
library(JavaUtils)

testJApply <- function() {
    l <- JObjects$list_by_ObjectArray(jDoubles(seq(1,5,1)))
    squares <- japply(l$iterator(), JDouble(), function(d) { d$doubleValue() ^ 2 })
    checkSame(squares, list(1,4,9,16,25))  
}

atestLongJApply <- function() {
    print(system.time(l <- JObjects$list_by_ObjectArray(jDoubles(seq(1,1000,1)))))
    print(system.time(squares <- japply(l$iterator(), JDouble(), function(d) { d$doubleValue() ^ 2 })))
}

atestLongControl <- function() {
    lapply(seq(1,1000,1), function(d) {d^2})
}