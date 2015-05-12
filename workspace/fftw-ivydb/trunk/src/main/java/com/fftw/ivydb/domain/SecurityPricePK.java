package com.fftw.ivydb.domain;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SecurityPricePK implements Serializable
{
    private static final long serialVersionUID = 9183822347193072791L;

    private Long securityId;

    // Using java.sql.Date because the time is always 00:00:00 in this table
    private Date date;

    @Column(name = "securityID")
    public Long getSecurityId ()
    {
        return securityId;
    }

    public void setSecurityId (Long securityId)
    {
        this.securityId = securityId;
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

    @Override
    public int hashCode ()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((date == null) ? 0 : date.hashCode());
        result = PRIME * result + ((securityId == null) ? 0 : securityId.hashCode());
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
        final SecurityPricePK other = (SecurityPricePK)obj;
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
        if (securityId == null)
        {
            if (other.securityId != null)
            {
                return false;
            }
        }
        else if (!securityId.equals(other.securityId))
        {
            return false;
        }
        return true;
    }

}
