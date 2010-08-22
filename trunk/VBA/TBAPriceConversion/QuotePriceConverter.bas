Attribute VB_Name = "QuotePriceConverter"
Option Explicit
Option Base 1

Public Type TBAPrice
    program As Variant
    coupon As Variant
    settleDate As Variant
    settleMonthYear As Variant
    price As Variant
End Type

Public Const couponStep = 0.5


Function quotesFromPrices(programs As Range, coupons As Range, settleDates As Range, prices As Range, frontSettles As Range, parCoupons As Range) As Variant

Dim count As Integer
Dim thisCoupon As TBAPrice
Dim resultVector() As Variant
Dim frontSettle As Variant, fnclFrontSettleMonthYear As Variant
Dim parCoupon As Variant

    ReDim resultVector(prices.Rows.count, 1)
    fnclFrontSettleMonthYear = getSettleMonthYear(getFrontSettle(frontSettles, "fncl"), 0)
    
    For count = 1 To prices.Rows.count
        With thisCoupon
            .program = programs(count).Value
            .coupon = coupons(count).Value
            .settleDate = settleDates(count).Value
            .settleMonthYear = getSettleMonthYear(.settleDate, 0)
            .price = prices(count).Value
        End With
                
        frontSettle = getFrontSettle(frontSettles, thisCoupon.program)
        parCoupon = getParCoupon(parCoupons, thisCoupon.program)
        
        If thisCoupon.settleMonthYear > fnclFrontSettleMonthYear Then
            resultVector(count, 1) = rollFromPrices(programs, coupons, settleDates, prices, thisCoupon, getSettleMonthYear(thisCoupon.settleDate, -1))
        End If
        
        If thisCoupon.settleMonthYear < fnclFrontSettleMonthYear Then
            resultVector(count, 1) = rollFromPrices(programs, coupons, settleDates, prices, thisCoupon, getSettleMonthYear(thisCoupon.settleDate, 1))
        End If
                
        If thisCoupon.settleMonthYear = fnclFrontSettleMonthYear Then
            Select Case thisCoupon.program
            Case "fncl"
                resultVector(count, 1) = quoteFromPricesFNCL(programs, coupons, settleDates, prices, parCoupon, thisCoupon)
            Case "fnci"
                resultVector(count, 1) = quoteFromPricesFNCI(programs, coupons, settleDates, prices, parCoupon, thisCoupon)
            Case "fglmc"
                resultVector(count, 1) = programSwapFromPrices(programs, coupons, settleDates, prices, thisCoupon, "fncl", thisCoupon.coupon)
            Case "gnsf"
                resultVector(count, 1) = programSwapFromPrices(programs, coupons, settleDates, prices, thisCoupon, "fncl", thisCoupon.coupon)
            Case "fgci"
                resultVector(count, 1) = programSwapFromPrices(programs, coupons, settleDates, prices, thisCoupon, "fnci", thisCoupon.coupon)
            Case Else
                resultVector(count, 1) = "NA"
            End Select
        End If
    Next

    quotesFromPrices = resultVector
End Function

Function quoteFromPricesFNCL(programs As Range, coupons As Range, settleDates As Range, prices As Range, parCoupon As Variant, thisCoupon As TBAPrice) As Variant
    
    On Error GoTo ErrorTrap
    
    If thisCoupon.coupon = parCoupon Then
        quoteFromPricesFNCL = thisCoupon.price
    End If
        
    If thisCoupon.coupon > parCoupon Then
        quoteFromPricesFNCL = thisCoupon.price - getPrice(programs, coupons, settleDates, prices, thisCoupon.program, (thisCoupon.coupon - couponStep), thisCoupon.settleMonthYear)
    End If
        
    If thisCoupon.coupon < parCoupon Then
        quoteFromPricesFNCL = getPrice(programs, coupons, settleDates, prices, thisCoupon.program, (thisCoupon.coupon + couponStep), thisCoupon.settleMonthYear) - thisCoupon.price
    End If
    
CleanExit:
    Exit Function
ErrorTrap:
    quoteFromPricesFNCL = "NA"
    Exit Function
End Function

