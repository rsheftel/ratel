package com.fftw.tsdb.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "general_attribute_value")
public class GeneralAttributeValue implements Serializable
{
    private static final long serialVersionUID = -1388085382782232597L;
    
    private Long id;
    private String name;
    
    
    public GeneralAttributeValue (){}


    @Id
    @GeneratedValue
    @Column(name = "attribute_value_id")
    public Long getId ()
    {
        return id;
    }


    public void setId (Long id)
    {
        this.id = id;
    }


    @Column(name = "attribute_value_name")
    public String getName ()
    {
        return name;
    }


    public void setName (String name)
    {
        this.name = name;
    }
    
    
    
}
