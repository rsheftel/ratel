alter table IvyDB.dbo.Security add constraint pkSecurity primary key nonclustered (securityID)
go
alter table IvyDB.dbo.SecurityName add constraint pkSecurityName primary key nonclustered (securityID,effectiveDate)
go
alter table IvyDB.dbo.Exchange add constraint pkExchange primary key nonclustered (securityID,effectiveDate,sequenceNumber)
go
alter table IvyDB.dbo.Distribution add constraint pkDistribution primary key nonclustered (securityID,recordDate,sequenceNumber)
go
alter table IvyDB.dbo.OptionInfo add constraint pkOptionInfo primary key nonclustered (securityID)
go
alter table IvyDB.dbo.IndustryGroup add constraint pkIndustryGroup primary key nonclustered (classificationCode)
go
alter table IvyDB.dbo.SecurityPrice add constraint pkSecurityPrice primary key nonclustered (securityID,date)
go
alter table IvyDB.dbo.OptionPrice add constraint pkOptionPrice primary key nonclustered (date,root,suffix)
go
alter table IvyDB.dbo.ZeroCurve add constraint pkZeroCurve primary key nonclustered (date,days)
go
alter table IvyDB.dbo.IndexDividend add constraint pkIndexDividend primary key nonclustered (securityID,date)
go
alter table IvyDB.dbo.StdOptionPrice add constraint pkStdOptionPrice primary key nonclustered (securityID,date,days,callPutFlag)
go
alter table IvyDB.dbo.OptionVolume add constraint pkOptionVolume primary key nonclustered (securityID,date,callPutFlag)
go
alter table IvyDB.dbo.VolatilitySurface add constraint pkVolatilitySurface primary key nonclustered (securityID,date,days,delta,callPutFlag)
go
alter table IvyDB.dbo.HistoricalVolatility add constraint pkHistoricalVolatility primary key nonclustered (securityID,date,days)
go
alter table IvyDB.dbo.OpenInterest add constraint pkOpenInterest primary key nonclustered (date,root,suffix)
go

