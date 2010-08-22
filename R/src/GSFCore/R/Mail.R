constructor("Mail", function(jmail = NULL) {
    this <- extend(RObject(), "Mail", .jmail = jmail)
    constructorNeeds(this, jmail="JEmail")
    this
})

method("problem", "Mail", function(static, subject, content, ...) {
    needs(subject="character", content="character")
    Mail(JEmail$problem_by_String_String(subject, content))
})

method("notification", "Mail", function(static, subject, content, ...) {
    needs(subject="character", content="character")
    Mail(JEmail$notification_by_String_String(subject, content))
})

method("sendTo", "Mail", function(this, address, ...) {
    needs(address="character")
    this$.jmail$sendTo_by_String(address)
})

method("attachFile", "Mail", function(this, filename, ...) {
    needs(filename="character")
    textfile <- JQFile$by_String(filename)
    this$.jmail$attach_by_QFile(textfile)
    this
})