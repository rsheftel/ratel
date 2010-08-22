library("GSFCore")



testClient <- function() { 
    client <- Client("nyux51", 9999, textConnection("cmd", "w", TRUE))
    client$run(print('hello'))
    checkSame(c('print("hello")', "end"), cmd)
}

testCurlies <- function() {
    client <- Client("nyux51", 9999, textConnection("cmd", "w", TRUE))
    client$run({
        print('hello')
        print('world')
    })
    checkSame(c('{', '    print("hello")', '    print("world")', '}', "end"), cmd)
}
