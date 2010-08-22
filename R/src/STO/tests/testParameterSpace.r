library("STO")

testParameterSpace <- function()
{
    params <- ParameterSpace()
    checkInherits(params, c("ParameterSpace", "RObject"))

    params <- ParameterSpace(param1 = c(1,3,1), param2 = c(20,40,5))
    checkSame(3, params$colCount())
    checkSame(15, params$rowCount())
    checkSame(1:15, params$runs())

    target <- data.frame(run = 1:15, param1 = rep(1:3, 5), param2 = sort(rep(seq(20,40,5),3)))
    checkSame(target, params$data())

    params <- ParameterSpace(test = c(1,3,1), tf = c(TRUE, FALSE, NA))
    checkSame(3, params$colCount())
    checkSame(6, params$rowCount())

    params <- ParameterSpace(test = c(1,3,1), tf = c(FALSE, TRUE, NA))
    checkSame(3, params$colCount())
    checkSame(6, params$rowCount())

    params <- ParameterSpace(test = c(1,3,1), tf = c(FALSE, FALSE, NA))
    checkSame(3, params$colCount())
    checkSame(3, params$rowCount())

    params <- ParameterSpace(test = c(1,3,1), tf = c(TRUE, TRUE, NA))
    checkSame(3, params$colCount())
    checkSame(3, params$rowCount())

    params <- ParameterSpace(
        BreakDays = c(10, 60, 1), 
        ATRLen = c(5, 15, 1),
        Risk = c(0.01, 0.05, 0.01),
        nATR = c(1, 2, 1),
        upATR = c(1, 1, 1),
        MaxPyramid = c(1, 1, 1),
        FirstDayATR = c(0.5, 0.5, 1),
        ATRlong = c(100, 100, 100),
        InitEquity = c(100000000, 100000000, 0),
        FixEquity = c(TRUE, TRUE, NA)
    )

    checkSame(
        params$colNames(), 
        c(
            "run", "BreakDays", "ATRLen", "Risk", 
            "nATR", "upATR", "MaxPyramid", "FirstDayATR", 
            "ATRlong", "InitEquity", "FixEquity"
        )
    )

    checkSame(11, params$colCount())
    checkSame(51*11*5*2, params$rowCount())

    params <- ParameterSpace(test = c(1,3,1), tf = c(TRUE, FALSE, NA))

    params$writeCSV(file = textConnection("testData", open = "w", local = TRUE))  
    checkSame(testData, c('"run","test","tf"','1,1,TRUE','2,2,TRUE','3,3,TRUE','4,1,FALSE','5,2,FALSE','6,3,FALSE'))
    params.reread = ParameterSpace$readCSV(file = textConnection(testData, open="r"))
    checkSame(params, params.reread)

}

testParameterSpaceDefinition <- function() {
    params <- ParameterSpace(param1 = c(1,3,1), param2 = c(20,40,5), param3 = c(TRUE, FALSE, NA))
    checkSame(params$definition(), list(param1 = c(1,3,1,3), param2 = c(20,40,5,5), param3 = c(TRUE, FALSE, NA, 2)))
    params$writeCSV(file = textConnection("testData", open = "w", local = TRUE))
    params.reread = ParameterSpace$readCSV(file = textConnection(testData, open="r"))
    checkSame(params$definition(), params.reread$definition())
    params <- ParameterSpace(param1 = c(1,3,1), param2 = c(20,40,5), param3 = c(TRUE, TRUE, NA))
    checkSame(params$definition(), list(param1 = c(1,3,1,3), param2 = c(20,40,5,5), param3 = c(TRUE, TRUE, NA, 1)))
    params <- ParameterSpace(param1 = c(1,3,1), param2 = c(20,20,0))
    checkSame(params$definition(), list(param1 = c(1,3,1,3), param2 = c(20,20,0,1)))
}

testToCode <- function() {
    params <- ParameterSpace(param1 = c(1,3,1), param2 = c(20,40,5), param3 = c(TRUE, FALSE, NA))
    code <- params$toCode()
    checkSame(params, eval(parse(text=code)))
}

testSubSetReturnsCorrectShape <- function() { 
    params <- ParameterSpace(param1 = c(1,3,1), param2 = c(20,40,5))
    params <- params$subSet(1:3, c("param1"))
    checkSame(2, params$colCount()) # have to keep the "run" column
    checkSame(1:3, params$runs())
}

testUniqueValues <- function() {
    params <- ParameterSpace(param1 = c(1,3,1), param2 = c(20,40,5))
    checkSame(params$uniqueValues("param1"), c(1,2,3))
    checkSame(params$uniqueValues("param2"), c(20,25,30,35,40))
}

testFilter <- function() {
    params <- ParameterSpace(param1 = c(1,3,1), param2 = c(20,40,5))
    filter <- params$filter(param1 == 3)
    checkSame(1:5 * 3, filter$runs())
    checkSame("param1 == 3", filter$name())
    filter <- params$filter(param1 > 2 & param2 > 35)
    checkSame(15, filter$runs())
    checkSame("param1 > 2 & param2 > 35", filter$name())
}

testFiltersExact <- function() {
    params <- ParameterSpace(param1 = c(1,10,1), param2 = c(20,40,5), param3 = c(-2,2,0.5))
    filters <- params$filtersExact(param1 = c(1,5,8), param3 = c(-1.5, 1.5))
    checkLength(filters, 6)
    checkSame(first(filters), params$filter(param1 == 1 & param3 == -1.5))
    checkSame(fourth(filters), params$filter(param1 == 1 & param3 == 1.5))
    checkSame(last(filters), params$filter(param1 == 8 & param3 == 1.5))
    shouldBombMatching(params$filtersExact(param1 = c(0,1,2)), "param1 == 0")
}

testFiltersAll <- function() {
    params <- ParameterSpace(param1 = c(1,10,1), param2 = c(20,40,5), param3 = c(-2,2,0.5))
    filters <- params$filtersAll("param1")
    checkLength(filters, 10)
    checkSame(first(filters), params$filter(param1 == 1))
    checkSame(fourth(filters), params$filter(param1 == 4))
    checkSame(last(filters), params$filter(param1 == 10))
    checkSame(params$filtersAll("param1", "param2"), RunFilter$cross(list(params$filtersAll("param1"), params$filtersAll("param2"))))
}

atestFiltersRange <- function() {
    params <- ParameterSpace(param1 = c(1,10,1), param2 = c(20,40,5), param3 = c(-2,2,0.5))
    filters <- params$filtersRange(param1 = c(1,4,7,10), param3 = c(-2,2,1))
    checkLength(filters, 15)
    checkSame(first(filters), params$filter(param1 >= 1 & param1 < 4 & param3 >= -2 & param3 < -1))
    checkSame(last(filters), params$filter(param1 >= 7 & param1 < 10 & param3 >= 1 & param3 < 2))


}
