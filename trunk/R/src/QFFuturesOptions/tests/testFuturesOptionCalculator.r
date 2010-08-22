library(QFFuturesOptions)

calc <- FuturesOptionCalculator()
checkInherits(calc, "FuturesOptionCalculator")

underlyingPrice <- c(113.203125, 113.203125, 113.203125, 112.578125)
underlyingDV01 <- c(6.819, 6.819, 6.819, 7.057)
underlyingConvexity <- c(0.418, 0.418, 0.418, 0.444)

ticker <- c("TYF8", "TYG8", "TYH8", "TYM8") #option root + expiry, not futures
discountRate <- c(0.04500, 0.04000, 0.04750, 0.04000)
expiry <- c("2007/12/21", "2008/01/25", "2008/02/22", "2008/05/23")
optionPrice <- c(0.7500000, 1.2031250, 1.4531250, 1.9062500)
optionType <- rep('put', 4)
strikeStep <- rep(1, 4)

valueDate = rep("2007/11/30", 4)


testStrike <- function() 
{    
    strikeStep <- c(1, 1, 2, 0.25)    
    strike <- calc$calcStrike(
            underlyingPrice = underlyingPrice,
            strikeStep = strikeStep,
            numStep = 0)    
    checkLength(strike, 1)
    #print(strike)
    checkSame(strike[[1]], c(113, 113, 112, 112.5))
    
    strike <- calc$calcStrike(
        underlyingPrice = underlyingPrice,
        strikeStep = strikeStep,
        numStep = 10)
    
    checkLength(strike, 21)
    checkSame(strike[[11]], c(113, 113, 112, 112.5))
    checkSame(strike[[1]], c(103, 103, 92, 110))
    #print(strikeStep)
}


testOTMPrice <- function() 
{  
    #print("TTTTTTTTTTT")
    #print(strikeStep)
    strike <- calc$calcStrike(
        underlyingPrice = underlyingPrice,
        strikeStep = strikeStep,
        numStep = 0) 
    
    otmPrice <- calc$calcOTMPrice(
        underlyingPrice = underlyingPrice,
        strike = strike[[1]])
    
    #print(otmPrice)
    checkSame(otmPrice, c(0.203125, 0.203125, 0.203125, 0.578125))    
    otmPrice
}


testOTMYield <- function() 
{
    otmPrice <- testOTMPrice()            
    #print(otmPrice)
    
    otmYield <- calc$calcOTMYield(
        underlyingPrice = underlyingPrice,
        underlyingDV01 = underlyingDV01,
        underlyingConvexity = underlyingConvexity,
        otmPrice = otmPrice)
    checkSame(otmYield, c(0.029760945271, 0.029760945271, 0.029760945271, 0.081712162843))    
    otmYield
}

testDV01 <- function() 
{
    otmYield <- testOTMYield()            
    #print(otmYield)
    
    dv01 <- calc$calcDV01(
        underlyingDV01 = underlyingDV01,
        underlyingConvexity = underlyingConvexity,
        otmYield = otmYield)
    checkSame(dv01, c(6.80655992488, 6.80655992488, 6.80655992488, 7.02071979970))    
    dv01
} 


testImpliedVol <- function() 
{
    strike <- calc$calcStrike(
        underlyingPrice = underlyingPrice,
        strikeStep = strikeStep,
        numStep = 0
    ) 
    checkSame(strike[[1]], c(113, 113, 113, 112))
        
    expectedImpliedVols <- c(0.089889357262646, 0.0897888676667087, 0.0876789327137552, 0.085294033876964)
    impliedVols <- calc$calcImpliedVol(
        valueDate = valueDate,
        underlyingPrice = underlyingPrice, 
        disc = discountRate, 
        expiry = expiry, 
        optionPrice = optionPrice,
        optionType = optionType,
        strike = strike[[1]],
        dateCountConvention = 'business')
    checkSame(impliedVols, expectedImpliedVols)    
    
    expectedImpliedVols <- c(0.0785045199255098, 0.0740984342978365, 0.0724620487327156, 0.071579036611042)
    impliedVols <- calc$calcImpliedVol(
        valueDate = valueDate,
        underlyingPrice = underlyingPrice, 
        disc = discountRate, 
        expiry = expiry, 
        optionPrice = optionPrice,
        optionType = optionType,
        strike = strike[[1]],
        dateCountConvention = 'calendar'
    )
    checkSame(impliedVols, expectedImpliedVols)    
    impliedVols
}


