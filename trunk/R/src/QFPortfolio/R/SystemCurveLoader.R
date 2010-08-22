constructor("SystemCurveLoader", function() {
    extend(RObject(), "SystemCurveLoader")
})

method("systemCurves", "SystemCurveLoader", function(static, dirName, fileName, range = NULL, ...) {
    needs(dirName="character", fileName="character", range="Range?")
    data <- read.csv(squish(dirName, "/", fileName), stringsAsFactors = FALSE)
    data$Version <- sub("#", "", as.character(data$Version))
    data <- subset(data, select=c("Market", "System", "Interval", "Version", "PV_Name"))

    systems <- unique(data[["System"]])
    sapply(systems, function(system) {
        msivPvs <- subset(data, System == system)
        filenames <- with(msivPvs, { paste(System, Version, Interval, PV_Name, Market, sep="_") })
        filenames <- paste(dirName, "/", filenames, ".csv", sep="")
        curves <- lapply(filenames, function(file) PositionEquityCurve(CurveFileLoader(file), range=range))
        WeightedCurves(curves)$curve()
    }, simplify = FALSE)
})
