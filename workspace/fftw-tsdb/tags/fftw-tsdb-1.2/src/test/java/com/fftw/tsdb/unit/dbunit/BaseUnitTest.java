package com.fftw.tsdb.unit.dbunit;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;


public class BaseUnitTest
{
    protected IDatabaseTester databaseTester;
    
    @BeforeClass(groups = {"unittest"})
    protected void setUp() throws DatabaseUnitException, SQLException, Exception{
        databaseTester =  new JdbcDatabaseTester("org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:.", "sa", "");
        
    }
    
    protected IDatabaseConnection getConnection() throws Exception {
        return databaseTester.getConnection();
    }
    
    protected IDataSet getDataSet () throws Exception
    {
        return new FlatXmlDataSet(this.getClass().getResourceAsStream("/dataset.xml"));
    }
    
    @AfterClass(groups = {"unittest"})
    public void tearDown() throws SQLException, Exception {
//        System.out.println("---------calling tear down-------------");
        getConnection().close();
    }
}
