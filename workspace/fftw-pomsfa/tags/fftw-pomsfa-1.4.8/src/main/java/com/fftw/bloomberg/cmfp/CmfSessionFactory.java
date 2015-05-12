package com.fftw.bloomberg.cmfp;

import quickfix.ConfigError;

public interface CmfSessionFactory
{
    CmfSession create(CmfSessionID sessionID, CmfSessionSettings settings) throws ConfigError;
}
