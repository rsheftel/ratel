library("GSFCore")

wasOff <- Progress$isOff()
testEnv <- environment()

.setUp <- function() {
    Progress$on() 
}

.tearDown <- function() {
    if (wasOff) Progress$off() else Progress$on()
}

testWritesDots <- function() { 
    checkOutput(".", {
        Progress$start('.')
        progress('.')
    })
}

testProgressOnOff <- function() {
    Progress$start('.')
    checkOutput(".", progress('.'))
    Progress$off()
    checkOutput("", progress('.'))
    Progress$on()
    checkOutput(".", progress('.'))
    Sys.setenv(NO_PROGRESS_DOTS=1)
    checkOutput("", progress('.'))
    Progress$on()
}

testWritesMultipleDotsUsingEvery <- function() { 
    checkOutput("....", { Progress$start('.', 10); for(i in 1:40) progress('.') })
}

testWritesMultipleDots <- function() { 
    checkOutput("....", { Progress$start('.'); for(i in 1:4) progress('.') })
    checkOutput("///", { Progress$start('/'); for(i in 1:3) progress('/') })
    shouldBombMatching(Progress$start("AB"), "symbol must be one")
}

testWritesNumbersOnEveryTimesTen <- function() { 
    checkOutput("*********1*********2*", {
        Progress$start('*')
        for(i in 1:21) progress('*')
    })
}


