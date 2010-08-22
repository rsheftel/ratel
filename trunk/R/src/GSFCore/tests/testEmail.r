library("GSFCore")

testEmail <- function() {
    emailer <- JMockEmailer$by()
    on.exit(emailer$reset())
    shouldBombMatching(
        Mail$notification("test subject", "test message")$sendTo("team"),
        "unexpected messages sent"
    )
    emailer$clear()
    emailer$allowMessages()
    Mail$notification("test subject", "test message")$sendTo("team") 
    emailer$requireSent_by_int(1)
    
    filename <- system.file("testdata/time_series_defs.csv", package="GSFCore")
    email <- Mail$problem("test subject", "test message")$attachFile(filename)$sendTo("team")
    
    
}

functestEmailFunc <- function() {
    email <- Mail$problem("test subject", "test message")
    print(system.file("testdata/time_series_defs.csv", package="GSFCore"))
    email$sendTo("us")
    email$attachFile(system.file("testdata/time_series_defs.csv", package="GSFCore"))
    email$sendTo("us")
}