testBpImpliedVol <- function() 
{
    strike <- calc$calcStrike(
        underlyingPrice = underlyingPrice,
        strikeStep = strikeStep,
        numStep = 0) 
    checkSame(strike[[1]], c(113, 113, 113, 112))
    
    impliedVol <- testImpliedVol()        
    dv01 <- testDV01()
    expectedBpImpliedVols <- c(130.328354013987, 123.013642860486, 120.297016640279, 114.188136848564)
    
    bpImpliedVols <- calc$calcBpImpliedVol(
        impliedVol = impliedVol,
        strike = strike[[1]],
        optionPrice = optionPrice,
        underlyingPrice = underlyingPrice,
        dv01 = dv01,
        underlyingDV01 = underlyingDV01)
    checkSame(bpImpliedVols, expectedBpImpliedVols)    
}


testFutOptTicker <- function() 
{
    ticker <- getBlbgFutOptTicker('TY', '2008/5/23', 112, 'call')
     checkSame(ticker, "TYK8C 112.00000 Comdty")    
}


testImpliedVolsFailureConditions <- function() 
{
    shouldBombMatching(calc$calcImpliedVol(
        valueDate = "2007/11/30",
        ticker = "test", 
        underlyingPrice = c(101, 102), 
        disc = 0.04, 
        expiry = "2007/10/11",
        optionPrice = 1.2,
        optionType = "call",
        strike = 100,
        dateCountConvention = "calendar"
    ), "must match all")

    shouldBombMatching(calc$calcImpliedVol(
        valueDate = "2007/11/30",
        ticker = "test", 
        underlyingPrice = 101, 
        disc = 0.04, 
        expiry = "2007/10/11",
        optionPrice = 1.2,
        optionType = "c",
        strike = 100,
        dateCountConvention = "business"
    ), "must be 'call' or 'put'")

    shouldBombMatching(calc$calcImpliedVol(
        valueDate = "2007/11/30",
        ticker = "test", 
        underlyingPrice = "p", 
        disc = 0.04, 
        expiry = "2007/10/11",
        optionPrice = 1.2,
        optionType = "call",
        strike = 100,
        dateCountConvention = "business"
    ), "underlyingPrice is not numeric is character")

    shouldBombMatching(calc$calcImpliedVol(
        valueDate = "2007/11/30",
        ticker = "test", 
        underlyingPrice = 100, 
        disc = 0.04, 
        expiry = "2007/10/11",
        optionPrice = 1.2,
        optionType = "call",
        strike = 100,
        dateCountConvention = "dateCountConvention"
    ), "date count convention must be 'calendar' or 'business'")
}

