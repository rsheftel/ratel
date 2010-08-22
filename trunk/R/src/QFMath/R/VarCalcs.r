
histVaR <- function(x, weights=NULL,alpha=0.05, ...) {
# This routine is taken from fPortfolio. It performs a straight, historically-based statistical VaR estimation.

    x = as.matrix(x)
    if (is.null(weights)) 
        weights = rep(1/dim(x)[[2]], dim(x)[[2]])
    n = dim(x)[1]
    x = apply(t(t(x) * weights), 1, sum)
    n.alpha = max(floor(n * alpha))
    ans = as.vector(sort(x)[n.alpha])
    names(ans) = "VaR"
    ans
}

histDownsideVaR <- function(x, weights=NULL,alpha=0.05, ...) {
# This routine is taken from fPortfolio. It performs a straight, historically-based statistical VaR estimation.
	
	x = as.matrix(x)
	if (is.null(weights)) 
		weights = rep(1/dim(x)[[2]], dim(x)[[2]])
	n = dim(x)[1]
	x = apply(t(t(x) * weights), 1, sum)
	n.alpha = max(floor(length(which(x<0)) * alpha))
	ans = as.vector(sort(x)[n.alpha])
	names(ans) = "VaR"
	ans
}

expectedShortFallVaR <- function (x, weights = NULL, alpha = 0.05) {
# This is also taken from fPortfolio (it is called CVaRplus in that package.

    x = as.matrix(x)
    if (is.null(weights)) {
        weights = rep(1/dim(x)[[2]], dim(x)[[2]])
    }
    n = dim(x)[1]
    x = apply(t(t(x) * weights), 1, sum)
    n.alpha = max(1, floor(n * alpha) - 1)
    ans = as.vector(mean(sort(x)[1:n.alpha]))
    names(ans) = "ExpectedShortFall"
    ans
}


expectedShortFallDownsideVaR <- function (x, weights = NULL, alpha = 0.05) {
# This is also taken from fPortfolio (it is called CVaRplus in that package.
	
	x = as.matrix(x)
	if (is.null(weights)) {
		weights = rep(1/dim(x)[[2]], dim(x)[[2]])
	}
	x = apply(t(t(x) * weights), 1, sum)
	n.alpha = max(1, floor(length(which(x<0)) * alpha) - 1)
	ans = as.vector(mean(sort(x)[1:n.alpha]))
	names(ans) = "ExpectedShortFallDownside"
	ans
}

parametricVaR <- function(x, weights=NULL, alpha=0.05)
{
	x = as.matrix(x)
	if (is.null(weights)) 
		weights = rep(1/dim(x)[[2]], dim(x)[[2]])
	n = dim(x)[1]
	x = apply(t(t(x) * weights), 1, sum)
	p.var <- VaR.CornishFisher(as.matrix(x),alpha)
	return(p.var)
}
	
VaR.CornishFisher <- function (R, p = 0.99, modified = TRUE) 
{
	if (p >= 0.51) {
		p = 1 - p
	}
	zc = qnorm(p)
#	R = checkData(R, method = "matrix")
	assert(any(class(R)=="matrix"))
	
	columns = ncol(R)
	columnnames = colnames(R)
	for (column in 1:columns) {
		r = as.vector(na.omit(R[, column]))
		if (!is.numeric(r)) 
			stop("The selected column is not numeric")
		if (modified) {
			s = skewness(r)
			k = kurtosis(r)
			Zcf = zc + (((zc^2 - 1) * s)/6) + (((zc^3 - 3 * zc) * 
							k)/24) + (((2 * zc^3) - (5 * zc) * s^2)/36)
			
			VaR = mean(r) - (Zcf * sqrt(var(r)))
		}
		else {
			VaR = mean(r) - (zc * sqrt(var(r)))
		}
		VaR = array(VaR)
		if (column == 1) {
			result = data.frame(VaR = VaR)
		}	
		else {
			VaR = data.frame(VaR = VaR)
			result = cbind(result, VaR)
		}
	}
	if (ncol(result) == 1) {
		result = as.numeric(result)
	}
	else colnames(result) = columnnames
		result
}

skewness <- function (x, na.rm = FALSE, method = c("moment", "fisher"), ...) 
{
	method = match.arg(method)
	if (!is.numeric(x) && !is.complex(x) && !is.logical(x)) {
		warning("argument is not numeric or logical: returning NA")
		return(as.numeric(NA))
	}
	if (na.rm) 
		x = x[!is.na(x)]
	n = length(x)
	if (is.integer(x)) 
		x = as.numeric(x)
	if (method == "moment") {
		skewness = sum((x - mean(x))^3/sqrt(var(x))^3)/length(x)
	}
	if (method == "fisher") {
		if (n < 3) 
			skewness = NA
		else skewness = ((sqrt(n * (n - 1))/(n - 2)) * (sum(x^3)/n))/((sum(x^2)/n)^(3/2))
	}
	attr(skewness, "method") <- method
	skewness
}

kurtosis <- function (x, na.rm = FALSE, method = c("excess", "moment", "fisher"), 
	...) 
{
	method = method[1]
	if (!is.numeric(x) && !is.complex(x) && !is.logical(x)) {
		warning("argument is not numeric or logical: returning NA")
		return(as.numeric(NA))
	}
	if (na.rm) 
		x = x[!is.na(x)]
	n = length(x)
	if (is.integer(x)) 
		x = as.numeric(x)
	if (method == "excess") {
		kurtosis = sum((x - mean(x))^4/var(x)^2)/length(x) - 
			3
	}
	if (method == "moment") {
		kurtosis = sum((x - mean(x))^4/var(x)^2)/length(x)
	}
	if (method == "fisher") {
		kurtosis = ((n + 1) * (n - 1) * ((sum(x^4)/n)/(sum(x^2)/n)^2 - 
					(3 * (n - 1))/(n + 1)))/((n - 2) * (n - 3))
	}
	attr(kurtosis, "method") <- method
	kurtosis
}

