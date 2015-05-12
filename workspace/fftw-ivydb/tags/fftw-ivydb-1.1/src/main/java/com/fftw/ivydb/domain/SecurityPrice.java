package com.fftw.ivydb.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NamedQuery;

@Entity
@Table(name = "SecurityPrice")
@NamedQuery(name="SecurityPrice.getSecurityPriceByDate", query="select s from SecurityPrice s where s.id.date = :date", readOnly=true)
public class SecurityPrice implements Serializable
{
    private static final long serialVersionUID = -2314362531097380211L;

    private SecurityPricePK id;

    private Double closePrice;

    private Double totalReturn;

    private Double cumulativeTotalReturnFactor;

    private Integer sharesOutstanding;

    private Double volume;

    @Id
    public SecurityPricePK getId ()
    {
        return id;
    }

    public void setId (SecurityPricePK id)
    {
        this.id = id;
    }

    @Column(name = "closePrice")
    public Double getClosePrice ()
    {
        return closePrice;
    }

    public void setClosePrice (Double closePrice)
    {
        this.closePrice = closePrice;
    }

    @Column(name = "totalReturn")
    public Double getTotalReturn ()
    {
        return totalReturn;
    }

    public void setTotalReturn (Double totalReturn)
    {
        this.totalReturn = totalReturn;
    }

    @Column(name = "cumulativeTotalReturnFactor")
    public Double getCumulativeTotalReturnFactor ()
    {
        return cumulativeTotalReturnFactor;
    }

    public void setCumulativeTotalReturnFactor (Double cumulativeTotalReturnFactor)
    {
        this.cumulativeTotalReturnFactor = cumulativeTotalReturnFactor;
    }

    @Column(name = "sharesOutstanding")
    public Integer getSharesOutstanding ()
    {
        return sharesOutstanding;
    }

    public void setSharesOutstanding (Integer sharesOutstanding)
    {
        this.sharesOutstanding = sharesOutstanding;
    }

    @Column(name = "volume")
    public Double getVolume ()
    {
        return volume;
    }

    public void setVolume (Double volume)
    {
        this.volume = volume;
    }

}