testCalcMergedVol <- function() 
{
    putVol <- c(0.0853022779008148 , 
                0.0776080168922164 ,
                0.0772243197908720 ,
                0.0775549638513349 ,
                0.0742074571023784 ,
                0.0707919985172548 ,
                0.0708782038225618 ,
                0.0701391292918710 ,
                0.0698487643092289 ,
                0.0706033657366609 ,
                0.0724519683643480 ,
                0.0739414017071504 ,
                0.0762994822061895 ,
                0.0787803300160483 ,
                0.0813988087153702 ,
                0.0852767447619077 ,
                0.0893045717483566 ,
                0.0944003874878648 ,
                0.0979854856065212 ,
                0.1042216544011710 ,
                0.1097504333696920 )
        
    callVol <- c(0.1157911581082310, 
                0.1039441255060120 ,    
                0.0922112908447982 ,
                0.0836244692390119 ,
                0.0772483316374658 ,
                0.0736773706613902 ,
                0.0720497323033059 ,
                0.0703280620630771 ,
                0.0702836300070712 ,
                0.0712332178802949 ,
                0.0725528749284218 ,
                0.0742808532408458 ,
                0.0761235696096717 ,
                0.0788676867729388 ,
                0.0808014801462058 ,
                0.0837558393138885 ,
                0.0879706418719193 ,
                0.0917061072128726 ,
                0.0954134094550830 ,
                0.0972771359415801 ,
                0.1027403982237480 )
    
    strike <- c(103,
                104,
                105,
                106,
                107,
                108,
                109,
                110,
                111,
                112,
                113,
                114,
                115,
                116,
                117,
                118,
                119,
                120,
                121,
                122,
                123)
    
    mergedVol <- calc$calcMergedVol(underlyingPrice = 113.203125,
                                    strike = strike,
                                    putVol = putVol,
                                    callVol = callVol)
    expectedMergedVol = c(0.0853022779008148 ,
                         0.0776080168922164 ,
                         0.0772243197908720 ,
                         0.0775549638513349 ,
                         0.0742074571023784 ,
                         0.0707919985172548 ,
                         0.0708782038225618 ,
                         0.0701391292918710 ,
                         0.0698487643092289 ,
                         0.0706033657366609 ,
                         0.0724519683643480 ,
                         0.0742808532408458 ,
                         0.0761235696096717 ,
                         0.0788676867729388 ,
                         0.0808014801462058 ,
                         0.0837558393138885 ,
                         0.0879706418719193 ,
                         0.0917061072128726 ,
                         0.0954134094550830 ,
                         0.0972771359415801 ,
                         0.1027403982237480)    
    #print(mergedVol)
    #print(expectedMergedVol)                               
    checkSame(mergedVol, expectedMergedVol)                               
    mergedVol            
}

testCalcOTMPriceStrike <- function() 
{
    OTMPriceStrike = calc$calcOTMPriceStrike(underlyingPrice = 113.203125,
                                             strikeStep = 1,
                                             numStep = 10)
    checkSame(OTMPriceStrike, c(103.2031250000,
                                104.2031250000,
                                105.2031250000,
                                106.2031250000,
                                107.2031250000,
                                108.2031250000,
                                109.2031250000,
                                110.2031250000,
                                111.2031250000,
                                112.2031250000,
                                113.2031250000,
                                114.2031250000,
                                115.2031250000,
                                116.2031250000,
                                117.2031250000,
                                118.2031250000,
                                119.2031250000,
                                120.2031250000,
                                121.2031250000,
                                122.2031250000,
                                123.2031250000))        
OTMPriceStrike                                      
}

testCalcOTMStrikeVolLinear <- function() 
{
    vol <- testCalcMergedVol()        
    OTMPriceStrike <- testCalcOTMPriceStrike()        
    strike <- c(103,
                104,
                105,
                106,
                107,
                108,
                109,
                110,
                111,
                112,
                113,
                114,
                115,
                116,
                117,
                118,
                119,
                120,
                121,
                122,
                123)
    
    OTMStrikeVolLinear <- calc$calcOTMStrikeVolLinear(strike = strike,
                                                      vol = vol,
                                                      OTMPriceStrike = OTMPriceStrike)
    checkSame(OTMStrikeVolLinear, c( 0.08373938113344 ,
                                     0.07753007841851 ,
                                     0.07729148186565 ,
                                     0.07687500154295 ,
                                     0.07351369207728 ,
                                     0.07080950896990 ,
                                     0.07072807930852 ,
                                     0.07008014890477 ,
                                     0.07000204272418 ,
                                     0.07097886314541 ,
                                     0.07282346060489 ,
                                     0.07465515500326 ,
                                     0.07668096840846 ,
                                     0.07926048855188 ,
                                     0.08140158435214 ,
                                     0.08461197108349 ,
                                     0.08872940826930 ,
                                     0.09245915298082 , 
                                     0.09579197889765 ,
                                     0.09838686109265 ,
                                     0.10274039822375))
}


