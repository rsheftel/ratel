japply <- function(i, contents.proto, func, ...) {
	needs(i="JIterator", contents.proto="JObject", func="function")
	
	func <- match.fun(func)
	
	l <- vector("list", 1)
	count <- 0
	while(i$hasNext()) {
	    o <- i$j_next()
	    count <- count + 1
	    class(o) <- class(contents.proto)
	    if(count > length(l))
	        length(l) <- length(l) * 2
	    l[[count]] <- func(o, ...)
    }
    
    length(l) <- count
    l
}
