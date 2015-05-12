package com.fftw.tsdb.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "time_series_attribute_map")
public class TimeSeriesAttributeMap implements Serializable
{
    private static final long serialVersionUID = -4087799215009641152L;
    
    private TimeSeriesAttributeMapPK id;
    private Long attributeValueId;
    
    public TimeSeriesAttributeMap (){}


    @EmbeddedId
    public TimeSeriesAttributeMapPK getId ()
    {
        return id;
    }

    public void setId (TimeSeriesAttributeMapPK id)
    {
        this.id = id;
    }

    @Column(name = "attribute_value_id")
    public Long getAttributeValueId ()
    {
        return attributeValueId;
    }

    public void setAttributeValueId (Long attributeValueId)
    {
        this.attributeValueId = attributeValueId;
    }

}
