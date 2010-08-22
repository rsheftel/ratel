constructor("Array", function(rowKeyType = NULL, colKeyType = NULL, valType = NULL, rows = NULL, cols = NULL) {
    this <- extend(RObject(), "Array", 
        .data = NULL,
        .rows = rows,
        .cols = cols,
        .rowKeyType = rowKeyType, 
        .colKeyType = colKeyType, 
        .valType = valType
    )
    constructorNeeds(this, 
        rowKeyType="character", 
        colKeyType="character", 
        valType="character", 
        rows=xOrList(rowKeyType),
        cols=xOrList(colKeyType)
    )
    if(inStaticConstructor(this)) return(this)

    this$.data <- array(this$.baseObj(), c(length(rows), length(cols)), list(sapply(rows, as.character), sapply(cols, as.character)))

    this
})

xOrList <- function(type) {
    squish(type, "|list(", type, ")")
}

method(".emptyRows", "Array", function(this, ...) {
    if (this$isValueAtomic()) 
        emptyCells <- is.na
    else 
        emptyCells <- function(cells) sapply(cells, is.null)
    isRowEmpty <- function(row) all(emptyCells(row))

    apply(this$.data, 1, isRowEmpty)
})

method("emptyRowIndices", "Array", function(this, ...) {
    this$rownames()[this$.emptyRows()]
})

method("populatedRowIndices", "Array", function(this, ...) {
    this$rownames()[!this$.emptyRows()]
})

method("isValueAtomic", "Array", function(this, ...) {
    this$.valType %in% c("numeric", "integer", "factor", "character", "logical")
})

method("requireFull", "Array", function(this, ...) {
    assertFalse(any(apply(this$.data, c(1,2), is.null)))
})

method(".baseObj", "Array", function(this, ...) {
    if(this$isValueAtomic())
        return(as(NA, this$.valType))
    else
        return(list(NULL))
})

method("rownames", "Array", function(this, ...) {
    this$.rows
})

method("colnames", "Array", function(this, ...) {
    this$.cols
})

method("as.character", "Array", function(this, ...) {
    dataStr <- paste(
        sapply(
            1:length(this$.rows), 
            function(rownum) paste(sapply(this$.data[rownum,], as.character), collapse="\t")
        ), 
        collapse="\n"
    )
    squish(
        "Rows:\n", 
        paste(sapply(this$.rows, as.character), collapse="\n"), 
        "\nColumns:\n", 
        paste(sapply(this$.cols, as.character), collapse="\n"), 
        "\nData:\n", 
        dataStr
    )
})

method("fetch", "Array", function(this, row, col, ...) {
    needs(row=this$.rowKeyType, col=this$.colKeyType)
    rowi <- as.character(row) 
    coli <- as.character(col) 
    tryCatch(
        this$.data[[rowi, coli]], 
        error = function(e) {
            if (!all(rowi %in% rownames(this$.data))) 
               throw("subscript(", rowi, ") out of row bounds:\n", humanString(rownames(this$.data)))
            else if (!all(coli %in% colnames(this$.data)))
               throw("subscript(", coli, ") out of col bounds:\n", humanString(colnames(this$.data)))
            else throw(e)
        }
    )
})

method("set", "Array", function(this, row, col, val, ...) {
    needs(row=this$.rowKeyType, col=this$.colKeyType, val=this$.valType)
    this$.data[[as.character(row), as.character(col)]] <- val
})

method("fetchColumn", "Array", function(this, col, ...) {
    needs(col=this$.colKeyType)
    this$.data[,as.character(col), drop=FALSE]
})

method("setColumn", "Array", function(this, col, vals, ...) {
    needs(col=this$.colKeyType, vals=xOrList(this$.valType))
    assert(length(vals) == 1 || length(vals) == length(this$.rows))
    this$.data[,as.character(col)] <- vals
})

method("hasRow", "Array", function(this, row, ...) {
    needs(row=this$.rowKeyType)
    any(sapply(this$rownames(), function(r) isTRUE(all.equal(r, row))))
})

method("hasColumn", "Array", function(this, col, ...) {
    needs(col=this$.colKeyType)
    any(sapply(this$colnames(), function(c) isTRUE(all.equal(c, col))))
})

method("fetchRow", "Array", function(this, row, ...) {
    needs(row=this$.rowKeyType)
    this$.data[as.character(row),, drop=FALSE]
})

method("setRow", "Array", function(this, row, vals, ...) {
    needs(row=this$.rowKeyType, vals=xOrList(this$.valType))
    assert(length(vals) == 1 || length(vals) == length(this$.cols))
    this$.data[as.character(row),] <- vals
})

method("addRow", "Array", function(this, row, new.data = NULL, ...) {
    needs(row=this$.rowKeyType)
    this$.rows <- appendSlowly(this$.rows, row)
    new.row <- array(this$.baseObj(), c(1, length(this$.cols)), list(as.character(row), sapply(this$.cols, as.character)))
    this$.data <- rbind(this$.data, new.row)
    if(!is.null(new.data)) this$setRow(row, new.data)
})

method("isType", "Array", function(val, type, ...) {
    if(substr(type,1,5) != "Array") return(FALSE)
    inner.type <- innerType(type)
    if(is.null(inner.type)) return(TRUE)
    match.indices <- the(gregexpr(",", inner.type))
    if (match.indices[[1]] == -1) fail("couldn't find row/col/val separator")
    if (length(match.indices) != 2) fail("invalid inner type declaration: ", inner.type)
    rowKeyType <- substr(inner.type, 1, match.indices[[1]] - 1)
    colKeyType <- substr(inner.type, match.indices[[1]] + 2, match.indices[[2]] - 1)
    valType <- substr(inner.type, match.indices[[2]] + 2, nchar(inner.type)+1)
    val$.rowKeyType == rowKeyType && val$.colKeyType == colKeyType && val$.valType == valType
})
