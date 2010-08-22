constructor("MonthCode", function() {
    extend(RObject(), "MonthCode")
})

method("letter", "MonthCode", function(static, number, ...) {
    JMonthCode$fromNumber_by_int(number)$letter()        
})

method("number", "MonthCode", function(static, letter, ...) {
    JMonthCode$fromChar_by_String(letter)$number()        
})