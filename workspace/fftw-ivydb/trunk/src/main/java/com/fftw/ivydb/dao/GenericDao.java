package com.fftw.ivydb.dao;

import java.io.Serializable;


public interface GenericDao<T, ID extends Serializable>
{
    T findByID (ID id);
    
}
