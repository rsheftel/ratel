package com.fftw.owndata;

import com.fftw.util.DBTools;

import java.sql.Connection;
import java.sql.PreparedStatement;

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
