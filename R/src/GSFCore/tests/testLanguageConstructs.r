
library("GSFCore")


testIfElseBehavior <- function() {
    checkSame(1, ifelse(TRUE, 1, 0))
    checkSame(1, ifelse(1 == 1, 1, 0))
    checkSame(c(1,1,1), ifelse(1:3 == 1:3, 1, 0))
    checkSame(1, ifelse(all(1:3 == 1:3), 1, 0))
    checkSame(c(0,1,0), ifelse(1:3 == c(3,2,1), 1, 0))
    shouldBombMatching(
        ifelse(list(1:3) == list(c(3,2,1)), 1, 0), 
        "comparison of these types is not implemented"
    )
    checkSame(c("lkj", "lkj", "lkj"), ifelse(1:3 == 2:4, "asdf", "lkj"))
    checkSame(c("asdf", "asdf", "lkj"), ifelse(c(2:3,1) == 2:4, "asdf", "lkj"))
    checkSame("lkj", ifelse(is.null(1:3), "asdf", "lkj"))
    checkSame("asdf", ifelse(is.null(c(NULL, NULL)), "asdf", "lkj"))
    checkSame("lkj", ifelse(is.null(list(NULL, NULL)), "asdf", "lkj"))
    ifelse(is.null(NULL), { foo <- 7 }, { foo <- 9})
    checkSame(foo, 7)
    shouldBombMatching(
        ifelse(is.null("a"), function() { foo <- 19 }, function() { foo <- 21 })(),
        "object is not subsettable|What the hell causes this?"
    )
    shouldBombMatching(
        ifelse(is.null("a"), function() { 19 }, function() { 21 }),
        "object is not subsettable|What the hell causes this?"
    )
    # these are strange - ifelse takes a logical vector and 
    # returns elements from it's other two arguments based on the ifelse logical vector.
    checkSame(list("z"), ifelse(is.null("a"), list("a", "b"), list("z", "y")))
    checkSame(list("z"), ifelse(FALSE, list("a", "b"), list("z", "y")))
    checkSame(list("a", "y"), ifelse(c(TRUE, FALSE), list("a", "b"), list("z", "y")))

    # with replacement, i'm sure.
    logicals <- c(TRUE, FALSE, TRUE, TRUE)
    checkSame(c(1, 6, 3, 4), ifelse(logicals, 1:4, 5:8))
    checkSame(c(1, 6, 1, 1), ifelse(logicals, 1, 5:8))
}

testSomeAnonymousFunctionWierdness <- function() { 
    funcs <- list(function() { 19}, function() { 21 })
    checkSame(funcs[[1]](), 19)

    myIfElse <- function(isYes, yes, no) {
        if (isYes) return(yes)
        no
    }
    checkSame(30, myIfElse(TRUE, function() {30}, function() {31})())
    myIfElse <- ifelse
    shouldBomb(myIfElse(TRUE, function() {30}, function() {31})())
    checkSame(31, the(ifelse(FALSE, list(function() {30}), list(function() { 31})))())
    thirty <- function() { 30 }
    thirtyOne <- function() { 31 }
    shouldBomb(myIfElse(TRUE, thirty, thirtyOne), "this fails because of the implementation of ifelse")
    shouldBombMatching(rep(function() { }, 2), "object is not subsettable")
    funcs <- rep(list(function() { 37 }), 37)
    checkSame(first(funcs)(), 37)
    checkSame(first(funcs)(), last(funcs)())
    checkSame(first(funcs)(), funcs[[17]]())
}