testDelta <- function() 
{
    strike <- c(113, 113, 113, 112)
    vol <- c(0.0785498740785, 0.0741622060572, 0.0725528749284, 0.0714021718848)
	prices <- c(0.750487361257953, 1.204242969975968,1.455066948696048, 1.900898218579187)     
    expectedDelta <- c(-0.457115357055514, -0.466714307697513, -0.467405135280602, -0.440261217752471)
	
	
    delta <- FuturesOptionCalculator$calcDelta(
        valueDate = valueDate,
        underlyingPrice = underlyingPrice, 
        disc = discountRate, 
        expiry = expiry, 
        vol = vol,
        optionType = optionType,
        strike = strike,
        dateCountConvention = "calendar")        
    checkSame(delta, expectedDelta)
	
	delta <- FuturesOptionCalculator$calcDelta(
		valueDate = valueDate,
		underlyingPrice = underlyingPrice,
		disc = discountRate,
		expiry = expiry, 
		price = prices,
		optionType = optionType,
		strike = strike,
		dateCountConvention = 'calendar',
		haveValue = 'price')
	checkSame(delta, expectedDelta)
}


testDeltaWeight <- function() 
{
     delta <- c(-0.0415310648132,
                -0.0417798473584,
                -0.0420278224105,
                -0.0480531302121,
                -0.0612143450266,
                -0.0877408066310,
                -0.1316683248889,
                -0.1907481705867,
                -0.2714193189106,
                -0.3668509468691,
                -0.4674744710341,
                -0.5649781937921,
                -0.6531778078234,
                -0.7265935417459,
                -0.7887426306911,
                -0.8354488540998,
                -0.8681128888837,
                -0.8941750694636,
                -0.9141543075344,
                -0.9329673599628,
                -0.9413722516846)

    weight <- calc$calcDeltaWeight(delta = delta)        
    checkSame(weight, c(0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0))
}


testDeltaVolQuadRegressionCoef <- function() 
{
    vol = c(0.11579115810823, 0.10394412550601, 0.09221129084480, 0.08362446923901, 
        0.07724833163747, 0.07367737066139, 0.07204973230331, 0.07032806206308, 0.07028363000707, 
        0.07123321788029, 0.07255287492842, 0.07428085324085, 0.07612356960967, 0.07886768677294, 
        0.08080148014621, 0.08375583931389, 0.08797064187192, 0.09170610721287, 0.09541340945508, 
        0.09727713594158, 0.10274039822375)
                             
    delta <- c( -0.0415310648132,
                -0.0417798473584,
                -0.0420278224105,
                -0.0480531302121,
                -0.0612143450266,
                -0.0877408066310,
                -0.1316683248889,
                -0.1907481705867,
                -0.2714193189106,
                -0.3668509468691,
                -0.4674744710341,
                -0.5649781937921,
                -0.6531778078234,
                -0.7265935417459,
                -0.7887426306911,
                -0.8354488540998,
                -0.8681128888837,
                -0.8941750694636,
                -0.9141543075344,
                -0.9329673599628,
                -0.9413722516846)

    coef <- calc$calcDeltaVolQuadRegressionCoef(vol = vol, delta = delta)        
    checkSame(coef, c(0.07597177492497490, 0.0364264637251537, 0.0571606200098714))
    coef
}


