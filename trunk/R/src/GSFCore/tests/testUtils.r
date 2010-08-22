testMonthCode <- function() { 
    checkSame('h', monthCode(3))
}

testRDate <- function() {
    jDate <- JDates$yyyyMmDdHhMmSs_by_String("2008/04/01 12:34:56")
    rDate <- as.POSIXct(jDate)
    checkSame(as.POSIXct("2008/04/01 12:34:56"), rDate)
}

testJDate <- function() {
    rDate <- as.POSIXct("2008/04/01 12:34:56")
    checkSame(as.JDate(rDate)$toString(), JDates$yyyyMmDdHhMmSs_by_String("2008/04/01 12:34:56")$toString())
}

testDateThatWindowsHates <- function() {
    checkJDateBackForth("2001/03/29")
    checkJDateBackForth("2006/10/29")
    checkJDateBackForth("2006/10/29 01:00:00")
    checkJDateBackForth("2006/10/29 02:00:00")
    checkJDateBackForth("2006/10/29 03:00:00")
    checkJDateBackForth("2006/11/05")
    checkJDateBackForth("2006/11/05 01:00:00")
    checkJDateBackForth("2006/11/05 02:00:00")
    checkJDateBackForth("2006/11/05 03:00:00")
    checkJDateBackForth("2006/03/12")
    checkJDateBackForth("2006/03/12 01:00:00")
# R cannot understand this date on Windows
#    checkJDateBackForth("2006/03/12 02:00:00")
    checkJDateBackForth("2006/03/12 03:00:00")
    checkJDateBackForth("2006/04/02")
    checkJDateBackForth("2006/04/02 01:00:00")
# This date does not exist
#    checkJDateBackForth("2006/04/02 02:00:00")
    checkJDateBackForth("2006/04/02 03:00:00")
}

checkJDateBackForth <- function(d) {
    jDate <- JDates$date_by_String(d)
    rDate <- as.POSIXct(jDate)
    checkSameLooking(as.POSIXct(d), rDate)
    checkSame(JDates$ymdHuman_by_Date(as.JDate(rDate)), JDates$ymdHuman_by_Date(jDate))
}

testBusinessDaysAgo <- function(){
    checkSame(businessDaysAgo(1, "2008/03/12"), as.POSIXct("2008/03/11"))   
    checkSame(businessDaysAgo(1, "2008/05/06", "lnb"), as.POSIXct("2008/05/02"))    
    checkSame(businessDaysAgo(1, "2008/05/06", "nyb"), as.POSIXct("2008/05/05"))        
}

testasPOSIXct <- function() {
	testValue <- 1182484800
	testDate <- as.POSIXct(testValue)
	checkSame("2007-06-22",as.character(testDate))
}

test.makeWindowsFilename <- function(){
	checkSame('V:/testdir/File.xxx', makeWindowsFilename('/data/testdir/File.xxx'))
	checkSame('V:/testdir/File.xxx', makeWindowsFilename('V:/testdir/File.xxx'))
}

test.makeLinuxFilename <- function(){
	checkSame('/data/testdir/File.xxx', makeLinuxFilename('/data/testdir/File.xxx'))
	checkSame('/data/testdir/File.xxx', makeLinuxFilename('V:/testdir/File.xxx'))
	checkSame('/data/testdir/File.xxx', makeLinuxFilename('v:/testdir/File.xxx'))
}

test.makeFilenameNative <- function(){
	if (isWindows()){
		checkSame('V:/testdir/File.xxx', makeFilenameNative('/data/testdir/File.xxx'))
		checkSame('V:/testdir/File.xxx', makeFilenameNative('V:/testdir/File.xxx'))
	}else{
		checkSame('/data/testdir/File.xxx', makeFilenameNative('/data/testdir/File.xxx'))
		checkSame('/data/testdir/File.xxx', makeFilenameNative('V:/testdir/File.xxx'))
		checkSame('/data/testdir/File.xxx', makeFilenameNative('v:/testdir/File.xxx'))
	}
}
