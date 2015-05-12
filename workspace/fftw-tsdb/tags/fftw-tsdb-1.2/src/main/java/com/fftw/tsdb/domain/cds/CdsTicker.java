package com.fftw.tsdb.domain.cds;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fftw.tsdb.domain.GeneralAttributeValue;
import com.fftw.tsdb.domain.Ticker;

@Entity
@Table(name = "cds_ticker")
public class CdsTicker implements Serializable
{
    private static final long serialVersionUID = -6045994065647567278L;

    private Long id;

    private String name;

    private GeneralAttributeValue ccy;

    private GeneralAttributeValue docClause;

    private Ticker ticker;

    private GeneralAttributeValue tier;

    @Id
    @GeneratedValue
    @Column(name = "cds_ticker_id")
    public Long getId ()
    {
        return id;
    }

    public void setId (Long id)
    {
        this.id = id;
    }

    @Column(name = "cds_ticker_name")
    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    @ManyToOne
    @JoinColumn(name = "ccy_id")
    public GeneralAttributeValue getCcy ()
    {
        return ccy;
    }

    public void setCcy (GeneralAttributeValue ccy)
    {
        this.ccy = ccy;
    }

    @ManyToOne
    @JoinColumn(name = "doc_clause_id")
    public GeneralAttributeValue getDocClause ()
    {
        return docClause;
    }

    public void setDocClause (GeneralAttributeValue docClause)
    {
        this.docClause = docClause;
    }

    @ManyToOne
    @JoinColumn(name = "ticker_id")
    public Ticker getTicker ()
    {
        return ticker;
    }

    public void setTicker (Ticker ticker)
    {
        this.ticker = ticker;
    }

    @ManyToOne
    @JoinColumn(name = "tier_id")
    public GeneralAttributeValue getTier ()
    {
        return tier;
    }

    public void setTier (GeneralAttributeValue tier)
    {
        this.tier = tier;
    }

}
