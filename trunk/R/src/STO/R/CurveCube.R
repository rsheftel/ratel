constructor("CurveCube", function(dir = NULL, msivs = NULL, runs = NULL, interval = NULL, range = NULL) {
    this <- extend(RObject(), "CurveCube", 
        .dates = NULL, 
        .curves = NULL,
        .dir = dir,
        .interval = interval,
        .range = range
    )
    constructorNeeds(this, dir="character", msivs="list(MSIV)?", runs="numeric?", interval="Interval?", range="Range?")
    if(inStaticConstructor(this)) return(this)

    if(is.null(msivs))
        msivs <- MSIV$fromDir(dir)
    if(is.null(runs))
        runs <- this$.lookupRunNumbersFromFirstMSIV(msivs)
    this$.curves <- Array("MSIV", "numeric", "PositionEquityCurve", msivs, runs)
    this$.setDates()
    this$.curves$requireFull()
    this
})

method("dates", "CurveCube", function(this, ...) {
    this$.dates
})

method("curvesDir", "CurveCube", function(this, msiv, ...) {
    squish(this$.dir, "/", msiv$fileName())
})

method(".setDates", "CurveCube", function(this, ...) {
    msivs <- this$msivs()
    msivs <- msivs[sapply(msivs, function(msiv) isDirectory(this$curvesDir(msiv)))]
    assert(length(msivs) > 0, "no MSIVs with raw data in CurveCube with which to calculate dates")
    Progress$start('f')
    firstCurve <- function(msiv) { 
        progress('f'); 
        this$curve(msiv, first(this$runNumbers())) 
    }
    allFirstCurves <- lapply(msivs, firstCurve)
    curveWithAllDates <- do.call( merge.zoo, allFirstCurves)
    this$.dates <- index(curveWithAllDates)
})

method(".lookupRunNumbersFromFirstMSIV", "CurveCube", function(this, msivs, ...) {
    needs(msivs="list(MSIV)")
    for(msiv in msivs) {
        if (isDirectory(this$curvesDir(msiv))) {
            fileGlob <- squish("run_.*\\.bin")
            run.files <- list.files(this$curvesDir(msiv), fileGlob)
            return(as.numeric(gsub("\\D+", "", run.files, perl = TRUE)))
        }
    }
    assert(FALSE, "no MSIVs with raw data in CurveCube")
})

method("runNumbers", "CurveCube", function(this, ...) {
    this$.curves$colnames()
})

method("msivs", "CurveCube", function(this, ...) {
    this$.curves$rownames()
})


method("pmCurve", "CurveCube", function(this, pm, ...) {
    needs(pm="ParameterizedMsivs")
    getNormalZoo <- function(msiv, run) {
        normalizeIndices(this$curve(msiv, run), this$.dates)
    }
    scaledMarketCurves <- pm$scale(getNormalZoo)
    z <- sumZoos(scaledMarketCurves)
    z[, "position"] <- NA
    PositionEquityCurve(ZooCurveLoader(z, "pmCurve"))
})

method("addMarket", "CurveCube", function(this, new.msiv, ...) {
    needs(new.msiv="MSIV")

    this$.curves$addRow(new.msiv)
})

method("requireMSIVs", "CurveCube", function(this, msivs, ...) {
    thisMSIVs <- this$msivNames()
    thatMSIVs <- this$msivNames(msivs)
    assert(
        length(this$msivs()) == length(msivs), 
        squish("msivs do not match.\nthis: ", humanString(thisMSIVs), "\nthat: ", humanString(thatMSIVs))
    )
    assert(
        all(thatMSIVs %in% thisMSIVs),
        squish("msivs do not match.\nthis: ", humanString(thisMSIVs), "\nthat: ", humanString(thatMSIVs))
    )
})

method("msivNames", "CurveCube", function(this, msivs = this$msivs(), ...) {
    sapply(msivs, fileName)
})

method("requireMsiv", "CurveCube", function(this, msiv, ...) {
    failUnless(any(sapply(this$msivs(), function(m) isTRUE(all.equal(m, msiv)))), as.character(msiv), " not in cube.  Has:\n", commaSep(this$msivs()))
})

method("curve", "CurveCube", function(this, msiv, run, ...) {
    this$requireMsiv(msiv)
    curve <- this$.curves$fetch(msiv, run)
    if(!is.null(curve)) return(curve)
    loader <- CurveFileLoader(this$.curveFileName(msiv, run))
    curve <- PositionEquityCurve(loader, this$.interval, this$.range) 
    progress("X")
    this$.curves$set(msiv, run, curve)
})

method("filename", "CurveCube", function(static, dir, msiv, run, ...) {
    msiv.dir <- squish(dir, "/", msiv$fileName())
    filename <- squish(msiv.dir, "/run_", run, ".bin")
    failUnless(file.exists(filename), "file does not exist: ", filename)
    filename
})


method(".curveFileName", "CurveCube", function(this, msiv, run, ...) {
    CurveCube$filename(this$.dir, msiv, run)
})
method("runCurves", "CurveCube", function(this, msiv, ...) {
    runCurves <- this$.curves$fetchRow(msiv)
    if(!any(sapply(runCurves, is.null))) return(runCurves)
    Progress$start('c', every=10)
    result <- lapply(this$runNumbers(), function(run) {
        progress('c')
        loader <- CurveFileLoader(this$.curveFileName(msiv, run))
        PositionEquityCurve(loader, this$.interval, this$.range)
    })
    this$.curves$setRow(msiv, result)
    result
})

method("requireCurve", "CurveCube", function(this, msiv, run, expectedCurve, ...) {
    needs(msiv = "MSIV", run = "numeric", expectedCurve = "PositionEquityCurve") 
    msivNames = this$msivNames()
    assert(msiv$fileName() %in% msivNames, squish("expected ", msiv, " not found in ", paste(msivNames)))
    checkSame(this$curve(msiv, run), expectedCurve)
})

method("metrics", "CurveCube", function(this, ...) {
    MetricCube(this$msivs(), this$runNumbers(), this)
})

method(".downloadCommand", "CurveCube", function(static, systemId, runs, markets=NULL, skipExisting=FALSE, ...) {
    needs(systemId = "numeric", runs = "numeric|list(numeric)", markets="character?", skipExisting="logical")
    command <- squish('QRun Q.Trading.Results.CurveFiles -systemId ', systemId);
    command <- squish(command, ' -runs ', dQuote(join(",", as.character(runs))))
    command <- squish(command, ' -skipExisting ', skipExisting)
    if (!is.null(markets))
        command <- squish(command, ' -markets ', dQuote(join(',', markets)))
    command
})

# markets here are just the market names
method("download", "CurveCube", function(static, systemId, runs, markets=NULL, skipExisting=FALSE, ...) {
    needs(systemId = "numeric", runs = "numeric|list(numeric)", markets="character?", skipExisting="logical")
    runCmd(static$.downloadCommand(systemId, runs, markets, skipExisting))
})
