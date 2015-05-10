IF NOT EXISTS (SELECT * FROM sysobjects WHERE Name = 'PATCH_LOG')
   CREATE TABLE dbo.PATCH_LOG (PatchDate smalldatetime, Message varchar(255))
IF NOT EXISTS (SELECT * FROM sysobjects WHERE Name = 'PATCH_SecurityPrice_TEMP')
   SELECT Action=' ',* INTO dbo.PATCH_SecurityPrice_TEMP FROM SecurityPrice WHERE 1=2
IF NOT EXISTS (SELECT * FROM sysobjects WHERE Name = 'PATCH_OptionPrice_TEMP')
   SELECT Action=' ',* INTO dbo.PATCH_OptionPrice_TEMP FROM OptionPrice WHERE 1=2
IF NOT EXISTS (SELECT * FROM sysobjects WHERE Name = 'PATCH_StdOptionPrice_TEMP')
   SELECT Action=' ',* INTO dbo.PATCH_StdOptionPrice_TEMP FROM StdOptionPrice WHERE 1=2
IF NOT EXISTS (SELECT * FROM sysobjects WHERE Name = 'PATCH_VolatilitySurface_TEMP')
   SELECT Action=' ',* INTO dbo.PATCH_VolatilitySurface_TEMP FROM VolatilitySurfacePatch WHERE 1=2
GO
IF EXISTS (SELECT * FROM sysobjects WHERE Name = 'PatchSecurityPrice')
DROP PROCEDURE dbo.PatchSecurityPrice
GO
CREATE PROCEDURE dbo.PatchSecurityPrice
  AS
  BEGIN
  
  DELETE SecurityPrice 
    FROM PATCH_SecurityPrice_TEMP P join SecurityPrice S
    on P.securityID = S.securityID and P.date = S.date
    WHERE P.Action = 'D'
  UPDATE S
    SET S.bid_Low = P.bid_Low,
        S.ask_High = P.ask_High,
        S.closePrice = P.closePrice,
        S.volume = P.volume,
        S.totalReturn = P.totalReturn,
        S.cumulativeAdjustmentFactor = P.cumulativeAdjustmentFactor,
        S.OpenPrice = P.openPrice,
        S.sharesOutstanding = P.shareOutstanding,
        S.cumulativeTotalReturnFactor = P.cumulativeTotalReturnFactor
    from PATCH_SecurityPrice_TEMP P join SecurityPrice S
    on P.securityID = S.securityID and P.date = S.date
    and P.Action = 'I'
  INSERT SecurityPrice (securityID, date, bid_Low, ask_High, closePrice, volume, totalReturn, cumulativeAdjustmentFactor,
    OpenPrice, sharesOutstanding, cumulativeTotalReturnFactor)
    SELECT P.securityID, P.date, P.bid_Low, P.ask_High, P.closePrice, P.volume, P.totalReturn, P.cumulativeAdjustmentFactor,
    P.openPrice, P.shareOutstanding, P.cumulativeTotalReturnFactor
    FROM PATCH_SecurityPrice_TEMP P left outer join SecurityPrice S
    on P.securityID = S.securityID and P.date = S.date
    where S.securityID is null
    and P.Action = 'I' 
  END
