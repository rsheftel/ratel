package malbec.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FixLogExecutionExtractor {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        //File sourceFile = new File("Y:\\tmp\\executions\\allmessages.txt");


//        File sourceFile = new File("\\\\NewYorkData.fftw.com\\users$\\MFranz\\Malbec\\EMSX-Drops.txt");
        File sourceFile = new File("C:\\Developer\\temp\\FIX-Messages\\fixmessages.log");
        
        FileReader fr = new FileReader(sourceFile);
        BufferedReader br = new BufferedReader(fr);

        String line = null;

        while ((line = br.readLine()) != null) {
            int startFix = line.indexOf("8=FIX.4.2");
            int isExecution = line.indexOf("35=8");
            if (startFix > -1 && isExecution > -1) {
                line = line.substring(startFix);
                System.out.println(line);
            }
        }

    }
}