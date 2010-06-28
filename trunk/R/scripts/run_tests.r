#! /tp/bin/Rscript --no-site-file --no-init-file

args <- commandArgs()
args <- args[-(1:match("--args", args))]

library("RUnit")
#library("GSFCore")

## How to run the tests (do not uncomment in this file,
## but execute the commands at the R prompt):
## All you have to do is to adapt the directory locations.
## ------------------------------------------------

## define the test suite:
regex <- ifelse(length(args) == 0, "^test.+\\.[rR]$", paste("^test.*", args[[1]], ".*\\.[rR]$", sep = ""))
funcRegex <- ifelse(length(args) < 2, "^test.+", paste("^test.*", args[[2]], ".*", sep = ""))

testsuite.cf <- defineTestSuite("myTests", dirs="tests", testFileRegexp=regex, testFuncRegexp=funcRegex)

## run test suite:
Sys.setenv(NO_PROGRESS_DOTS="1")
testResult <- runTestSuite(testsuite.cf)

## print text protocol to console:
#printQuantysProtocol(testResult)
errInfo <- getErrors(testResult)
if (errInfo$nErr > 0 || errInfo$nFail > 0) {
    cat("\n\n", errInfo$nErr, "errors.  ", errInfo$nFail, "failures.\n")
    quit(status = 255)
}

printTextProtocol(testResult)
#cat(squish("\n\nALL (", errInfo$nTestFunc, ") tests passed successfully!\n")
cat("\n\nALL tests passed successfully!\n")

