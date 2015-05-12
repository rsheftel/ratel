package com.fftw.tsdb.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "attribute")
@NamedQuery(name = "Attribute.getAttributeByName", query = "from Attribute as a where a.name = :name")
public class Attribute implements Serializable
{
    private static final long serialVersionUID = -5179471497439557914L;

    private Long id;

    private String name;

    private String tableName;

    private String primaryKeyColumnName;

    private String descColumnName;

    public Attribute ()
    {
    }

    @Id
    @GeneratedValue
    @Column(name = "attribute_id")
    public Long getId ()
    {
        return id;
    }

    public void setId (Long id)
    {
        this.id = id;
    }

    @Column(name = "description_col_name")
    public String getDescColumnName ()
    {
        return descColumnName;
    }

    public void setDescColumnName (String descColumnName)
    {
        this.descColumnName = descColumnName;
    }

    @Column(name = "attribute_name")
    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    @Column(name = "primary_key_col_name")
    public String getPrimaryKeyColumnName ()
    {
        return primaryKeyColumnName;
    }

    public void setPrimaryKeyColumnName (String primaryKeyColumnName)
    {
        this.primaryKeyColumnName = primaryKeyColumnName;
    }

    @Column(name = "table_name")
    public String getTableName ()
    {
        return tableName;
    }

    public void setTableName (String tableName)
    {
        this.tableName = tableName;
    }

    @Override
    public int hashCode ()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((descColumnName == null) ? 0 : descColumnName.hashCode());
        result = PRIME * result + ((id == null) ? 0 : id.hashCode());
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        result = PRIME * result
            + ((primaryKeyColumnName == null) ? 0 : primaryKeyColumnName.hashCode());
        result = PRIME * result + ((tableName == null) ? 0 : tableName.hashCode());
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
        final Attribute other = (Attribute)obj;
        if (descColumnName == null)
        {
            if (other.descColumnName != null)
            {
                return false;
            }
        }
        else if (!descColumnName.equals(other.descColumnName))
        {
            return false;
        }
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        }
        else if (!id.equals(other.id))
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        if (primaryKeyColumnName == null)
        {
            if (other.primaryKeyColumnName != null) {
                return false;
            }
        }
        else if (!primaryKeyColumnName.equals(other.primaryKeyColumnName)) {
            return false;
        }
        if (tableName == null)
        {
            if (other.tableName != null)
            {
                return false;
            }
        }
        else if (!tableName.equals(other.tableName))
        {
            return false;
        }
        return true;
    }

}
