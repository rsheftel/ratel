package com.fftw.ivydb.domain;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class StandardOptionPricePK implements Serializable
{
    private static final long serialVersionUID = -302177315972394601L;

    private Long securityId;

    // Using java.sql.Date because the time is always 00:00:00 in this table
    private Date date;

    private Double days;

    private String callPutFlag;

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

    @Column(name = "days")
    public Double getDays ()
    {
        return days;
    }

    public void setDays (Double days)
    {
        this.days = days;
    }

    @Column(name = "callPutFlag")
    public String getCallPutFlag ()
    {
        return callPutFlag;
    }

    public void setCallPutFlag (String callPutFlag)
    {
        this.callPutFlag = callPutFlag;
    }

    @Override
    public int hashCode ()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((callPutFlag == null) ? 0 : callPutFlag.hashCode());
        result = PRIME * result + ((date == null) ? 0 : date.hashCode());
        result = PRIME * result + ((days == null) ? 0 : days.hashCode());
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
        final StandardOptionPricePK other = (StandardOptionPricePK)obj;
        if (callPutFlag == null)
        {
            if (other.callPutFlag != null)
            {
                return false;
            }
        }
        else if (!callPutFlag.equals(other.callPutFlag))
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
        if (days == null)
        {
            if (other.days != null)
            {
                return false;
            }
        }
        else if (!days.equals(other.days))
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
