package com.fftw.tsdb.domain.cds;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NamedQuery;

@Entity
@Table(name = "credit_rating")
@NamedQuery(name="CreditRating.getCreditRating", query="select c from CreditRating c where c.snp = :name", readOnly=true)
public class CreditRating implements Serializable
{
    private static final long serialVersionUID = 7070664711464952685L;

    private Double ratingValue;

    private String fitch;

    private String moodys;

    private String snp;

    @Id
    @Column(name = "rating_value")
    public Double getRatingValue ()
    {
        return ratingValue;
    }

    public void setRatingValue (Double ratingValue)
    {
        this.ratingValue = ratingValue;
    }

    @Column(name = "fitch")
    public String getFitch ()
    {
        return fitch;
    }

    public void setFitch (String fitch)
    {
        this.fitch = fitch;
    }

    @Column(name = "moodys")
    public String getMoodys ()
    {
        return moodys;
    }

    public void setMoodys (String moodys)
    {
        this.moodys = moodys;
    }

    @Column(name = "snp")
    public String getSnp ()
    {
        return snp;
    }

    public void setSnp (String snp)
    {
        this.snp = snp;
    }
}
