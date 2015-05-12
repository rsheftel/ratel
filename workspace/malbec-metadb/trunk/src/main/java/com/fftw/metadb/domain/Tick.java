package com.fftw.metadb.domain;

import java.util.Date;

public class Tick
{
    //private String topic;
    private Date timestamp;
    private Double price;
    private Double volume;
    
    public Double getPrice ()
    {
        return price;
    }

    public Date getTimestamp ()
    {
        return timestamp;
    }

    public Double getVolume ()
    {
        return volume;
    }

    public Tick (Date timestamp, Double price, Double volume)
    {
        super();
        //this.topic = topic;
        this.timestamp = timestamp;
        this.price = price;
        this.volume = volume;
    }    
    
    public String toString()
    {
        return timestamp + " " + price + " " + volume;
    }
}
