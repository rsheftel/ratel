library("GSFCore")


testCanCreateServer <- function() { 
    a <- "a"
    server <- Server(9999, textConnection(a), textConnection("res", "w", local=TRUE))
    checkInherits(server, "Server")
    checkSame(13, server$runOne(c("a <- 7", "b <- 6", "a + b")))
}

testCanTellServerToQuit <- function() { 
    cmds <- "a <- 7\nb<-6\na+b\nend\nquit\n"
    outConn <- textConnection("res", "w", local=TRUE)
    server <- Server(9999, textConnection(cmds), outConn)
    server$run()
    checkNull(server$.connection)
    checkSame(13, unserialize(textConnection(res)))
}

testHandlesErrors <- function() { 
    cmds <- "print(hello)\nend\nquit\n"
    outConn <- textConnection("res", "w", local=TRUE)
    server <- Server(9999, textConnection(cmds), outConn)
    server$run()
    checkNull(server$.connection)
    checkInherits(unserialize(textConnection(res)), "error")
}

testCanSubmitCommandsToServer <- function() { 
    cmds <- c("a <- 7\nb <- 6\na + b\n")
    conn <- textConnection(cmds)
    server <- Server(9999, conn, textConnection("res", "w", local=TRUE))
    toRun <- server$readCmds()
    checkLength(toRun, 3)
    checkSame(toRun, c("a <- 7", "b <- 6", "a + b"))
    checkTrue(isOpen(server$connection()))
}

testScoping <- function() { 
    a <- "a"
    server <- Server(9999, textConnection(a), textConnection("res", "w", local=TRUE))
    checkInherits(server, "Server")
    checkSame(13, server$runOne(c("a <- 7", "b <- 6", "a + b")))
    checkSame(7, server$runOne(c("a")))
}