Function quoteFromPricesFNCI(programs As Range, coupons As Range, settleDates As Range, prices As Range, parCoupon As Variant, thisCoupon As TBAPrice) As Variant
    
    On Error GoTo ErrorTrap
    
    If thisCoupon.coupon = parCoupon Then
        quoteFromPricesFNCI = programSwapFromPrices(programs, coupons, settleDates, prices, thisCoupon, "fncl", thisCoupon.coupon + couponStep)
    End If
        
    If thisCoupon.coupon > parCoupon Then
        quoteFromPricesFNCI = thisCoupon.price - getPrice(programs, coupons, settleDates, prices, thisCoupon.program, (thisCoupon.coupon - couponStep), thisCoupon.settleMonthYear)
    End If
        
    If thisCoupon.coupon < parCoupon Then
        quoteFromPricesFNCI = getPrice(programs, coupons, settleDates, prices, thisCoupon.program, (thisCoupon.coupon + couponStep), thisCoupon.settleMonthYear) - thisCoupon.price
    End If
    
CleanExit:
    Exit Function
ErrorTrap:
    quoteFromPricesFNCI = "NA"
    Exit Function
End Function

Function programSwapFromPrices(programs As Range, coupons As Range, settleDates As Range, prices As Range, thisCoupon As TBAPrice, otherProgram As String, otherCoupon As Variant) As Variant

On Error GoTo ErrorTrap
programSwapFromPrices = "NA"

    programSwapFromPrices = thisCoupon.price - getPrice(programs, coupons, settleDates, prices, otherProgram, otherCoupon, thisCoupon.settleMonthYear)

CleanExit:
    Exit Function
ErrorTrap:
    programSwapFromPrices = "NA"
    Exit Function

End Function

Function rollFromPrices(programs As Range, coupons As Range, settleDates As Range, prices As Range, thisCoupon As TBAPrice, otherSettleMonthYear As Variant) As Variant

Dim otherPrice As Variant

On Error GoTo ErrorTrap
rollFromPrices = "NA"
    
    otherPrice = getPrice(programs, coupons, settleDates, prices, thisCoupon.program, thisCoupon.coupon, otherSettleMonthYear)
    If thisCoupon.settleMonthYear < otherSettleMonthYear Then
        rollFromPrices = thisCoupon.price - otherPrice
    ElseIf thisCoupon.settleMonthYear > otherSettleMonthYear Then
        rollFromPrices = otherPrice - thisCoupon.price
    End If

CleanExit:
    Exit Function
ErrorTrap:
    rollFromPrices = "NA"
    Exit Function
    
End Function


Function getPrice(programs As Range, coupons As Range, settleDates As Range, prices As Range, program As Variant, coupon As Variant, settleMonthYear As Variant) As Variant

Dim count As Integer

On Error GoTo ErrorTrap

    getPrice = "NA"
    If Not (IsNumeric(settleMonthYear)) Then
        getPrice = "NA"
        Exit Function
    End If
    
    For count = 1 To prices.Rows.count
        If programs(count) = program Then
            If coupons(count) = coupon Then
                If getSettleMonthYear(settleDates(count), 0) = settleMonthYear Then
                    getPrice = prices(count)
                    Exit For
                End If
            End If
        End If
    Next
    
CleanExit:
    Exit Function
ErrorTrap:
    getPrice = "NA"
    Exit Function
End Function

Function getFrontSettle(frontSettles As Range, program As Variant) As Variant

Dim count As Integer
Dim result As Variant

    result = "NA"
    For count = 1 To frontSettles.Rows.count
        If frontSettles(count, 1) = program Then
            result = frontSettles(count, 2)
            Exit For
        End If
    Next
    getFrontSettle = result
End Function

Function getParCoupon(parCoupons As Range, program As Variant) As Variant

Dim count As Integer
Dim result As Variant

    result = "NA"
    For count = 1 To parCoupons.Rows.count
        If parCoupons(count, 1) = program Then
            result = parCoupons(count, 2)
            Exit For
        End If
    Next
    getParCoupon = result
End Function

Function getSettleMonthYear(settleDate As Variant, offsetMonths As Variant) As Variant
    If IsNumeric(settleDate) Then
        getSettleMonthYear = Int(settleDate / 100) + offsetMonths
    Else
        getSettleMonthYear = "NA"
    End If
End Function


Function priceFromQuotes(program As Variant, coupon As Variant, settleMonthYear As Variant, programs As Range, coupons As Range, settleDates As Range, quotes As Range, frontSettles As Range, parCoupons As Range) As Variant

Dim result As Variant
Dim frontSettle As Variant, fnclFrontSettleMonthYear As Variant
Dim parCoupon As Variant
Dim settleDate As Variant
Dim quote As Variant

