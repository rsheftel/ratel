constructor("TimeSeriesBatchUpload", function(dir = "character") {
    this <- extend(RObject(), "TimeSeriesBatchUpload", .base = the(dir), .archiveDir = NULL)
    if (inStaticConstructor(this)) return(this)
    if (!isDirectory(this$archiveDir())) dir.create(this$archiveDir(), recursive=TRUE)
    this
})

method("upload", "TimeSeriesBatchUpload", function(this, ...) {
    tsdb <- TimeSeriesDB()
    requireDirectory(this$.base)
    requireDirectory(this$todayDir())
    files <- list.files(this$todayDir(), "\\.csv", full.names = TRUE)
    failureArray <- Array("character", "numeric", "character", files, 1)
    for (file in files) {
        shortFile <- gsub(".*/", "", file)
        tryCatch(
            { 
                ts.array <- TimeSeriesFile$readTimeSeries(file)
                tsdb$writeTimeSeries(ts.array)
                archiveFile <- path(this$archiveDir(), shortFile)
                file.rename(file, archiveFile)
            },
            error = function(e) {
                failureArray$set(file, 1, conditionMessage(e))
                failureFile <- path(this$failuresDir(), shortFile)
                file.rename(file, failureFile)
            }
        )
    }
    failures <- failureArray$fetchColumn(1)
    failures <- failures[!is.na(failures),, drop=FALSE]

    return(failures)
})

method("todayDir", "TimeSeriesBatchUpload", function(this, ...) {
    path(this$.base, "Today")
})

method("archiveDir", "TimeSeriesBatchUpload", function(this, ...) {
    if (is.null(this$.archiveDir))
        this$.archiveDir <- path(this$.base, "Archive", strftime(as.POSIXlt(Sys.time()), "%Y%m%d"))
    this$.archiveDir
})

method("failuresDir", "TimeSeriesBatchUpload", function(this, ...) {
    path(this$.base, "Failures")
})

method("run", "TimeSeriesBatchUpload", function(static, ...) {
    dir <- ifElse(isWindows(), "V:/TSDB_upload", "/data/TSDB_upload")
    uploader <- TimeSeriesBatchUpload(dir)
    failures <- uploader$upload()
    if(length(failures) > 0) {
        cat("\nErrors during time series upload! (bad files stored in ", uploader$failuresDir(),")\n\n", sep="")
        for(i in seq_along(failures)) {
            cat(rownames(failures)[[i]], ":\n")
            cat(failures[[i, 1]], "\n-----------------------\n")
        }
        return(-1)
    } else {
        cat("\nAll time series uploaded successfully!\n")
        return(0)
    }

})
