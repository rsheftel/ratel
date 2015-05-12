#!/usr/bin/env groovy
package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Read in a TSDB CSV file and convert it into a CSV file that is easier to read
 * in Excel.
 * 
 * This takes two lines of CSV with X number of items and converts 
 * the data into two columns of data (key,value) for X number of rows 
 */
public class Csv2Excel
{

    /**
     * @param args
     */
    public static void main (String[] args) throws Exception
    {
        def cli = new CliBuilder(usage: 'groovy util/Csv2Excel.groovy -f file');
        // If you do not want the 'longOpt' remove it
        cli.h(longOpt: 'help', 'usage information');
        cli.f(argName: 'file', longOpt: 'filename', args: 1, required: true, 'csv file to be converted, - for stdin');

        def opt = cli.parse(args);

        if (!opt) return;
        if (opt.h) cli.usage();
        
        BufferedReader br = null;
        
        if (opt.f == "-")
        {
            br = new BufferedReader(new InputStreamReader(System.in));
        } else {
            File srcFile = new File(opt.f);
            br = new BufferedReader(new FileReader(srcFile));
        }
        
        // Read each line in and parse it into an array
        List keys = new ArrayList<String>();
        String line = br.readLine();
        
        // First line
        StringTokenizer st = new StringTokenizer(line, ",");
        
        while (st.hasMoreElements()) 
        {
            keys.add((String)st.nextElement());
        }
        
        List values = new ArrayList<String>();    
        line = br.readLine();
        
        // Second line
        st = new StringTokenizer(line, ",");
        
        while (st.hasMoreElements()) 
        {
            values.add((String)st.nextElement());
        }

        // Since the lines are the same length, spit out an element from
        // each line one at a time
        for (int i in 0..keys.size()-1)
        {
            String key = keys.get(i);
            String value = values.get(i);
            System.out.println(key+","+value);
        }
    }

}
