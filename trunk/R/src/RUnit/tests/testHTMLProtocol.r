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
##  $Id: runitHTMLProtocol.r,v 1.2 2007/03/18 23:52:37 burgerm Exp $




testRUnit.printHTMLProtocol <- function()
{
  ##  FIXME
  ##  this is not safe, think about assigning .logger to new environment
  ##  copy baseenv() .logger
  tmp <- get(".testLogger", envir = .GlobalEnv)
  testCaseDir <- file.path(system.file(package="RUnit"), "examples")
  testSuiteInternal <- defineTestSuite("RUnit Self Test", testCaseDir, "correctTestCase.r")
  testData2 <- runTestSuite(testSuiteInternal, useOwnErrorHandler=FALSE)

  timeStamp <- format(Sys.time(), "%y%m%d-%H%M")
  testProtocolFile <- file.path(tempdir(), paste(timeStamp, "test_printHTMLProtocol.html", sep="_"))
  ret <- printHTMLProtocol(testData2, fileName=testProtocolFile)

  assign(".testLogger", tmp, envir = .GlobalEnv)
  
  checkTrue( file.exists(testProtocolFile))
  
  
  ##  input argument error handling
  ##  missing 'testData' object
  shouldBomb(printHTMLProtocol())

  ##  wrong class
  shouldBomb(printHTMLProtocol("dummy"))

  
  ##  fileName arg errors
  testData <- list()
  class(testData) <- "RUnitTestData"
  ##  wrong type
  shouldBomb(printHTMLProtocol(testData, fileName=numeric(1)))
  ##  wrong length
  shouldBomb(printHTMLProtocol(testData, fileName=character(0)))
  shouldBomb(printHTMLProtocol(testData, fileName=character(2)))

  
  ##  separateFailureList arg errors
  ##  wrong type
  shouldBomb(printHTMLProtocol(testData, separateFailureList=numeric(0)))
  ##  wrong length
  shouldBomb(printHTMLProtocol(testData, separateFailureList=logical(0)))
  shouldBomb(printHTMLProtocol(testData, separateFailureList=logical(2)))
}

