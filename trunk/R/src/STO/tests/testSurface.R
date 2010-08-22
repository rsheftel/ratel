library("STO")

source(system.file("testHelper.r", package = "STO"))

testSurface <- function() {
    sto <- STO(stoDirectory(), "SurfaceSTO")
    surface <- sto$surface(the(sto$msivs()), "a", "b", NetProfit, sum);
    checkInherits(surface, "Surface")
    params <- sto$parameters()
    checkShape(surface, rownames = uniqueValues(params, "a"), colnames = uniqueValues(params, "b"))
    checkSame(as.vector(surface$row(1)), c(50, 60, 70, 80))
    checkSame(as.vector(surface$row(2)), c(500, 600, 700, 800))
    checkSame(as.vector(surface$column(7)), c(70, 700, 7000, 70000)) # c^a * b
    checkSame(surface$point(2,7), 700)
	
	tempo <- c(10,100,1000,10000)
	expected <- data.frame('5'=tempo*5,'6'=tempo*6,'7'=tempo*7,'8'=tempo*8,check.names=FALSE)
	checkSameLooking(as.data.frame(surface), expected)
}

testSurfaceWithFilter <- function() {
    sto <- STO(stoDirectory(), "SurfaceSTO")
    filter <- sto$parameters()$filter(a == 1 & b == 5)
    surface <- sto$surface(the(sto$msivs()), "a", "b", NetProfit, sum, filter);
    checkInherits(surface, "Surface")
    params <- sto$parameters()
    checkShape(surface, rownames = 1, colnames = 5)
    checkSame(surface$point(1,5), 50)
}

testSurfaceTakesMetricCube <- function() {
    sto <- STO(stoDirectory(), "SurfaceSTO")
    filter <- sto$parameters()$filter(a == 1 & b == 5)
    metrics <- sto$metrics()
    surface <- sto$surface(the(sto$msivs()), "a", "b", NetProfit, sum, filter, metrics);
    checkInherits(surface, "Surface")
    params <- sto$parameters()
    checkShape(surface, rownames = 1, colnames = 5)
    checkSame(surface$point(1,5), 50)
}

