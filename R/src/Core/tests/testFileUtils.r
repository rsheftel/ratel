library("Core")

testCopyAndDestroyDirectory <- function() { 
    dir = squish(tempdir(), "/testdir")
    dirToCopy = squish(dir, "/toCopy")
    dir.create(dirToCopy, recursive=TRUE)
    requireDirectory(dirToCopy)
    mainFile <- squish(dirToCopy, "/inMain")
    file.create(mainFile)
    subDir <- squish(dirToCopy, "/someDir")
    dir.create(subDir)
    requireDirectory(subDir)
    subFile <- squish(subDir, "/inSubDir")
    file.create(subFile)
    
    copiedDir <- squish(dir, "/target")
    copyDirectory(dirToCopy, copiedDir)
    requireDirectory(copiedDir)
    requireDirectory(squish(copiedDir, "/someDir"))

    checkTrue(file.exists(squish(dir, "/target/someDir/inSubDir")))

    destroyDirectory(dir)
    checkFalse(isDirectory(dir))

    
    on.exit({ destroyDirectory(dir, failExisting=FALSE); dir.create(dir) })
}

testUNCPathsBorkWithGoodErrorMessage <- function() {
    dir = tempdir()
    shouldBombMatching(copyDirectory(dir, "//a"), "Please use a path like V:/foo instead of //machine/data/foo")
}

testDirectoryCopyWithInvalidArguments <- function() {
    dir = tempdir()
    dir.create(dir, showWarnings=FALSE)
    shouldBombMatching(
        copyDirectory("nonexistent dir", "target"), 
        "invalid source directory: nonexistent dir"
    )
    shouldBombMatching(
        copyDirectory(dir, dir), 
        squish(":target directory ", dir, " already exists!")
    )
    on.exit({ destroyDirectory(dir, failExisting=FALSE); dir.create(dir) })
}

#test.fileMatches <- function(){
#	testDataPath <- squish(system.file("testdata", package="Core"),'/')
#	fileMatches(squish(testDataPath,'fileMatches_1.csv'),squish(testDataPath,'fileMatches_2.csv'))
#	shouldBomb(fileMatches(squish(testDataPath,'fileMatches_1.csv'),squish(testDataPath,'fileMatches_3.csv')))
#	
#	#Test deleting when done
#	tempFile <- squish(dataDirectory(),'temp_TSDB/','test.fileMatches.csv')
#	file.copy(squish(testDataPath,'fileMatches_1.csv'), tempFile)
#	file.exists(tempFile)
#	fileMatches(tempFile, squish(testDataPath,'fileMatches_1.csv'), deleteFile=TRUE)
#	!file.exists(tempFile)
#}
