package com.fftw.owndata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.fftw.util.DBTools;

public class GDBDownloader
{
    public static void download() throws Exception
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
     
        try
        {   
            con = DBTools.getConnection("DB.OwnDataDB");            
            ps = con.prepareCall("exec Download");            
            ps.executeUpdate();
            
            System.out.println("Done");
            
            ps = con.prepareStatement("select max(ID) from " +
                    "(select max(EXCHANGE) as ID from EXCHANGES union " + 
                    "select max(SYMBOL) from DICTIONARY union " +
                    "select max(DATA_FEED) from DATA_FEEDS union " +
                    "select max(ID) from ASC_MAP union " +
                    "select max(SETTING) from SETTINGS where SETTING<99999 union " +
                    "select max(SESSION) from SESSIONS union " +
                    "select max(HOLIDAY) from HOLIDAYS) z ");            
            rs = ps.executeQuery();
            
            if (rs.next())
            {
                int id = rs.getInt(1) + 1;
                System.out.println("The max ID is " + id);
                DBTools.close(rs, ps);
                DBTools.close(con);
                                
                con = DBTools.getConnection("DB.InterBase");            
                ps = con.prepareStatement("SET GENERATOR GLOBALGENERATOR TO " + id);
                //ps.setInt(1, id);                
                ps.executeUpdate();                
                System.out.println("Set Generator to be " + id);
            }
        }
        finally
        {
            DBTools.close(rs, ps);
            DBTools.close(con);
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        download();
    }
}
