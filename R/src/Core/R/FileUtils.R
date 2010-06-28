

destroyDirectory <- function(dir, failExisting = TRUE) {
    needs(dir = "character")
    if(failExisting) requireDirectory(the(dir))
    else if (!isDirectory(the(dir))) return()
    unlink(recursive=TRUE, dir)
}

copyDirectory <- function(source, target) { 
    needs(source = "character", target = "character")
    requireDirectory(source, "invalid source directory")
    failIf(isUNC(target), "Please use a path like V:/foo instead of //machine/data/foo")
    if (isDirectory(target)) 
        fail("target directory ", target, " already exists!")
        
    failUnless(
    	dir.create(target, recursive=TRUE, showWarnings=TRUE), 
    	"failed to create ", target
    )
    for(file in list.files(source, all.files=TRUE)) {
        if (file == "." || file == "..") next
        sourceFilePath = squish(source, "/", file)
        targetFilePath = squish(target, "/", file)
        if(isDirectory(sourceFilePath)) 
            copyDirectory(sourceFilePath, targetFilePath)
        else
            file.copy(sourceFilePath, targetFilePath)
    }

}

isUNC <- function(dir) {
    matches("^//", dir)
}

checkEmptyDirectory <- function(dir) {
    requireDirectory(dir)
    checkLength(list.files(dir), 0)
}

createTempDir <- function(extra) {
    dir <- tempdir()
    temp <- squish(dir, "/", extra)
    dir.create(temp, recursive = TRUE)
    temp
}

recreateSessionTempDir <- function() {
    dir <- tempdir()
    requireDirectory(dir,
        squish("per session temp dir ", dir, " has been deleted - bad test interaction?")
    )
    destroyDirectory(dir)
    dir.create(dir)
}
path <- function(...) {
    params <- list(...)
    paste(params, sep="/", collapse="/")
}

checkFileExists <- function(dir, files) {
    filenames <- paste(the(dir), "/", files, sep="")
    existence <- file.exists(filenames)
    if(!all(existence)) {
        fail(
            "files do not exist: ", commaSep(files[!existence]), "\n",
            "in ", dir
        )
    }
}

checkFileCount <- function(dir, expectedCount) {
    requireDirectory(dir)
    checkLength(list.files(the(dir)), expectedCount)
}

fileMatches <- function(testFilename, benchFilename, deleteFile=FALSE){
	testData <- read.csv(testFilename)
	benchData <- read.csv(benchFilename)
	if (deleteFile) file.remove(testFilename)	
	return(checkSame(benchData,testData))
}
