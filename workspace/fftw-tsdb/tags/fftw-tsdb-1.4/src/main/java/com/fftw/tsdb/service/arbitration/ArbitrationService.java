package com.fftw.tsdb.service.arbitration;

import java.util.Calendar;
import java.io.IOException;

public interface ArbitrationService
{
    void cdsArbitration (Calendar calArbitration) throws IOException;
}
