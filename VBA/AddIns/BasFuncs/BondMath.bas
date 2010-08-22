Attribute VB_Name = "BondMath"
Option Explicit

Function Convexity(Settle As Date, Maturity As Date, Coupon As Double, Yield As Double, Redemption As Double, Freq As Integer, Basis As Integer) As Double
'Calculates modified convexity for a fixed coupon, non-amortizing bond
'Result - The modified convexity in decimal form
'Variables:
'Settle     - Settlement in date form
'Maturity   - Maturity in date form
'Coupon     - Coupon in decimal (0.06) for a 6% coupon
'Yield      - The yield in decimal form. Periods per year is Freq
'Redemption - The amount of par value as percent (usually 100)
'Freq       - The number of payments per year (same as yield and other Excel function)
'Basis      - Day count convention (same as yield and other Excel functions)


Dim T As Double, n As Double
Dim PVCF As Double, TPVCF As Double
Dim Numerator As Double

TPVCF = 0  'Initialize Total PV(CashFlow) as zero
Numerator = 0 'Use this to add up the top values

n = Application.Run("yearfrac", Settle, Maturity, Basis) * Freq
T = n - Int(n)

Do While (T < n)
    PVCF = (Coupon / Freq) / (1 + Yield / Freq) ^ T
    TPVCF = TPVCF + PVCF
    Numerator = Numerator + (T * (T + 1) * PVCF)
    T = T + 1
Loop

'Do the last cash flow date
    PVCF = (Redemption / 100 + Coupon / Freq) / (1 + Yield / Freq) ^ T
    TPVCF = TPVCF + PVCF
    Numerator = Numerator + (T * (T + 1) * PVCF)

'Now do the math

Convexity = (1 / (1 + Yield / Freq) ^ 2) * Numerator / (Freq ^ 2 * TPVCF) / 100

End Function
