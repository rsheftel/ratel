constructor("RunFilter", function(name = NULL, runs = NULL, ...) {
    this <- extend(RObject(), "RunFilter", .runs = runs, .name = name) 
    if (inStaticConstructor(this)) return(this)
    constructorNeeds(this, runs = "numeric|integer", name = "character")
    this
})

method("with", "RunFilter", function(staticClass, name, runs, ...) {
    needs(name = "character", runs = "numeric|integer")
    RunFilter(name, runs)
})

method("runs", "RunFilter", function(this, ...) {
    this$.runs
})

method("name", "RunFilter", function(this, ...) {
    this$.name
})

method("where", "RunFilter", function(this, toBeFiltered, ...) {
    res <- match(this$runs(), toBeFiltered)
    failIf(any(is.na(res)), "toBeFiltered must be a super set of this$runs()")
    res
})

method("isEmpty", "RunFilter", function(this, ...) {
    length(this$runs()) == 0
})

method("as.character", "RunFilter", function(this, ...) {
    this$name()
})

method("and", "RunFilter", function(this, that, ...) {
    needs(that="RunFilter")
    RunFilter$with(squish(this$name(), " & ", that$name()), intersect(this$runs(), that$runs()))
})

method("andAll", "RunFilter", function(static, filters, ...) {
    needs(filters="list(RunFilter)")
    if(length(filters) == 1) return(the(filters))
    filter <- first(filters)
    for(i in 2:length(filters)) filter <- filter$and(filters[[i]])
    filter
})

method("cross", "RunFilter", function(static, dimensions, ...) { 
    needs(dimensions="list(list(RunFilter))")
    indices <- lapply(dimensions, seq_along)
    filterGrid <- expand.grid(indices)
    filters <- function(indexRow) { lapply(seq_along(indexRow), function(i) { j <- indexRow[[i]]; dimensions[[i]][[j]] } ) }
    lapply(1:nrow(filterGrid), function(i) RunFilter$andAll(filters(filterGrid[i,])))
})
