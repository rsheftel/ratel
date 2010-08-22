library("STO")

source(system.file("testHelper.r", package = "STO"))

testUntilNextOptimization <- function() {
	wfo <- createWFO()
	wfo$createSchedule("2005-09-27",365)
	ranges <- ec.untilNextOptimization(wfo)
	ranges.target <- list(
		Range("2005-09-27","2006-09-27"),
		Range("2006-09-27","2007-09-27"),
		Range("2007-09-27","2007-10-05")
	)
	checkSame(ranges.target,ranges)
}
testOptiSteps <- function() {
	wfo <- createWFO()
	wfo$createSchedule("2005-09-27",365)
	ranges <- ec.optiSteps(wfo,1:3)
	ranges.target <- list(
		Range("2005-09-27","2006-09-27"),
		Range("2006-09-27","2007-09-27"),
		Range("2007-09-27","2007-10-05")
	)
	checkSame(ranges.target,ranges)
	ranges <- ec.optiSteps(wfo,1:2)
	ranges.target <- list(
			Range("2005-09-27","2006-09-27"),
			Range("2006-09-27","2007-10-05"),
			"Pass"
	)
	checkSame(ranges.target,ranges)
	ranges <- ec.optiSteps(wfo,2:3)
	ranges.target <- list(
			"Pass",
			Range("2006-09-27","2007-09-27"),
			Range("2007-09-27","2007-10-05")
	)
	checkSame(ranges.target,ranges)
}