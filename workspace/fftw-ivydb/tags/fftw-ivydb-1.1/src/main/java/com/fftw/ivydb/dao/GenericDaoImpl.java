package com.fftw.ivydb.dao;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class GenericDaoImpl<T, ID extends Serializable> implements GenericDao<T, ID>
{
    @PersistenceContext
    protected EntityManager em;
    
    private Class<T> entityClass;
    
    public GenericDaoImpl (Class<T> entityClass)
    {
        this.entityClass = entityClass;
    }

    public T findByID (ID id)
    {
        return em.find(entityClass, id);
    }
}
