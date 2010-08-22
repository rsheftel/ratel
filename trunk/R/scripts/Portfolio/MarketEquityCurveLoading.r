


rm(list=ls())
library(QFPortfolio)

getMonthlyReturns <- function(z){
	res = changeZooFrequency(cumsum(go),'monthly')
	zoo(c(first(res),diff(res)),c(first(index(res)),index(diff(res))))
}

dirName <- squish('//nyux51/data/STProcess/RightEdge/Portfolio/20090406/curves')
range <- Range("1990-01-03", "2009-04-01")
systemCurves <- CurveGroup("FXCarry")$childCurves(dirName, extension = "bin", range = range)




dirName <- squish(dataDirectory(),"/tempPerformanceDB")
range <- Range("1990-01-03", "2009-03-20")
systemCurves <- CurveGroup("G7_CARRY_START")$childCurves(dirName, extension = "bin", range = range)

# CVE (Done)

hypo <- read.table('f.cve.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))
desk <- read.table('f.cve.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
go <- read.table('f.cve.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot(na.omit(m),plot.type = 'single',col = 1:3,ylab = '');grid()

plot(na.omit(m)[,3]-na.omit(m[,2]),plot.type = 'single',col = 1:3,ylab = '',main = 'slippage');grid()


# LiqInj (Done)

desk <- read.table('f.liqinj.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
hypo <- read.table('f.liqinj.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))
go <- read.table('f.liqinj.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot(na.omit(m),plot.type = 'single',col = 1:3,ylab = '');grid()

sectorETFs.hypo <- read.table('sectorETFs.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
sectorETFs.hypo <- zoo(sectorETFs.hypo[,2],as.POSIXct(sectorETFs.hypo[,1]))
esty.hypo <- read.table('esty.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
esty.hypo <- zoo(esty.hypo[,2],as.POSIXct(esty.hypo[,1]))
esig.hypo <- read.table('esig.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
esig.hypo <- zoo(esig.hypo[,2],as.POSIXct(esig.hypo[,1]))

m.temp <- merge(esig.hypo,esty.hypo,sectorETFs.hypo)
m.temp[is.na(m.temp[,1]),1] <- 0;m.temp[is.na(m.temp[,2]),2] <- 0; m.temp[is.na(m.temp[,3]),3] <- 0
hypo <- m.temp[,1] + m.temp[,2] + m.temp[,3]



# DTD (Done)

desk <- read.table('f.dtd.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
hypo <- read.table('f.dtd.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))
go <- read.table('f.dtd.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
m <- m[index(m)<as.POSIXct('2008-11-01'),]
plot(na.omit(m),plot.type = 'single',col = 1:3,ylab = '');grid()

# NDaybreak (Done)

desk <- read.table('f.ndaybreak.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
go <- read.table('f.ndaybreak.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))
hypo <- read.table('f.ndaybreak.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot(na.omit(m),plot.type = 'single',col = 1:3,ylab = '');grid()

fx.hypo <- read.table('fx.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
fx.hypo <- zoo(fx.hypo[,2],as.POSIXct(fx.hypo[,1]))
energy.hypo <- read.table('energy.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
energy.hypo <- zoo(energy.hypo[,2],as.POSIXct(energy.hypo[,1]))
bond.hypo <- read.table('bond.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
bond.hypo <- zoo(bond.hypo[,2],as.POSIXct(bond.hypo[,1]))

m.temp <- merge(bond.hypo,energy.hypo,fx.hypo)
m.temp[is.na(m.temp[,1]),1] <- 0;m.temp[is.na(m.temp[,2]),2] <- 0; m.temp[is.na(m.temp[,3]),3] <- 0
hypo <- m.temp[,1]+m.temp[,2]+m.temp[,3]

go.credit <- read.table('credit.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go.credit <- zoo(go.credit[,2],as.POSIXct(go.credit[,1]))

m <- merge(m.temp,desk,go,go.credit)



# Straddle (Done)

desk <- read.table('f.straddle.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
hypo <- read.table('f.straddle.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))
go <- read.table('f.straddle.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot((m),plot.type = 'single',col = 1:3,ylab = '');grid()

# FX (Done)

desk <- read.table('f.g7.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
go <- read.table('f.g7.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))
hypo <- read.table('f.g7.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot(na.omit(m),plot.type = 'single',col = 1:3,ylab = '');grid()

starthypo <- read.table('startfx.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
starthypo <- zoo(starthypo[,2],as.POSIXct(starthypo[,1]))
singlehypo <- read.table('singlefx.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
singlehypo <- zoo(singlehypo[,2],as.POSIXct(singlehypo[,1]))

m.temp <- merge(starthypo,singlehypo)
m.temp[is.na(m.temp[,1]),1] <- 0;m.temp[is.na(m.temp[,2]),2] <- 0
hypo <- m.temp[,1]+m.temp[,2]



systemCurves
l <- NULL
for(i in 1:length(systemCurves)){
	l[[i]] <- systemCurves[[i]]$pnl()
}
names(l) <- names(systemCurves)
m <- do.call(merge,l)
m0 <- m[index(m) < as.POSIXct('2008-06-10') & index(m) >= as.POSIXct('2007-12-12'),]
m1 <- m[index(m) < as.POSIXct('2008-12-10') & index(m) >= as.POSIXct('2008-06-10'),]
m2 <- m[index(m) >= as.POSIXct('2008-12-10'),]

m <- merge(m0,m1*2,m2)

# QF.NDBrkClsMV (Done)

desk <- read.table('f.credit.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
hypo <- read.table('f.credit.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))
go <- read.table('f.credit.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot((m),plot.type = 'single',col = 1:3,ylab = '');grid()

# QF.FaderClose (Done)

desk <- read.table('f.fader.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
hypo <- read.table('f.fader.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))
go <- read.table('f.fader.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot(na.omit(m),plot.type = 'single',col = 1:3,ylab = '');grid()

fx.hypo <- read.table('f.fx.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
fx.hypo <- zoo(fx.hypo[,2],as.POSIXct(fx.hypo[,1]))
energy.hypo <- read.table('f.energy.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
energy.hypo <- zoo(energy.hypo[,2],as.POSIXct(energy.hypo[,1]))
eq.hypo <- read.table('f.equity.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
eq.hypo <- zoo(eq.hypo[,2],as.POSIXct(eq.hypo[,1]))
vol.hypo <- read.table('f.equityvol.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
vol.hypo <- zoo(vol.hypo[,2],as.POSIXct(vol.hypo[,1]))

m.temp <- merge(eq.hypo,energy.hypo,fx.hypo,vol.hypo)
hypo <- m.temp[,1]+m.temp[,2]+m.temp[,3]+m.temp[,4]



# QF.CPNSWAP_FD

desk <- read.table('f.couponfd.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
hypo <- read.table('f.couponfd.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))
go <- read.table('f.couponfd.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot((m),plot.type = 'single',col = 1:3,ylab = '');grid()

# QF.CPNSWAP_FN

desk <- read.table('cpn_fn.desk.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
desk <- zoo(desk[,2],as.POSIXct(desk[,1]))
hypo <- read.table('cpn_fn.hypo.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
hypo <- zoo(hypo[,2],as.POSIXct(hypo[,1]))
go <- read.table('cpn_fn.go.csv',sep = ',',header = FALSE,stringsAsFactors = FALSE)
go <- zoo(go[,2],as.POSIXct(go[,1]))

m <- merge(cumsum(desk),cumsum(go),cumsum(hypo))
plot((m),plot.type = 'single',col = 1:3,ylab = '');grid()