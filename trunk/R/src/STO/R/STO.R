constructor("STO", function(dir = NULL, id = NULL, calculateMetrics = TRUE) {
    this <- extend(RObject(), "STO", 
    	.baseDir = dir,
        .dir = squish(dir, "/", id), 
        .id = id, 
        .msivs = NULL, 
        .runs = NULL,
        .calculateMetrics = calculateMetrics,
        .hasValidMetrics = NULL
    )
    constructorNeeds(this, dir = "character", id = "character")
    if(inStaticConstructor(this)) return(this)

    requireDirectory(dir)
    assert(isDirectory(this$.dir), squish("invalid stoID: ", id))
    this$.hasValidMetrics = file.exists(this$.metricsDir()) && length(list.files(this$.metricsDir())) > 0
    this$.readMsivsAndRuns()
    this
})

method("id", "STO", function(this, ...) {
    this$.id
})

method("baseDir", "STO", function(this, ...) {
    this$.baseDir
})

method("create", "STO", function(class, dir, id, msivs, ...) {
    needs(dir = "character", id = "character", msivs = "list(MSIV)")

    assert(length(msivs) > 0, "no msivs")
    requireDirectory(dir)

    dirname <- squish(dir, "/", id)
    assertFalse(file.exists(dirname), squish("sto ", id, " already exists.  use constructor."))
    dir.create(dirname)
    dir.create(squish(dirname, "/CurvesBin"))
    dir.create(squish(dirname, "/Metrics"))
    dir.create(squish(dirname, "/Trades"))
    dir.create(squish(dirname, "/Workspaces"))

    class$.writeMsivs(msivs, class$.msivFilename(dirname))
    sto <- STO(dir, id)
    sto
})

method("add", "STO", function(this, portfolios, noCombine=FALSE,...) {
    needs(portfolios="Portfolio|list(Portfolio)", noCombine="logical")
    portfolios <- listify(portfolios)
    this$.msivs <- c(this$.msivs, portfolios)
    dirname <- this$dirname("Portfolios")
    dir.create(dirname)
    for (port in portfolios)
        port$writeCSV(squish(dirname, "/", port$market()))
    if(!noCombine) {
        message("Combining consituent equity curves to create portfolio curves...\n")
        this$combine(port)
    }
})

method("addMsivs", "STO", function(this, msivs, ...) {
    msivs <- STO$rawMsivList(msivs)
    hasDuplicates <- any(strings(msivs) %in% strings(this$.msivs))
    failIf(hasDuplicates, "cannot add duplicates to a STO.  Inserting:\n", msivs, "\nHave:\n", this$.msivs)
    this$.msivs <- c(this$.msivs, msivs)
    this$.writeMsivs(this$.msivs, STO$.msivFilename(this$.dir))
})

method("rawMsivList", "STO", function(class, msivs, ...) {
    needs(msivs="MSIV|list(MSIV)")
    msivs <- listify(msivs)
    failIf(any(sapply(msivs, function(m) inherits(m, "Portfolio"))), "addMsivs() does not support adding Portfolios.  Use add() instead.\n", msivs)
    msivs
})

method("msivs", "STO", function(this, ...) {
    this$.msivs
})

method(".msivFilename", "STO", function(class, dir, ...) {
    squish(dir, "/", "MSIVs.csv")
})

method(".writeMsivs", "STO", function(class, msivs, filename, ...) {
    msivNames <- sapply(msivs, as.character)
    write.table(msivNames, filename)
    
})

method(".readMsivsAndRuns", "STO", function(this, ...) {
    this$.readMsivs()   
    this$.readRuns(first(this$.msivs))

    portfolioDir <- this$dirname("Portfolios")
    if(isDirectory(portfolioDir)) {
        files <- list.files(portfolioDir)
        ports <- lapply(files, function(file) Portfolio$constructFromFilename(file, squish(portfolioDir, "/", file)))
        this$.msivs <- c(this$.msivs, ports)
    }
})

method(".readMsivs", "STO", function(this, ...) {
    msivFile <- STO$.msivFilename(this$.dir)
    if (file.exists(msivFile)) { 
        df <- read.table(msivFile, stringsAsFactors=FALSE)
        this$.msivs <- MSIV$constructFromFilename(column(df, 1))
    } else { 
        if(this$.hasValidMetrics)
            this$.msivs <- MSIV$fromDir(this$.metricsDir(), extra = ".csv")
        else 
            this$.msivs <- MSIV$fromDir(this$.curveDir())
        this$.writeMsivs(this$.msivs, msivFile)
    }
})

