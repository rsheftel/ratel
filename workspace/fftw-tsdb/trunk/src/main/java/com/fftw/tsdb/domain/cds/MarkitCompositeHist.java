package com.fftw.tsdb.domain.cds;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NamedQuery;

@Entity
@Table(name = "T_Markit_Cds_Composite_Hist")
@NamedQuery(name="MarkitCompositeHist.getCdsHistByDate", query="select m from MarkitCompositeHist m where m.id.date = :date", readOnly=true)
public class MarkitCompositeHist implements Serializable
{
    private static final long serialVersionUID = 3692665949576210876L;
    
    private MarkitCompositeHistPK id;
    private String avRating;
    private String compositeDepth5y;
    private Double recovery;
    private String tickerShortName;
    private Double spread6m;
    private Double spread1y;
    private Double spread2y;
    private Double spread3y;
    private Double spread4y;
    private Double spread5y;
    private Double spread7y;
    private Double spread10y;
    private Double spread15y;
    private Double spread20y;
    private Double spread30y;
    

    @Id
    public MarkitCompositeHistPK getId ()
    {
        return id;
    }

    public void setId (MarkitCompositeHistPK id)
    {
        this.id = id;
    }

    @Column(name = "avRating")
    public String getAvRating ()
    {
        return avRating;
    }

    public void setAvRating (String avRating)
    {
        this.avRating = avRating;
    }

    @Column(name = "compositeDepth5y")
    public String getCompositeDepth5y ()
    {
        return compositeDepth5y;
    }

    public void setCompositeDepth5y (String compositeDepth5y)
    {
        this.compositeDepth5y = compositeDepth5y;
    }

    @Column(name = "recovery")
    public Double getRecovery ()
    {
        return recovery;
    }

    public void setRecovery (Double recovery)
    {
        this.recovery = recovery;
    }

    @Column(name = "shortName")
    public String getTickerShortName ()
    {
        return tickerShortName;
    }

    public void setTickerShortName (String tickerShortName)
    {
        this.tickerShortName = tickerShortName;
    }

    @Column(name = "spread6m")
    public Double getSpread6m ()
    {
        return spread6m;
    }

    public void setSpread6m (Double spread6m)
    {
        this.spread6m = spread6m;
    }

    @Column(name = "spread1y")
    public Double getSpread1y ()
    {
        return spread1y;
    }

    public void setSpread1y (Double spread1y)
    {
        this.spread1y = spread1y;
    }

    @Column(name = "spread2y")
    public Double getSpread2y ()
    {
        return spread2y;
    }

    public void setSpread2y (Double spread2y)
    {
        this.spread2y = spread2y;
    }

    @Column(name = "spread3y")
    public Double getSpread3y ()
    {
        return spread3y;
    }

    public void setSpread3y (Double spread3y)
    {
        this.spread3y = spread3y;
    }

    @Column(name = "spread4y")
    public Double getSpread4y ()
    {
        return spread4y;
    }

    public void setSpread4y (Double spread4y)
    {
        this.spread4y = spread4y;
    }

    @Column(name = "spread5y")
    public Double getSpread5y ()
    {
        return spread5y;
    }

    public void setSpread5y (Double spread5y)
    {
        this.spread5y = spread5y;
    }

    @Column(name = "spread7y")
    public Double getSpread7y ()
    {
        return spread7y;
    }

    public void setSpread7y (Double spread7y)
    {
        this.spread7y = spread7y;
    }

    @Column(name = "spread10y")
    public Double getSpread10y ()
    {
        return spread10y;
    }

    public void setSpread10y (Double spread10y)
    {
        this.spread10y = spread10y;
    }

    @Column(name = "spread15y")
    public Double getSpread15y ()
    {
        return spread15y;
    }

    public void setSpread15y (Double spread15y)
    {
        this.spread15y = spread15y;
    }

    @Column(name = "spread20y")
    public Double getSpread20y ()
    {
        return spread20y;
    }

    public void setSpread20y (Double spread20y)
    {
        this.spread20y = spread20y;
    }

    @Column(name = "spread30y")
    public Double getSpread30y ()
    {
        return spread30y;
    }

    public void setSpread30y (Double spread30y)
    {
        this.spread30y = spread30y;
    }
}
