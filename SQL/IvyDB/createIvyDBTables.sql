use IvyDB
/*

create table dbo.Security (
securityID int not null
,cusip char(8) not null
,ticker char(6)  null
,sic char(4) null
,indexFlag char(1) not null
,exchangeDesignator int  null
,class char(1) null
,issueType char(1) null
,industryGroup char(3) null

)
--alter table dbo.Security alter column issueType char(1) null
alter table dbo.Security
add constraint pkSecurity primary key (securityID)
alter table dbo.Security
add constraint chk_indexFlag check (indexFlag in ('0','1'))
alter table dbo.Security
add constraint chk_exchangeDesignator check (exchangeDesignator in (0,1,2,4,8,16,32768))
alter table dbo.Security
--drop constraint chk_issueType 
add constraint chk_issueType check (issueType in ('0','A','7','F','%',' '))

create table dbo.IndustryGroup (
classificationCode int not null
,industryGroupDescription varchar(100) not null
)
alter table dbo.IndustryGroup
add constraint pkIndustryGroup primary key (classificationCode)

create table dbo.SecurityName (
securityID int not null
,effectiveDate datetime not null
,cusip char(8) not null
,ticker char(6)  null
,class char(1)  null
,issuerDescription varchar(28) not null
,issueDescription varchar(20)  null
,sic char(4)  null
)
alter table dbo.SecurityName
--drop constraint pkSecurityName 
add constraint pkSecurityName primary key (securityID,effectiveDate)

--drop table dbo.Exchange
create table dbo.Exchange(
securityID int not null
,effectiveDate datetime not null
,sequenceNumber int not null
,status char(1) null
,exchange char(1) not null
,addDeleteIndicator char(1) null
,exchangeFlags int not null
)
alter table dbo.Exchange
--drop constraint pkExchange 
add constraint pkExchange primary key (securityID,effectiveDate,sequenceNumber)
alter table dbo.Exchange
--drop constraint chk_status 
add constraint chk_status check (status in ('$','A','C','D','E','N','S','X','3','4','5'))
alter table dbo.Exchange
add constraint chk_exchange check (exchange in ('A','B','F','G','H','O','%','?','D','E','J','K','T','U','V','X'))
alter table dbo.Exchange
--drop constraint chk_addDeleteIndicator 
add constraint chk_addDeleteIndicator check ( addDeleteIndicator in ('*',' '))
alter table dbo.Exchange
--drop constraint chk_exchangeFlags 
add constraint chk_exchangeFlags check (exchangeFlags in (0,1,2,4,8,16,32768))

--drop table dbo.Distribution
--truncate table dbo.Distribution
create table dbo.Distribution (
securityID int not null
,recordDate datetime not null
,sequenceNumber int not null
,exDate datetime not null
,amount float not null
,adustmentFactor float not null
,declareDate datetime not null
,paymentDate datetime not null
,linkSecurityID int not null
,distributionType char(1) not null
,frequency char(1) null
,currency char(3) null /* iso currency code: create iso currency code table in IvyDB | TSDB*/
,approximateFlag char(1) null
,cancelFlag char(1)  null
,liquidationFlag char(1)  null
)
--alter table dbo.Distribution alter column liquidationFlag char(1) null
alter table dbo.Distribution
--drop constraint pkDistribution 
add constraint pkDistribution primary key (securityID,recordDate,sequenceNumber)
alter table dbo.Distribution
add constraint chk_distributionType check (distributionType in ('0','1','2','3','4','5','6','7','8','9','%'))
alter table dbo.Distribution
add constraint chk_frequency check (frequency in ('0','1','2','3','4','5',' '))
alter table dbo.Distribution
add constraint chk_approximateFlag check (approximateFlag in ('0','1'))
alter table dbo.Distribution
add constraint chk_cancelFlag check (cancelFlag in ('0','1'))
alter table dbo.Distribution
add constraint chk_liquidationFlag check (liquidationFlag in ('0','1'))

--drop table dbo.SecurityPrice 
create table dbo.SecurityPrice (
securityID int not null
,date datetime not null
,bid_Low float not null
,ask_High float not null
,closePrice float not null
,volume float not null
,totalReturn float not null
,cumulativeAdjustmentFactor float not null
,openPrice float null
,sharesOutstanding int not null
,cumulativeTotalReturnFactor float not null
)
alter table dbo.SecurityPrice
--drop constraint pkSecurityPrice 
add constraint pkSecurityPrice primary key (securityID,date)