GO
IF EXISTS (SELECT * FROM sysobjects WHERE NAME = 'PatchOptionPriceTag')
DROP PROCEDURE dbo.PatchOptionPriceTag
GO
  CREATE PROCEDURE dbo.PatchOptionPriceTag
  @date smalldatetime = NULL
  AS
  BEGIN
  
  DECLARE @tag VARCHAR(10)
  DECLARE @rollover smalldatetime
  DECLARE @dt1 smalldatetime
  DECLARE @dt2 smalldatetime
  IF @date is not null
    BEGIN
    DECLARE @mo int
    DECLARE @yr int
    SET @dt1 = @date
    SET @dt2 = Dateadd(day, -1, Dateadd(month, 1, @dt1))
    SET @mo = MONTH(@date)
    SET @yr = YEAR(@date)
    SET @tag = '_' + CAST(@yr AS CHAR(4)) + '_' + RIGHT('0' + CAST(@mo AS VARCHAR(2)),2)
    END
  ELSE
    BEGIN
    SET @tag = ''
    END
  DECLARE @sql nvarchar(2000)
  SET @sql = '
  --DELETE OptionPrice' + @tag + ' 
  --  FROM PATCH_OptionPrice_TEMP P join OptionPrice' + @tag + ' S
  DELETE OptionPricePatch
  FROM PATCH_OptionPrice_TEMP P join OptionPricePatch S
    on P.root = S.root and P.date = S.date and P.suffix = S.suffix
    and P.Action = ''D'''
  EXEC sp_executesql @sql
  SET @sql = '
  UPDATE S
     SET  S.securityID = P.securityID,
        S.expiration = P.expiration,
        S.strike = P.strike,
        S.callPutFlag = P.callPutFlag,
        S.bestBid = P.bestBid,
        S.bestOffer = P.bestOffer,
        S.lastTradeDate = P.lastTradeDate,
        S.volume = P.volume,
        S.openInterest = P.openInterest,
        S.specialSettlement = P.specialSettlement,
        S.impliedVolatility = P.impliedVolatility,
        S.delta = P.delta,
        S.gamma = P.gamma,
        S.theta = P.theta,
        S.vega = P.vega,
        S.optionID = P.optionID,
        S.adjustmentFactor = P.adjustmentFactor
    -- from PATCH_OptionPrice_TEMP P join OptionPricePatch' + @tag + ' S
      from PATCH_OptionPrice_TEMP P join OptionPricePatch S
     on P.root = S.root and P.date = S.date and P.suffix = S.suffix
     and P.Action = ''I'''
  EXEC sp_executesql @sql
  SET @sql = '
  --INSERT OptionPricePatch' + @tag + '(securityID, date, root, suffix, strike, expiration, callPutFlag,
    INSERT OptionPricePatch (securityID, date, root, suffix, strike, expiration, callPutFlag,
    bestBid, bestOffer, lastTradeDate, volume, openInterest,specialSettlement,
    impliedVolatility, delta, gamma, theta, vega, optionID, adjustmentFactor)
    SELECT P.securityID, P.date, P.root, P.suffix, P.strike, P.expiration, P.callPutFlag,
    P.bestBid, P.bestOffer, P.lastTradeDate, P.volume, P.openInterest,P.specialSettlement,
    P.impliedVolatility, P.delta, P.gamma, P.theta, P.vega, P.optionID, P.adjustmentFactor
    --FROM PATCH_OptionPrice_TEMP P left outer join OptionPricePatch' + @tag + ' S
    FROM PATCH_OptionPrice_TEMP P left outer join OptionPricePatch S
    on P.root = S.root and P.date = S.date and P.suffix = S.suffix 
    where S.root is null
    and P.Action = ''I''' 
     /*
     IF @tag <> ''
    BEGIN
    SET @sql = @sql + ' AND P.date BETWEEN ''' + cast(@dt1 as varchar(20)) + ''' AND ''' + cast(@dt2 as varchar(20)) + ''''
    END
    */
  EXEC sp_executesql @sql
  END
GO
IF EXISTS (SELECT * FROM sysobjects WHERE Name = 'PatchOptionPrice')
DROP PROCEDURE dbo.PatchOptionPrice
GO
CREATE PROCEDURE dbo.PatchOptionPrice
   AS 
   BEGIN
/*  
 DECLARE @rollover smalldatetime
 select @rollover = [DATE] from ROLLOVER
 DECLARE @date smalldatetime
 SET @date = '1/1/96'
 WHILE @date < @rollover
   BEGIN
    EXEC PatchOptionPriceTag @date
    SET @date = DATEADD(month, 1, @date)
   END
*/
   EXEC PatchOptionPriceTag 
   END                                           
GO
IF EXISTS (SELECT * FROM sysobjects WHERE Name = 'PatchVolatilitySurfaceTag')
DROP PROCEDURE dbo.PatchVolatilitySurfaceTag
GO
CREATE PROCEDURE dbo.PatchVolatilitySurfaceTag
   @date smalldatetime = NULL
 AS
 BEGIN
 
 DECLARE @tag VARCHAR(10)
 DECLARE @rollover smalldatetime
 DECLARE @dt1 smalldatetime
 DECLARE @dt2 smalldatetime
 IF @date is not null
  BEGIN
  DECLARE @mo int
  DECLARE @yr int
  SET @dt1 = @date
  SET @dt2 = Dateadd(day, -1, Dateadd(month, 1, @dt1))
  SET @mo = MONTH(@date)
  SET @yr = YEAR(@date)
  SET @tag = '_' + CAST(@yr AS CHAR(4)) + '_' + RIGHT('0' + CAST(@mo AS VARCHAR(2)),2)
  END
 ELSE
  BEGIN
  SET @tag = ''
  END
 DECLARE @sql nvarchar(2000)
 SET @sql = '
 --DELETE VolatilitySurfacePatch' + @tag + ' 
 --  FROM PATCH_VolatilitySurface_TEMP P join VolatilitySurfacePatch' + @tag + ' S
    DELETE VolatilitySurfacePatch 
    FROM PATCH_VolatilitySurface_TEMP P join VolatilitySurfacePatch S
   on P.securityID = S.securityID and P.date = S.date and P.days = S.days and P.delta = S.delta
   and P.Action = ''D'''
 EXEC sp_executesql @sql
 SET @sql = '
 UPDATE S
   SET  S.callPutFlag = P.callPutFlag,
        S.impliedVolatility = P.impliedVolatility,
        S.impliedStrike = P.impliedStrike,
        S.impliedPremium = P.impliedPremium,
        S.dispersion = P.dispersion
   --from PATCH_VolatilitySurface_TEMP P join VolatilitySurfacePatch' + @tag + ' S
   from PATCH_VolatilitySurface_TEMP P join VolatilitySurfacePatch S
   on P.securityID = S.securityID and P.date = S.date and P.days = S.days and P.delta = S.delta
   and P.Action = ''I'''
 EXEC sp_executesql @sql
 SET @sql = '
 --INSERT VolatilitySurfacePatch' + @tag + '(securityID, date, days, delta, callPutFlag, 
    INSERT VolatilitySurfacePatch (securityID, date, days, delta, callPutFlag, 
   impliedVolatility, impliedStrike, impliedPremium, dispersion)
   SELECT P.securityID, P.date, P.days, P.delta, P.callPutFlag,
   P.impliedVolatility, P.impliedStrike, P.impliedPremium, P.dispersion
   --FROM PATCH_VolatilitySurface_TEMP P left outer join VolatilitySurfacePatch' + @tag + ' S
    FROM PATCH_VolatilitySurface_TEMP P left outer join VolatilitySurfacePatch S
   on P.securityID = S.securityID and P.date = S.date and P.days = S.days and P.delta = S.delta
   where S.securityID is null
   and P.Action = ''I'' '
    /*
    IF @tag <> ''
   BEGIN
   SET @sql = @sql + ' AND P.date BETWEEN ''' + cast(@dt1 as varchar(20)) + ''' AND ''' + cast(@dt2 as varchar(20)) + ''''
   END
   */
 EXEC sp_executesql @sql
 END
GO
IF EXISTS (SELECT * FROM sysobjects WHERE NAME = 'PatchVolatilitySurface')
DROP PROCEDURE dbo.PatchVolatilitySurface
GO
CREATE PROCEDURE dbo.PatchVolatilitySurface
  AS
  BEGIN
/*  
     DECLARE @rollover smalldatetime
  select @rollover = [DATE] from ROLLOVER
  DECLARE @date smalldatetime
  SET @date = '1/1/96'
  WHILE @date < @rollover
    BEGIN
    EXEC PatchVolatilitySurfaceTag @date
    SET @date = DATEADD(month, 1, @date)
    END
*/
    EXEC PatchVolatilitySurfaceTag 
  END
GO
IF EXISTS (SELECT * FROM sysobjects WHERE Name = 'PatchStdOptionPrice')
DROP PROCEDURE dbo.PatchStdOptionPrice
GO
CREATE PROCEDURE dbo.PatchStdOptionPrice
  AS
  BEGIN
  
  DELETE StdOptionPrice 
    FROM PATCH_StdOptionPrice_TEMP P join StdOptionPrice S
    on P.securityID = S.securityID and P.date = S.date and P.days = S.days and P.callPutFlag = S.callPutFlag
    and P.Action = 'D'
  UPDATE S
    SET S.ForwardPrice = P.forwardPrice,
        S.strikePrice = P.strike,
        S.Premium = P.premium,
        S.impliedVolatility = P.impliedVolatility,
        S.delta = P.delta,
        S.Gamma = P.gamma,
        S.Theta = P.theta,
        S.Vega = P.vega
    from PATCH_StdOptionPrice_TEMP P join StdOptionPrice S
    on P.securityID = S.securityID and P.date = S.date and P.days = S.days and P.callPutFlag = S.callPutFlag
    and P.Action = 'I'
  INSERT StdOptionPrice (securityID, date, days, ForwardPrice, strikePrice, callPutFlag, 
    Premium, impliedVolatility, delta, Gamma, Theta, Vega)
    SELECT P.securityID, P.date, P.days, P.forwardPrice, P.strike, P.callPutFlag,
    P.premium, P.impliedVolatility, P.delta, P.gamma, P.theta, P.vega
    FROM PATCH_StdOptionPrice_TEMP P left outer join StdOptionPrice S
    on P.securityID = S.securityID and P.date = S.date and P.days = S.days and P.callPutFlag = S.callPutFlag
    where S.securityID is null
    and P.Action = 'I' 
  END
GO
TRUNCATE TABLE PATCH_SecurityPrice_TEMP
TRUNCATE TABLE PATCH_OptionPrice_TEMP
TRUNCATE TABLE PATCH_StdOptionPrice_TEMP
TRUNCATE TABLE PATCH_VolatilitySurface_TEMP
GO
