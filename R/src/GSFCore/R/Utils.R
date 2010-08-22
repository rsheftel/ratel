jDoubles <- function (x) {
    lapply(x, function(d) JDouble$by_double(d))
}

jIntegers <- function (x) {
    lapply(x, function(i) JInteger$by_integer(i))
}

jBooleans <- function (x) {
    lapply(x, function(b) JBoolean$by_boolean(b))
}

jStrings <- function (x) {
    lapply(x, function(s) JString$by_String(s))
}

stringsVector <- function(l) {
    unlist(japply(l$iterator(), JString(), toString))
}

monthCode <- function(num) { 
    JMonthCode$fromNumber_by_String(as.character(num))$letter()
}

method("as.JDate", "POSIXct", function(this, ...) {
    JDate$by_long(millis.from.POSIXct(this))
})

method("as.POSIXct", "JDate", function(this, ...) {
    if (is.jnull(this$.jobj)) return(NULL)
    POSIXct.from.millis(this$getTime())
})

millis.from.POSIXct <- function(t) {
    t <- as.vector(t)
    if(isWindows()) {
        bugged <- isBugged(t)
        t[bugged] <- t[bugged] + 3600
    }
    t * 1000
}

ymdHuman <- function(date) {
    JDates$ymdHuman_by_Date(as.JDate(date))
    
}

POSIXct.from.millis <- function(t) {
    t <- t / 1000
    if(isWindows()) {
        bugged <- isBugged(t)
        t[bugged] <- t[bugged] - 3600
    }
    attr(t, "class") <-  c("POSIXt", "POSIXct")
    attr(t, "tzone") <-  ""
    t
}

isBugged <- function(x) {
    (x < 1162706400) & (
        (x >= 1162101600 & x < 1162706400) |
        (x >= 1142146800 & x < 1143961200) |
        (x >= 1130652000 & x < 1131256800) |
        (x >= 1110697200 & x < 1112511600) |
        (x >= 1099202400 & x < 1099807200) |
        (x >= 1079247600 & x < 1081062000) |
        (x >= 1067148000 & x < 1067752800) |
        (x >= 1047193200 & x < 1049612400) |
        (x >= 1035698400 & x < 1036303200) |
        (x >= 1015743600 & x < 1018162800) |
        (x >= 1004248800 & x < 1004853600) |
        (x >= 984294000 & x < 986108400) |
        (x >= 972799200 & x < 973404000) |
        (x >= 952844400 & x < 954658800) |
        (x >= 941349600 & x < 941954400) |
        (x >= 921394800 & x < 923209200) |
        (x >= 909295200 & x < 909900000) |
        (x >= 889340400 & x < 891759600) |
        (x >= 877845600 & x < 878450400) |
        (x >= 857890800 & x < 860310000) |
        (x >= 846396000 & x < 847000800) |
        (x >= 826441200 & x < 828860400) |
        (x >= 814946400 & x < 815551200) |
        (x >= 794991600 & x < 796806000) |
        (x >= 783496800 & x < 784101600) |
        (x >= 763542000 & x < 765356400) |
        (x >= 752047200 & x < 752652000) |
        (x >= 732092400 & x < 733906800) |
        (x >= 719992800 & x < 720597600) |
        (x >= 700038000 & x < 702457200) |
        (x >= 688543200 & x < 689148000) |
        (x >= 668588400 & x < 671007600) |
        (x >= 657093600 & x < 657698400) |
        (x >= 637138800 & x < 638953200) |
        (x >= 625644000 & x < 626248800) |
        (x >= 605689200 & x < 607503600) |
        (x >= 594194400 & x < 594799200) |
        (x >= 574239600 & x < 576054000) |
        (x >= 562140000 & x < 562744800) |
        (x >= 542185200 & x < 544604400) |
        (x >= 530690400 & x < 531295200) |
        (x >= 510735600 & x < 514969200) |
        (x >= 499240800 & x < 499845600) |
        (x >= 479286000 & x < 483519600) |
        (x >= 467791200 & x < 468396000) |
        (x >= 447836400 & x < 452070000) |
        (x >= 436341600 & x < 436946400) |
        (x >= 416386800 & x < 420015600) |
        (x >= 404892000 & x < 405496800) |
        (x >= 384937200 & x < 388566000) |
        (x >= 372837600 & x < 373442400) |
        (x >= 352882800 & x < 357116400) |
        (x >= 341388000 & x < 341992800) |
        (x >= 321433200 & x < 325666800) |
        (x >= 309938400 & x < 310543200) |
        (x >= 289983600 & x < 294217200) |
        (x >= 278488800 & x < 279093600) |
        (x >= 258534000 & x < 262767600) |
        (x >= 247039200 & x < 247644000) |
        (x >= 227084400 & x < 230713200) |
        (x >= 215589600 & x < 216194400) |
        (x >= 195634800 & x < 199263600) |
        (x >= 183535200 & x < 184140000) |
        (x >= 162370800 & x < 163580400) |
        (x >= 152085600 & x < 152690400) |
        (x >= 126687600 & x < 132130800) |
        (x >= 120636000 & x < 121240800) |
        (x >= 100681200 & x < 104914800) |
        (x >= 89186400 & x < 89791200) |
        (x >= 69231600 & x < 73465200) |
        (x >= 57736800 & x < 58341600) |
        (x >= 37782000 & x < 41410800) |
        (x >= 25682400 & x < 26287200) |
        (x >= 5727600 & x < 9961200)
    )
}

businessDaysAgo <- function(days,date,center = "nyb"){
    date <- as.POSIXct(date)
    date <- as.JDate(date)
    as.POSIXct(JDates$businessDaysAgo_by_int_Date_String(days,date,center))
}

as.POSIXct.numeric <- function(x,tz=""){
	class(x) <- c("POSIXt","POSIXct")
	attr(x,"tzone") <- tz
	x
}

makeWindowsFilename <- function(filename){
	if(leftStr(filename,6)=='/data/') return(squish('V:/',midStr(filename,7,nchar(filename)-6)))
	return(filename)
}

makeLinuxFilename <- function(filename){
	return(sub("^V:", "/data",filename,ignore.case=TRUE)) 
}

makeFilenameNative <- function(filename){
	if(isWindows()) return(makeWindowsFilename(filename))
	return(makeLinuxFilename(filename))
}
