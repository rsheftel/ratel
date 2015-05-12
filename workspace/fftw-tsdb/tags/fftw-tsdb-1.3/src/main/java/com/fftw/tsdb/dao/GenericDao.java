package com.fftw.tsdb.dao;

import java.io.Serializable;


public interface GenericDao<T, ID extends Serializable>
{
    void persist (T type);
    void update (T type);
    void save (T type);
    void saveOrUpdate(T type);
    void flush();
    void clear();
    T findByID (ID id);
    T findByName (String name);
}
