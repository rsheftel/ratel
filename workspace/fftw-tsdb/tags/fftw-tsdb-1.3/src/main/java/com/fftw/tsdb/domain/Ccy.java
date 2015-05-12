package com.fftw.tsdb.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ccy")
public class Ccy implements Serializable
{
    private static final long serialVersionUID = 3262986738155393583L;
    
    private Long id;
    private String name;
    private Double precedence;
        
    @Id
    @GeneratedValue
    @Column(name = "ccy_id")
    public Long getId ()
    {
        return id;
    }
    public void setId (Long id)
    {
        this.id = id;
    }
    
    @Column(name = "ccy_name")
    public String getName ()
    {
        return name;
    }
    public void setName (String name)
    {
        this.name = name;
    }
    
    @Column(name = "precedence")
    public Double getPrecedence ()
    {
        return precedence;
    }
    public void setPrecedence (Double precedence)
    {
        this.precedence = precedence;
    }
    
    
    
}
