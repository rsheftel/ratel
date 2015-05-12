package com.fftw.owndata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.fftw.util.DBTools;

public class GDBUploader
{
    public static void upload() throws Exception
    {
        Connection con = null;
        PreparedStatement ps = null;
     
        try
        {   
            con = DBTools.getConnection("DB.OwnDataDB");            
            ps = con.prepareCall("exec Upload");            
            ps.executeUpdate();
            
            System.out.println("Done");
        }
        finally
        {
            DBTools.close(ps);
            DBTools.close(con);
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        upload();
    }
}
