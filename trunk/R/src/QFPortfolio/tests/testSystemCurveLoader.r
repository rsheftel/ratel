library(QFPortfolio)

testDir <- system.file("testdata/PortfolioEquityCurves", package="QFPortfolio")

testSystemCurveLoader <- function() {
    on.exit(JDb$rollback())

    insertTestData()
    
    curves <- curves("TEST.AllSystems")
    
    checkSame(c("TEST.CVE", "TEST.FXCarry", "TEST.NDayBreak"), names(curves))
    checkSame(addCurves("NDayBreak*")$pnl(), third(curves)$pnl())
    checkSame(addCurves("CVE*")$pnl(), first(curves)$pnl())
    checkSame(addCurves("FXCarry*")$pnl(), second(curves)$pnl())
    
    checkLength(curves("TEST.NDayBreak"), 7)

    range <- Range("2004-01-03", "2008-05-30")
    smallCurves <- curves("TEST.AllSystems", range)
    checkSameLooking(first(third(smallCurves)$dates()), as.POSIXct("2004-01-05"))
    checkSameLooking(last(second(smallCurves)$dates()), as.POSIXct("2008-05-27"))
}

curves <- function(group, range = NULL) {
    CurveGroup(group)$childCurves(testDir, extension = "csv", range = range)
}

insertTestData <- function() {
    
    JGroups$GROUPS()$insert_by_String("TEST.AllSystems")
    JGroups$GROUPS()$insert_by_String("TEST.NDayBreak")
    JGroups$GROUPS()$insert_by_String("TEST.CVE")
    JGroups$GROUPS()$insert_by_String("TEST.FXCarry")
    JGroups$GROUPS()$insert_by_String_String_double("TEST.AllSystems", "TEST.NDayBreak", 1.0)
    JGroups$GROUPS()$insert_by_String_String_double("TEST.AllSystems", "TEST.CVE", 1.0)
    JGroups$GROUPS()$insert_by_String_String_double("TEST.AllSystems", "TEST.FXCarry", 1.0)
    insertLeaf("TEST.NDayBreak", "AD.1C_NDayBreak_daily_1.0", "FXBD20", 1.0)
    insertLeaf("TEST.NDayBreak", "BP.1C_NDayBreak_daily_1.0", "FXBD20", 1.0)
    insertLeaf("TEST.NDayBreak", "CD.1C_NDayBreak_daily_1.0", "FXBD20", 1.0)
    insertLeaf("TEST.NDayBreak", "EC.1C_NDayBreak_daily_1.0", "FXBD20", 1.0)
    insertLeaf("TEST.NDayBreak", "CL.1C_NDayBreak_daily_1.0", "NRGBD40", 1.0)
    insertLeaf("TEST.NDayBreak", "FV.1C_NDayBreak_daily_1.0", "BFBD20", 1.0)
    insertLeaf("TEST.NDayBreak", "FV.1C_NDayBreak_daily_1.0", "BFBD30", 1.0)
    insertLeaf("TEST.CVE", "CET21.AA5MCVE_daily_2.1", "CVEV1", 1.0)
    insertLeaf("TEST.CVE", "CET21.YUM5MCVE_daily_2.1", "CVEV1", 1.0)
    insertLeaf("TEST.FXCarry", "USDTRY6MTRI.P1_FXCarry", "FXCarryV1", 1.0)
    
}

insertLeaf <- function(group, msiv, pv, weight) {
    msivpv <- JMsivPv$by_String_String(msiv, pv)
    JGroupLeafs$LEAFS()$insert_by_String_MsivPv_double(group, msivpv, weight);
}

addCurves <- function(pattern) {
    files <- list.files(testDir, pattern, full.names = TRUE)
    curves <- lapply(files, function(file) PositionEquityCurve(CurveFileLoader(file)))
    WeightedCurves(curves)$curve()
}
