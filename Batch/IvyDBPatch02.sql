-------------------------------
PRINT 'Updating SECURITY_PRICE'
EXEC PatchSecurityPrice
GO
PRINT 'Updating OPTION_PRICE'
EXEC PatchOptionPrice
GO
PRINT 'Updating STD_OPTION_PRICE'
EXEC PatchStdOptionPrice
GO
PRINT 'Updating VOLATILITY_SURFACE'
EXEC PatchVolatilitySurface
GO

