/*
select 'Security',count(*) from IvyDB.dbo.Security --31681
select 'IndustryGroup',count(*) from IvyDB.dbo.IndustryGroup --247
select 'SecurityName',count(*) from IvyDB.dbo.SecurityName --99014
select 'Exchange',count(*) from IvyDB.dbo.Exchange --57570
select 'Distribution',count(*) from IvyDB.dbo.Distribution --220530
select 'SecurityPrice',count(*) from IvyDB.dbo.SecurityPrice --22966427
select 'OptionPrice',count(*) from IvyDB.dbo.OptionPrice --328887942
select 'ZeroCurve',count(*) from IvyDB.dbo.ZeroCurve --113646
select 'IndexDividend',count(*) from IvyDB.dbo.IndexDividend --212495
select 'OptionVolume',count(*) from IvyDB.dbo.OptionVolume --19073543
select 'VolatilitySurface',count(*) from IvyDB.dbo.VolatilitySurface --1604961020
select 'HistoricalVolatility',count(*) from IvyDB.dbo.HistoricalVolatility --73011495
select 'OpenInterest',count(*) from IvyDB.dbo.OpenInterest --0
select 'OptionInfo',count(*) from IvyDB.dbo.OptionInfo --5742


*/
/*
truncate table dbo.Security
exec IvyDB.dbo.sp_spaceused OptionPrice
backup log IvyDB with no_log
dbcc SQLPERF(LOGSPACE)
*/
/*
dbcc CHECKDB('IvyDB') WITH NO_INFOMSGS, ALL_ERRORMSGS
*/
/*
use IvyDB
select
'drop table ' + TABLE_NAME + char(13) 
from INFORMATION_SCHEMA.TABLES

drop table Security
drop table IndustryGroup
drop table SecurityName
drop table Exchange
drop table Distribution
drop table SecurityPrice
drop table ZeroCurve
drop table IndexDividend
drop table StdOptionPrice
drop table OptionVolume
drop table VolatilitySurface
drop table HistoricalVolatility
drop table OpenInterest
drop table OptionInfo
drop table OptionPrice



*/