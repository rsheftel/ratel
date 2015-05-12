package com.fftw.tsdb.domain.cds;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MarkitCompositeHistPK implements Serializable
{
    private static final long serialVersionUID = 8826561979341638131L;

    private String ccy;

    // Using java.sql.Date because the time is always 00:00:00 in this table
    private Date date;

    private String docClause;

    private String ticker;

    private String tier;

    @Column(name = "ccy")
    public String getCcy ()
    {
        return ccy;
    }

    public void setCcy (String ccy)
    {
        this.ccy = ccy;
    }

    @Column(name = "date")
    public Date getDate ()
    {
        return date;
    }

    public void setDate (Date date)
    {
        this.date = date;
    }

    @Column(name = "docClause")
    public String getDocClause ()
    {
        return docClause;
    }

    public void setDocClause (String docClause)
    {
        this.docClause = docClause;
    }

    @Column(name = "ticker")
    public String getTicker ()
    {
        return ticker;
    }

    public void setTicker (String ticker)
    {
        this.ticker = ticker;
    }

    @Column(name = "tier")
    public String getTier ()
    {
        return tier;
    }

    public void setTier (String tier)
    {
        this.tier = tier;
    }

    @Override
    public int hashCode ()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((ccy == null) ? 0 : ccy.hashCode());
        result = PRIME * result + ((date == null) ? 0 : date.hashCode());
        result = PRIME * result + ((docClause == null) ? 0 : docClause.hashCode());
        result = PRIME * result + ((ticker == null) ? 0 : ticker.hashCode());
        result = PRIME * result + ((tier == null) ? 0 : tier.hashCode());
        return result;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final MarkitCompositeHistPK other = (MarkitCompositeHistPK)obj;
        if (ccy == null)
        {
            if (other.ccy != null)
            {
                return false;
            }
        }
        else if (!ccy.equals(other.ccy))
        {
            return false;
        }
        if (date == null)
        {
            if (other.date != null)
            {
                return false;
            }
        }
        else if (!date.equals(other.date))
        {
            return false;
        }
        if (docClause == null)
        {
            if (other.docClause != null)
            {
                return false;
            }
        }
        else if (!docClause.equals(other.docClause))
        {
            return false;
        }
        if (ticker == null)
        {
            if (other.ticker != null)
            {
                return false;
            }
        }
        else if (!ticker.equals(other.ticker))
        {
            return false;
        }
        if (tier == null)
        {
            if (other.tier != null)
            {
                return false;
            }
        }
        else if (!tier.equals(other.tier))
        {
            return false;
        }
        return true;
    }

}
