library(STO)

testFilter <- function() {
    filter <- RunFilter$with("foo", c(1,4,7,9,12))
    which <- filter$where(c(1,3,4,2,6,7,5,12,8,11,9))
    checkSame(which, c(1,3,6,11,8))

    shouldBombMatching(filter$where(c(1,4,7,9)), "must be a super set")
}

testFilterAnd <- function() {
    filterFoo <- RunFilter$with("foo", c(1,4,7,9,12))
    filterBar <- RunFilter$with("bar", c(1,2,3,4,5))
    filter <- filterFoo$and(filterBar)
    checkSame(filter$name(), "foo & bar")
    checkSame(filter$runs(), c(1,4))
}

testFilterCross <- function() {
    filterList1 <- list(RunFilter$with("a1", c(1,2,3,4)), RunFilter$with("b1", c(5,6,7,8)))
    filterList2 <- list(RunFilter$with("a2", c(1,3,5,7)), RunFilter$with("b2", c(2,4,6,8)))
    filters <- RunFilter$cross(list(filterList1, filterList2))
    checkLength(filters, 4)
    checkSame(first(filters)$name(), "a1 & a2")
    checkSame(first(filters)$runs(), c(1,3))
    checkSame(last(filters)$name(), "b1 & b2")
    checkSame(last(filters)$runs(), c(6,8))
}
