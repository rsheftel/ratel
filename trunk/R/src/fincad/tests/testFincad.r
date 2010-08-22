cat("\n\nTest cases for fincad module\n\n")

library("fincad")

testFincad <- function()
{
    ##@bdescr
    ## test cases for FinCAD function
    ##@edescr

    dates <- c( "2000-12-30", "2006-02-01", "1900-01-01", "1970-01-01", "1999-12-31", "2000-02-29", "2000-03-01", "1996-03-01", "1996-02-29", "2100-03-01", "2100-02-28")
    answers <- c( 36890, 38749, 1, 25569, 36525, 36585, 36586, 35125, 35124, 73110, 73109 )

    checkIdentical(fincad.date(dates), answers)
    checkIdentical(fincad.date(as.POSIXlt(dates)), answers)
    checkIdentical(fincad.date(as.POSIXct(dates)), answers)
    checkIdentical(fincad.to.POSIXct(answers), as.POSIXct(dates))

    # Examples are from FinCAD example Excel sheets
    checkEquals(182, fincad("aaAccrual_days", d_e = "2000-12-30", d_t = "2001-06-30", acc = 2))
    checkEquals(182, fincad("aaAccrual_days", d_e = as.POSIXct("2000-12-30"), d_t = as.POSIXct("2001-06-30"), acc = 2))
    checkEquals(127, fincad("aaAccrual_days_act252", d_e = "2000-12-30", d_t = "2001-06-30", hl = c("2001-01-01", "2001-02-20", "2001-04-14", "2001-05-29", "2001-07-04", "2001-09-04", "2001-10-09", "2001-11-23", "2001-12-25")))
    shouldBomb(fincad("aaAccrual_days", d_e = "2000-12-30", d_t = "2001-06-30"))
    shouldBomb(fincad("aaAccrual_days", d_e = "2000-12-30", d_t = "2001-06-30", acc = 16))

#    checkEquals(matrix(10.59426947,1,1), fincad("aaCDS", 
#        d_v = "2005-12-01",
#        contra_d = c(fincad.date("2008-12-01"), fincad.date("2005-06-01"), 0, 0, 2, 2, 1),
#        cpn_pr = 0.02,
#        freq_pr = 2,
#        pr_acc_type = 1,
#        acc = 1,
#        d_rul = 1,
#        pr_fix = 0.01,
#        ref_type = 1,
#        ref_tbl = 100,
#        p_off = 1,
#        dp_type = 1,
#        dp_crv = list( c(fincad.date("2005-12-01"), fincad.date("2006-06-01"), 0.05),
#                       c(fincad.date("2005-12-01"), fincad.date("2006-12-01"), 0.055),
#                       c(fincad.date("2005-12-01"), fincad.date("2007-12-01"), 0.06),
#                       c(fincad.date("2005-12-01"), fincad.date("2008-12-01"), 0.065),
#                       c(fincad.date("2005-12-01"), fincad.date("2009-12-01"), 0.07)
#                     ),
#        intrp_tb = c(1,1,4,2,2,1),
#        rate_recover = 0.4,
#        hl = c("2004-12-25", "2005-01-01", "2005-12-25", "2006-01-01", "2006-12-25", "2007-01-01", "2007-12-25", "2008-01-01", 
#               "2008-12-25", "2009-01-01", "2009-12-25", "2010-01-01"),
#        dfstd = list( c(fincad.date("2005-12-01"), 1.000000000),
#                      c(fincad.date("2006-06-01"), 0.971285862),
#                      c(fincad.date("2006-12-01"), 0.943396226),
#                      c(fincad.date("2007-12-01"), 0.889996440),
#                      c(fincad.date("2008-12-01"), 0.839619283),
#                      c(fincad.date("2010-12-01"), 0.747258173),
#                      c(fincad.date("2015-12-01"), 0.558394777),
#                      c(fincad.date("2020-12-01"), 0.417265061)
#                    ),
#        intrp = 1,
#        pos = 1,
#        calc_para = c(1,1),
#        stat = 1
#    ))
#
    checkEquals(
        matrix(c( 10.59426947,
                  15.30283368,
                  -4.708564209,
                  -0,
                  10.59426947,
                  0.060752421,
                  -0.001634119,
                  0,
                  fincad.date("2006-06-01"),
                  fincad.date("2005-12-01"),
                  6,
                  0.021108116,
                  -0.024841385,
                  -0.013278053
                ),14,1), 
        fincad("aaCDS", 
            d_v = "2005-12-01",
            contra_d = data.frame( "2008-12-01", "2005-06-01", 0, 0, 2, 2, 1),
            cpn_pr = 0.02,
            freq_pr = 2,
            pr_acc_type = 1,
            acc = 1,
            d_rul = 1,
            pr_fix = 0.01,
            ref_type = 1,
            ref_tbl = 100,
            p_off = 1,
            dp_type = 1,
            dp_crv = data.frame( 
                eff_date = c("2005-12-01", "2005-12-01", "2005-12-01", "2005-12-01", "2005-12-01"),
                mat_date = c("2006-06-01", "2006-12-01", "2007-12-01", "2008-12-01", "2009-12-01"),
                spread   = c(        0.05,        0.055,         0.06,        0.065,         0.07),
                check.rows = TRUE
            ),
            intrp_tb = c(1,1,4,2,2,1),
            rate_recover = 0.4,
            hl = c("2004-12-25", "2005-01-01", "2005-12-25", "2006-01-01", "2006-12-25", "2007-01-01", "2007-12-25", 
                   "2008-01-01", "2008-12-25", "2009-01-01", "2009-12-25", "2010-01-01"),
            dfstd = data.frame( 
                date = c("2005-12-01", "2006-06-01", "2006-12-01", "2007-12-01", "2008-12-01", "2010-12-01", "2015-12-01", "2020-12-01"),
                  df = c( 1.000000000,  0.971285862,  0.943396226,  0.889996440,  0.839619283,  0.747258173,  0.558394777,  0.417265061),
                check.rows = TRUE
            ),
            intrp = 1,
            pos = 1,
            calc_para = c(1,1),
            stat = data.frame(1:14)
        )
    )

    checkEquals(
        matrix(c( 
                 37868, 1.00000000000000000, 0.05125670075913580,
                 37872, 0.99945235487404160, 0.05125670075913580,
                 37961, 0.98741401956699193, 0.05096635104810576,
                 38052, 0.97540136237707276, 0.05064724488408512,
                 38236, 0.95198358436593200, 0.05001698439759839,
                 38238, 0.95172210683108727, 0.05002451669517582,
                 38603, 0.90518528274856880, 0.05071300206604845,
                 38968, 0.86151143896757354, 0.05070684427345373,
                 39335, 0.81972412141572992, 0.05070327166313460,
                 39699, 0.78028075198399782, 0.05070114291573580,
                 40064, 0.74263432935649820, 0.05069980302312693,
                 40429, 0.70680424416016641, 0.05069884506112587,
                 40794, 0.67270286305739413, 0.05069812609910440,
                 41162, 0.63998661267946866, 0.05069756253071867,
                 41526, 0.60919152782511987, 0.05069711663777499,
                 41890, 0.57987652569935344, 0.05069703513910406,
                 42255, 0.55189741506758749, 0.05069696699660708
                ), 17, 3, byrow = TRUE
        ),
        fincad("aaSwap_crv3",
            d_v = "2003-09-04",
            cash_crv = data.frame(
                eff_date    = c("2003-09-04", "2003-09-08", "2003-09-08", "2003-09-08"),
                term_date   = c("2003-09-08", "2003-12-06", "2004-03-06", "2004-09-06"),
                rate        = c(        0.05,         0.05,         0.05,         0.05),
                quote_basis = c(           7,            7,            7,            7),
                acc_method  = c(           1,            1,            1,            1),
                use_point   = c(           1,            1,            1,            1),
                check.rows = TRUE
            ),
            fut_crv = data.frame(
                eff_date    = c("2004-09-16", "2004-12-16", "2005-03-17", "2005-06-16", "2005-09-22"),
                term_date   = c("2004-12-16", "2005-03-16", "2005-06-17", "2005-09-16", "2005-12-22"),
                rate        = c(        0.05,         0.05,         0.05,         0.05,         0.05),
                quote_basis = c(           7,            7,            7,            7,            7),
                acc_method  = c(           1,            1,            1,            1,            1),
                use_point   = c(           1,            1,            1,            1,            1),
                check.rows = TRUE
            ),
            swp_crv = data.frame(
                eff_date    = c("2003-09-08", "2003-09-08", "2003-09-08", "2003-09-08"),
                term_date   = c("2005-09-08", "2006-09-08", "2008-09-08", "2013-09-08"),
                rate        = c(        0.05,         0.05,         0.05,         0.05),
                fix_freq    = c(           1,            1,            1,            1),
                fix_acc     = c(           2,            2,            2,            2),
                day_conv    = c(           2,            2,            2,            2),
                use_point   = c(           1,            1,            1,            1),
                reset_rate  = c(           0,            0,            0,            0),
                flt_freq    = c(           3,            3,            3,            3),
                flt_acc     = c(           1,            1,            1,            1),
                check.rows = TRUE
            ),
            boot_swap = 1,
            boot_intrp = 1,
            fut_splice = 1,
            rate_basis = 1,
            acc_rate = 1,
            hl = c("2003-01-01", "2004-01-01"),
            method_gen = 2,
            min_years = 12,
            extend_method = 1,
            sprd = 0,
            sprdtype = 1,
            table_type = 3
        )
    )

}

testErrorHandlingEnable <- function() {
    shouldBombMatching(fincad("aaConvert_cmpd", freq_to=1, rate_from = -0.01, freq_from=2), "must be > 0")
    checkSame(-0.009975, fincadNoErrorHandling("aaConvert_cmpd", freq_to=1, rate_from = -0.01, freq_from=2))
    shouldBombMatching(fincad("aaConvert_cmpd", freq_to=1, rate_from = -0.01, freq_from=2), "must be > 0")

}