--drop table dbo.OptionPrice 
create table dbo.OptionPrice (
securityID int not null
,date datetime not null
,root char(5) not null
,suffix char(2) not null
,strikePrice int not null
,expirationDate datetime not null
,callPutFlag char(1) not null
,bestBid float not null
,bestOffer float not null
,lastTradeDate datetime null
,volume int not null
,openInterest int not null
,specialSettlementFlag char(1) not null
,impliedVolatility float not null
,delta float not null
,gamma float not null
,vegaKappa float not null
,theta float not null
,optionID int not null
,adjustmentFactor int not null
)
alter table dbo.OptionPrice
--drop constraint pkOptionPrice 
add constraint pkOptionPrice primary key (date,root,suffix)

alter table dbo.OptionPrice
add constraint chk_callPutFlag check (callPutFlag in ('P','C'))
alter table dbo.OptionPrice
add constraint chk_specialSettlementFlag check (specialSettlementFlag in ('0','1','E'))

create table dbo.ZeroCurve (
date datetime not null
,days int not null
,rate float not null
)
alter table dbo.ZeroCurve
--drop constraint pkZeroCurve 
add constraint pkZeroCurve primary key (date,days)

create table dbo.IndexDividend (
securityID int not null
,date datetime not null
,rate float not null
)
alter table dbo.IndexDividend
--drop constraint pkIndexDividend
add constraint pkIndexDividend primary key (securityID,date)

--truncate table dbo.StdOptionPrice
create table dbo.StdOptionPrice (
securityID int not null
,date datetime not null
,days float not null
,forwardPrice float not null
,strikePrice float not null
,callPutFlag char(1) not null
,premium float not null
,impliedVolatility float not null
,delta float not null
,gamma float not null
,vega float not null
,theta float not null
)
alter table dbo.StdOptionPrice
--drop constraint pkStdOptionPrice 
add constraint pkStdOptionPrice primary key (securityID,date,days,callPutFlag)
alter table dbo.StdOptionPrice
--drop constraint chk_StdOptionPricecallPutFlag 
add constraint chk_StdOptionPricecallPutFlag check (callPutFlag in ('P','C'))

--truncate table dbo.OptionVolume 
create table dbo.OptionVolume (
securityID int not null
,date datetime not null
,callPutFlag char(1) not null
,volume int not null
,openInterest int not null
)
alter table dbo.OptionVolume
--drop constraint pkOptionVolume 
add constraint pkOptionVolume primary key (securityID,date,callPutFlag)
alter table dbo.OptionVolume
add constraint chk_OptionVolumecallPutFlag check (callPutFlag in ('P','C',' '))

--drop table dbo.VolatilitySurface 
create table dbo.VolatilitySurface (
securityID int not null
,date datetime not null
,days int not null
,delta int not null
,callPutFlag char(1) not null
,impliedVolatility float not null
,impliedStrike float not null
,impliedPremium float not null
,dispersion float not null                     
)
alter table dbo.VolatilitySurface
--drop constraint pkVolatilitySurface 
add constraint pkVolatilitySurface primary key (securityID,date,days,delta,callPutFlag)
alter table dbo.VolatilitySurface
add constraint chk_VolatilitySurfacecallPutFlag check (callPutFlag in ('P','C'))

--drop table dbo.HistoricalVolatility 
create table dbo.HistoricalVolatility (
securityID int not null
,date datetime not null
,days int not null
,volatility float null
)
alter table dbo.HistoricalVolatility
--drop constraint pkHistoricalVolatility 
add constraint pkHistoricalVolatility primary key (securityID,date,days)

create table dbo.OpenInterest (
securityID int not null
,date datetime not null
,root char(5) not null
,suffix char(2) not null
,openInterest int not null
)
alter table dbo.OpenInterest
add constraint pkOpenInterest primary key (date,root,suffix)

--drop table dbo.OptionInfo 
create table dbo.OptionInfo (
securityID int not null
,dividendConvention char(1)  null
,exercizeStyle char(1) not null
,amSettlementFlag int not null
)
alter table dbo.OptionInfo
add constraint pkOptionInfo primary key (securityID)
alter table dbo.OptionInfo
add constraint chk_OptionInfodividendConvention check (dividendConvention in (' ', 'I','C'))
alter table dbo.OptionInfo
add constraint chk_OptionInfoexercizeStyle check (exercizeStyle in ('A','E','?'))
alter table dbo.OptionInfo
add constraint chk_OptionInfoamSettlementFlag check (amSettlementFlag in ('0', '1'))
*/