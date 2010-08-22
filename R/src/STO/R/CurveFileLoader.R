constructor("CurveFileLoader", function(filename = NULL) {
    this <- extend(RObject(), "CurveFileLoader", .filename = filename, .binary = NULL)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, filename="character")
    failUnless(file.exists(filename), "file does not exist", filename)
    this$.binary <- matches("bin$", filename)
    this
})

method("curveZoo", "CurveFileLoader", function(this, ...) {
    if(this$.binary)
        return(CurveFileLoader$readBin(this$.filename))
    CurveFileLoader$readCSV(this$.filename)
})

method("curveName", "CurveFileLoader", function(this, ...) {
    this$.filename
})

method("readBin", "CurveFileLoader", function(static, file, ...) {
    input.data <- readBin(file, "double", file.info(file)$size / 8)
    assert(length(input.data) %% 3 == 0)
    nrows <- length(input.data) / 3

    input.array <- t(array(input.data, c(3, nrows)))
    colnames(input.array) <- c("date", "pnl", "position")
    dates <- input.array[, "date"]
    class(dates) <- c("POSIXt", "POSIXct")
    zoo(input.array[, c("pnl", "position")], order.by = dates)
})

method("readCSV", "CurveFileLoader", function(static, file, ...) {
    input.df <- read.csv(file, header=TRUE, colClasses=c("POSIXct", "numeric", "numeric"), stringsAsFactors=FALSE)
    colnames(input.df) <- c("date", "pnl", "position")
    zoo(input.df[, c("pnl", "position")], order.by=input.df[["date"]])
})