method(".readRuns", "STO", function(this, msiv, ...) {
    needs(msiv="MSIV")
    if (this$.hasValidMetrics) {
        metricsDf <- MetricCube$metricsDataFrame(msiv, this$.metricsDir())
        this$.runs <- as.numeric(column(metricsDf, "run"))
        return(this$.runs);
    }
    curveDir <- squish(this$.curveDir(), "/", msiv$fileName())
    if(!isDirectory(curveDir)) return()
    fileGlob <- squish("run_.*\\.bin")
    run.files <- list.files(curveDir, fileGlob)
    this$.runs <- as.numeric(gsub("\\D+", "", run.files, perl = TRUE))
    
})

method("hasRunNumbers", "STO", function(this, ...) {
    !is.null(this$.runs)
})

method("runNumbers", "STO", function(this, ...) {
    assert(this$hasRunNumbers(), "no MSIVs with raw data in CurveCube (nowhere to get run numbers)")
    this$.runs
})

method(".curveDir", "STO", function(this, ...) {
    this$dirname("CurvesBin")
})

method("destroy", "STO", function(class, dir, id, ...) {
    needs(dir = "character", id = "character")
    unlink(recursive=TRUE, squish(dir, "/", id))
})

method("dirname", "STO", function(this, extra = "", ...) {
    squish(this$.dir, "/", extra)
})

method("parameters", "STO", function(this, param.space = NULL, ...) {
    needs(param.space = "ParameterSpace?")
    dirname <- this$dirname("Parameters")
    siv <- first(this$msivs())$.siv
    fname <- squish(dirname, "/", as.character(siv), ".csv")
    if(!is.null(param.space)) {
        assertFalse(file.exists(fname), "cannot create a second parameter space over existing one")
        dir.create(dirname)
        param.space$writeCSV(fname)
    } else {
        requireDirectory(dirname, "no persisted ParameterSpace exists")
        param.space <- ParameterSpace$readCSV(fname)
    }
    param.space
})

method("curves", "STO", function(this, msivs = this$msivs(), interval = NULL, range = NULL, ...) {
    needs(msivs="MSIV|list(MSIV)")
    CurveCube(this$.curveDir(), listify(msivs), ifElse(this$.calculateMetrics, this$runNumbers(), NULL), interval = interval, range = range)
})

method("metrics", "STO", function(this, msivs = this$msivs(), withCurves = TRUE, interval = NULL, range = NULL, ...) {
    needs(msivs="MSIV|list(MSIV)", withCurves="logical")
    msivs <- listify(msivs)
    curves <- NULL
    if(withCurves && this$.calculateMetrics) curves <- this$curves(msivs, interval, range)
    cube <- MetricCube(msivs, this$runNumbers(), curves, doCalculate = this$.calculateMetrics)
    if(isDirectory(this$.metricsDir()))
        cube$load(this$.metricsDir())
    cube
})

method(".metricsDir", "STO", function(this, ...) {
    this$dirname("Metrics")      
})

method("surface", "STO", function(this, 
    msiv, rowParam, colParam, metric, 
    aggregationFunction, filter = RunFilter$with("ALL", this$.runs), metric.cube = this$metrics(), ...) 
{
    needs(msiv="MSIV", rowParam="character", colParam="character", metric="Metric", aggregationFunction="function")

    params <- this$parameters()$subSet(filter$runs(), c(rowParam, colParam))
    metrics <- metric.cube$values(metric, msiv, filter)
    Surface(params, metrics, aggregationFunction)
})

method("msiv", "STO", function(this, name, ...) {
    needs(name="character")
    markets <- sapply(this$msivs(), market)
    msivs <- this$msivs()[markets %in% name]
    failIf(length(msivs) == 0, "cannot find market '", name, "' in msivs.  Have:\n", commaSep(markets)) 
    the(msivs)
})

method("combine", "STO", function(this, portfolio, nParallel = 6, ...) {
    needs(portfolio="Portfolio", nParallel="numeric")
    
    JPortfolio$main_by_StringArray(c(this$dirname(), portfolio$market(), nParallel)) 
})

method("siv", "STO", function(this, ...){
	failUnless(length(this$msivs()) > 0,"Method requires at least one MSIV defined on STO.")
	first(this$msivs())$siv()
})