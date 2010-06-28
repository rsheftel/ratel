#! /tp/bin/Rscript --no-init-file --no-site-file

args <- commandArgs()
args <- args[-(1:match("--args", args))]

.libPaths("../lib")

library("RUnit")
library("GSFCore")

arg.list <- getopt("o:p:", args)
file.name <- ifelse(is.null(arg.list$o), "", arg.list$o)
if(is.null(arg.list$p)) {
    packages <- list.files() 
} else {
    packages <- strsplit(arg.list$p, ",")[[1]]
}

hasTests <- function(dir) { file.exists(squish(dir, "/tests")) }
packages <- packages[sapply(packages, hasTests)]

## How to run the tests (do not uncomment in this file,
## but execute the commands at the R prompt):
## All you have to do is to adapt the directory locations.
## ------------------------------------------------

## define the test suite:
testsuite.cf <- defineTestSuite("allTests", dirs=paste(packages, "tests", sep = "/"), testFileRegexp="^test.+\\.[rR]$")

## run test suite:

Sys.setenv(NO_PROGRESS_DOTS = "1")
test.result <- runTestSuite(testsuite.cf)

## print text protocol to console:
#printTextProtocol(test.result, fileName = file.name)
printTextProtocol(test.result, fileName = file.name)

err.info <- getErrors(test.result)
if (err.info$nErr > 0 || err.info$nFail > 0) {
    cat("\n\n", err.info$nErr, "errors.  ", err.info$nFail, "failures.\n")
    quit(status = 255)
}

cat("\n\nALL tests passed successfully!\n")

## print HTML version to a file:
#printHTMLProtocol(testResult, fileName="someFileName.html")

## In this case we also have a shortcut
#runTestFile("directoryOfThisFile/runitcfConversion.r")
