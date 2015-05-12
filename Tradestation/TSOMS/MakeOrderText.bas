Attribute VB_Name = "MakeOrderText"
Option Explicit
Option Base 1

Function tsomsMakeLimitOrder(action As String, symbol As String, symbolCategory As String, limitPrice As String, quantity As String, duration As String, account As String) As String
'Helper make function for limit orders

Dim strOrder As String

    strOrder = ".PlaceOrder """
    strOrder = strOrder & "Action = '" & action & "',"
    strOrder = strOrder & "Symbol = '" & symbol & "',"
    strOrder = strOrder & "SymbolCategory = '" & symbolCategory & "',"
    strOrder = strOrder & "OrderType = 'Limit',"
    strOrder = strOrder & "LimitPrice = '" & limitPrice & "',"
    strOrder = strOrder & "Quantity = " & quantity & ","
    strOrder = strOrder & "Duration = '" & duration & "',"
    strOrder = strOrder & "Account = '" & account & "'"
        
    tsomsMakeLimitOrder = strOrder
    
End Function

Function tsomsMakeOrder(action As String, symbol As String, symbolCategory As String, stopPrice As String, limitPrice As String, quantity As String, duration As String, account As String) As String
'Helper make function for orders

Dim strOrder As String

    strOrder = ".PlaceOrder """
    strOrder = strOrder & "Action = '" & action & "',"
    strOrder = strOrder & "Symbol = '" & symbol & "',"
    strOrder = strOrder & "SymbolCategory = '" & symbolCategory & "',"
    strOrder = strOrder & "OrderType = 'Limit',"
    If limitPrice <> "" Then strOrder = strOrder & "LimitPrice = '" & limitPrice & "',"
    If stopPrice <> "" Then strOrder = strOrder & "StopPrice = '" & stopPrice & "',"
    strOrder = strOrder & "Quantity = " & quantity & ","
    strOrder = strOrder & "Duration = '" & duration & "',"
    strOrder = strOrder & "Account = '" & account & "'"
        
    tsomsMakeOrder = strOrder
    
End Function
