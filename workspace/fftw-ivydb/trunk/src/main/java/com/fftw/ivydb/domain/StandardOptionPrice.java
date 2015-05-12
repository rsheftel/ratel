package com.fftw.ivydb.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NamedQuery;

@Entity
@Table(name = "StdOptionPrice")
@NamedQuery(name = "StandardOptionPrice.getStandardOptionPriceByDate", query = "select s from StandardOptionPrice s where s.id.date = :date", readOnly = true)
public class StandardOptionPrice implements Serializable
{
    private static final long serialVersionUID = 8472837176433926469L;

    private StandardOptionPricePK id;

    private Double impliedVolatility;

    private Double delta;

    @Id
    public StandardOptionPricePK getId ()
    {
        return id;
    }

    public void setId (StandardOptionPricePK id)
    {
        this.id = id;
    }

    @Column(name = "impliedVolatility")
    public Double getImpliedVolatility ()
    {
        return impliedVolatility;
    }

    public void setImpliedVolatility (Double impliedVolatility)
    {
        this.impliedVolatility = impliedVolatility;
    }

    @Column(name = "delta")
    public Double getDelta ()
    {
        return delta;
    }

    public void setDelta (Double delta)
    {
        this.delta = delta;
    }

}
