package com.fftw.tsdb.dao;

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
    
    
    public T findByName (String name)
    {
        return (T)em.createQuery("from "  + entityClass.getName() + " as e where e.name = :name").setParameter("name", name).getSingleResult();
    }

    public void persist (T type)
    {
        em.persist(type);
        em.flush();
    }
    
    public void update (T type)
    {
        em.merge(type);
        em.flush();
    }
    
    public void save (T type)
    {
        em.persist(type);
    }
    
    
    public void saveOrUpdate (T type)
    {
        em.merge(type);
    }

    public void flush ()
    {
        em.flush();
    }

    public void clear ()
    {
        em.clear();
    }
    
    
}
