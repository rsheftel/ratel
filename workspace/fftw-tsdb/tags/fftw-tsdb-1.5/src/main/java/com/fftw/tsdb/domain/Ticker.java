package com.fftw.tsdb.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ticker")
public class Ticker implements Serializable
{
    private static final long serialVersionUID = -6012151922401160795L;
    
    private Long id;
    private String name;
    private String description;
    
    public Ticker (){}

    @Id
    @GeneratedValue
    @Column(name = "ticker_id")
    public Long getId ()
    {
        return id;
    }

    public void setId (Long id)
    {
        this.id = id;
    }
    
    @Column(name = "ticker_description")
    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    @Column(name = "ticker_name")
    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }
    
    
    
}
