package com.fftw.bloomberg.types;

import junit.framework.TestCase;

public class TestBBProductCode extends TestCase
{

    public void testValueOf ()
    {

        BBProductCode pc = BBProductCode.valueOf(0);

        assert pc == BBProductCode.Uknown : "Lookup failed for Unknown";

        pc = BBProductCode.valueOf(11);
        assert pc == BBProductCode.Mortgage : "Lookup failed for Mortgage";
    }
}
