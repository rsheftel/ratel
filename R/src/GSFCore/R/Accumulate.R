accumulate <- function(op, l, allowMultipleTypes = FALSE) {
    op <- match.fun(op)
    assert(length(l) > 0)
    if (length(l) == 1) return(l[[1]])
    if(!allowMultipleTypes) requireAllMatchFirst(class, l)
    result <- l[[1]]
    env <- environment()
    lapply(l[2:length(l)], function(a) { assign("result", op(result, a), envir=env); })
    return(result)
}
