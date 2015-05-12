package com.fftw.tsdb.service;

import com.fftw.tsdb.domain.Attribute;

public interface AttributeService
{
    Attribute findByName(String name);
}