On Error GoTo ErrorTrap

    quote = getPrice(programs, coupons, settleDates, quotes, program, coupon, settleMonthYear)
    priceFromQuotes = "NA"
    fnclFrontSettleMonthYear = getSettleMonthYear(getFrontSettle(frontSettles, "fncl"), 0)
    settleDate = settleMonthYear * 100
                
    frontSettle = getFrontSettle(frontSettles, program)
    parCoupon = getParCoupon(parCoupons, program)
        
    If settleMonthYear > fnclFrontSettleMonthYear Then
        priceFromQuotes = priceFromQuotes(program, coupon, getSettleMonthYear(settleDate, -1), programs, coupons, settleDates, quotes, frontSettles, parCoupons) - quote
    End If
        
    If settleMonthYear < fnclFrontSettleMonthYear Then
        priceFromQuotes = priceFromQuotes(program, coupon, getSettleMonthYear(settleDate, 1), programs, coupons, settleDates, quotes, frontSettles, parCoupons) + quote
    End If
                
    If settleMonthYear = fnclFrontSettleMonthYear Then
        Select Case program
        Case "fncl"
            priceFromQuotes = priceFromQuotesFNCL(programs, coupons, settleDates, quotes, frontSettles, parCoupons, parCoupon, program, coupon, settleMonthYear, quote)
        Case "fnci"
            priceFromQuotes = priceFromQuotesFNCI(programs, coupons, settleDates, quotes, frontSettles, parCoupons, parCoupon, program, coupon, settleMonthYear, quote)
        Case "fglmc"
            priceFromQuotes = priceFromQuotes("fncl", coupon, settleMonthYear, programs, coupons, settleDates, quotes, frontSettles, parCoupons) + quote
        Case "gnsf"
            priceFromQuotes = priceFromQuotes("fncl", coupon, settleMonthYear, programs, coupons, settleDates, quotes, frontSettles, parCoupons) + quote
        Case "fgci"
            priceFromQuotes = priceFromQuotes("fnci", coupon, settleMonthYear, programs, coupons, settleDates, quotes, frontSettles, parCoupons) + quote
        Case Else
            priceFromQuotes = "NA"
        End Select
    End If

CleanExit:
    Exit Function
ErrorTrap:
    priceFromQuotes = "NA"
    Exit Function
End Function

Function priceFromQuotesFNCL(programs As Range, coupons As Range, settleDates As Range, quotes As Range, frontSettles As Range, parCoupons As Range, parCoupon As Variant, program As Variant, coupon As Variant, settleMonthYear As Variant, quote As Variant) As Variant
    
    On Error GoTo ErrorTrap
    
    If coupon = parCoupon Then
        priceFromQuotesFNCL = getPrice(programs, coupons, settleDates, quotes, program, coupon, settleMonthYear)
    End If

    If coupon > parCoupon Then
        priceFromQuotesFNCL = quote + priceFromQuotes(program, (coupon - couponStep), settleMonthYear, programs, coupons, settleDates, quotes, frontSettles, parCoupons)
    End If

    If coupon < parCoupon Then
        priceFromQuotesFNCL = priceFromQuotes(program, (coupon + couponStep), settleMonthYear, programs, coupons, settleDates, quotes, frontSettles, parCoupons) - quote
    End If
    
CleanExit:
    Exit Function
ErrorTrap:
    priceFromQuotesFNCL = "NA"
    Exit Function
End Function


Function priceFromQuotesFNCI(programs As Range, coupons As Range, settleDates As Range, quotes As Range, frontSettles As Range, parCoupons As Range, parCoupon As Variant, program As Variant, coupon As Variant, settleMonthYear As Variant, quote As Variant) As Variant
    
    On Error GoTo ErrorTrap
    
    If coupon = parCoupon Then
        priceFromQuotesFNCI = priceFromQuotes("fncl", (coupon + couponStep), settleMonthYear, programs, coupons, settleDates, quotes, frontSettles, parCoupons) + quote
    End If
        
    If coupon > parCoupon Then
        priceFromQuotesFNCI = quote + priceFromQuotes(program, (coupon - couponStep), settleMonthYear, programs, coupons, settleDates, quotes, frontSettles, parCoupons)
    End If
        
    If coupon < parCoupon Then
        priceFromQuotesFNCI = priceFromQuotes(program, (coupon + couponStep), settleMonthYear, programs, coupons, settleDates, quotes, frontSettles, parCoupons) - quote
    End If
    
CleanExit:
    Exit Function
ErrorTrap:
    priceFromQuotesFNCI = "NA"
    Exit Function
End Function