testVolQuadractic <- function() 
{
    delta <- c(-0.10, -0.20, -0.30, -0.40, -0.50, -0.60, -0.70, -0.80, -0.90)
    coef <- testDeltaVolQuadRegressionCoef()
    vol <- calc$calcVolQuadractic(delta = delta, coef = coef)
    checkSame(vol, c( 0.072900734752558 ,  0.070972906980339 ,  0.070188291608317 ,  
        0.070546888636493 ,  0.072048698064866 ,  0.074693719893436 ,  0.078481954122204 ,  
        0.083413400751170 ,  0.089488059780333))
}


testPerpetualExpiryWeight <- function() 
{
    expiry <- c("2007/12/21", "2008/01/25")
    weight <- calc$calcPerpetualExpiryWeight(valueDate = "2007/11/30", perpetualDistance = 30,
        expiry = expiry, dateCountConvention = "calendar")
    
    checkSame(weight, c(0.742857143, 0.257142857))
}


testPerpetualExpiryVol <- function() 
{
    vol1 <- c(0.170426045964559, 0.155050459611933, 0.139660105613825, 0.124229278063976, 
        0.108724657482073, 0.093101051145138, 0.077293419521629, 0.079800291800854, 0.075091704704581, 
        0.076681444158885, 0.078502065023743, 0.081923755671444, 0.085511505327396, 0.089464641271097, 
        0.096904701191704, 0.099963702559955, 0.108240343028119, 0.111062223744006, 0.124508483033476, 
        0.137658675445907, 0.150542957415391)
        
    vol2 <- c( 0.127721155791849, 0.114654060415485, 0.101712751210466, 0.088894754572999, 
        0.081071313760134, 0.075694895035305, 0.072694779656431, 0.072163244900642, 0.071955281497012, 
        0.072407234495709, 0.074162206057151, 0.076138486242149, 0.079193010619694, 0.081778026441775, 
        0.085481182197862, 0.089595481852259, 0.094075477030300, 0.098038990246608, 0.102376641899766, 
        0.108497327583590, 0.113416211970865)
    
    expectedVol <- c(0.1605335041534240, 0.1457362346158770, 0.1309566914704360, 0.1161742838830720, 
        0.1023300473946360, 0.0889510945957262, 0.0761374451030474, 0.0779080146418743, 
        0.0742978426564840, 0.0756054437385196, 0.0774093432535601, 0.0804758467955959, 
        0.0839321924457711, 0.0875525611047967, 0.0940997709924168, 0.0974030566962307, 
        0.1047809997411300, 0.1078636770047170, 0.1192105235395150, 0.1307825583498470, 0.1419267282742410)
    
    vol <- calc$calcPerpetualExpiryVol(weight1 = 0.742857143, weight2 = 0.257142857,
        vol1 = vol1, vol2 = vol2)    
        
    checkSame(vol, expectedVol)    
}

testActualOptTRI <- function()
{
    underlyingPrice <- c(108.875, 109.15625, 109.015625, 109.1875, 108.328125, 108.234375)
    disc <- rep(0.04, length(underlyingPrice))
    expiry <- rep("2008/02/22", length(underlyingPrice))
    optionPrice <- c(0.3125, 0.3125, 0.265625, 0.28125, 0.15625, 0.125)
    optionType <- rep("call", length(underlyingPrice))
    strike <- rep(114, length(underlyingPrice))
    valueDate <- c("2007/10/1", "2007/10/2", "2007/10/3", "2007/10/4", "2007/10/5", "2007/10/9")
    
    TRI <- calc$calcActualOptTRI(
        valueDate = valueDate,
        underlyingPrice = underlyingPrice,
        disc = disc,
        expiry = expiry,
        optionPrice = optionPrice,
        optionType = optionType,
        strike = strike,
        dateCountConvention = 'calendar')
        
    checkSame(TRI, c(-0.03868834757817, -0.02694768882372, -0.00625720564709, 
        -0.00980377910293, -0.02320794757529))  
}
