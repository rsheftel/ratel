library("GSFCore")
library("rgl")

tenors <- c("18m" = 1.5, "2y" = 2, "3y" = 3, "4y" = 4, "5y" = 5, "6y" = 6, "7y" = 7, "8y" = 8, "9y" = 9, "10y" = 10, "12y" = 12, "15y" = 15, "20y" = 20, "25y" = 25, "30y" = 30, "40y" = 40)
swap.data <- do.call(merge.zoo, tsdb$retrieveTimeSeriesByAttributeList(list(instrument="irs", ccy="usd"), data.source="internal", start = "2007-08-01", arrange.by = "tenor"))[,names(tenors)]
colnames(swap.data) <- tenors

surface3d(tenors * 30000, index(swap.data), t(swap.data) * 1000000, back="fill", color="red")
surface3d(tenors * 30000, index(swap.data), t(swap.data) * 1000000, front="lines", color="black", size=3)
rgl.bbox(color = c("lightgray", "black"), xat = c(2,seq(0,40,5)) * 30000, xlab = c(2, seq(0,40,5)), yat = index(swap.data), ylab = gsub(" .*", "", as.character(index(swap.data))), zat = seq(4.8, 6.0, 0.2) * 1e6, zlab = seq(4.8, 6.0, 0.2))
title3d("Swap Curve: 8/1/07-8/10/07", NULL, "tenor", "date", color = "black", line=3)
