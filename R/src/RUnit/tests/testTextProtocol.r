##  RUnit : A unit test framework for the R programming language
##  Copyright (C) 2003, 2004  Thomas Koenig, Matthias Burger, Klaus Juenemann
##
##  This program is free software; you can redistribute it and/or modify
##  it under the terms of the GNU General Public License as published by
##  the Free Software Foundation; either version 2 of the License, or
##  (at your option) any later version.
##
##  This program is distributed in the hope that it will be useful,
##  but WITHOUT ANY WARRANTY; without even the implied warranty of
##  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
##  GNU General Public License for more details.
##
##  You should have received a copy of the GNU General Public License
##  along with this program; if not, write to the Free Software
##  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
##
##  $Id: runitTextProtocol.r,v 1.2 2007/03/18 23:51:51 burgerm Exp $




testRUnit.printTextProtocol <- function()
{
  ##  copy baseenv() logger
  tmp <- get(".testLogger", envir = .GlobalEnv)
  testCaseDir <- file.path(system.file(package="RUnit"), "examples")
  testSuiteInternal <- defineTestSuite("RUnit Self Test", testCaseDir, "correctTestCase.r")
  testData2 <- runTestSuite(testSuiteInternal, useOwnErrorHandler=FALSE)

  timeStamp <- format(Sys.time(), "%y%m%d-%H%M")
  testProtocolFile <- file.path(tempdir(), paste(timeStamp, "test_printHTMLProtocol.txt", sep="_"))
  ret <- printTextProtocol(testData2, fileName=testProtocolFile)

  assign(".testLogger", tmp, envir = .GlobalEnv)
  
  checkTrue( file.exists(testProtocolFile))


  ##  input argument error handling
  ##  missing 'testData' object
  shouldBomb(printTextProtocol())

  ##  wrong class
  shouldBomb(printTextProtocol("dummy"))

  
  ##  fileName arg errors
  testData <- list()
  class(testData) <- "RUnitTestData"
  ##  wrong type
  shouldBomb(printTextProtocol(testData, fileName=numeric(1)))
  ##  wrong length
  shouldBomb(printTextProtocol(testData, fileName=character(0)))
  shouldBomb(printTextProtocol(testData, fileName=character(2)))

  
  ##  separateFailureList arg errors
  ##  wrong type
  shouldBomb(printTextProtocol(testData, separateFailureList=numeric(0)))
  ##  wrong length
  shouldBomb(printTextProtocol(testData, separateFailureList=logical(0)))
  shouldBomb(printTextProtocol(testData, separateFailureList=logical(2)))

  ##  showDetails arg errors
  ##  wrong type
  shouldBomb(printTextProtocol(testData, showDetails=numeric(0)))
  ##  wrong length
  shouldBomb(printTextProtocol(testData, showDetails=logical(0)))
  shouldBomb(printTextProtocol(testData, showDetails=logical(2)))
}

