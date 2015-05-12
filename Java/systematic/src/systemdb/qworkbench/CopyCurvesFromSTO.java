package systemdb.qworkbench;

import static util.Arguments.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import file.*;
import util.*;

public class CopyCurvesFromSTO {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Arguments arguments = arguments(args, list("curveDir", "runs", "outDir"));
        QDirectory curveDir = new QDirectory(arguments.get("curveDir"));
        List<String> runs = split(",", arguments.get("runs"));
        QDirectory outDir = new QDirectory(arguments.get("outDir"));
        
        for (QDirectory msivDir : curveDir.directories()) {
            if(!msivDir.name().matches(".*XL.*")) continue; 
            for (String run : runs) {
                QFile curveFile = msivDir.file("run_" + run + ".bin");
                // LiqInj_1.0_daily_PTT10.XLBXLI - basename of msivDir
                // 1686 - run
                String base = msivDir.name();
                String name= base.replaceFirst("PTT10", "LiqInjETF" + run + "_PTT10") + ".bin";
                // LiqInj_1.0_daily_LiqInjETF1686_PTT10.XLBXLI.bin
                info("copy " + curveFile + " to " + outDir.file(name));
                curveFile.copyTo(outDir.file(name));
            }
        }
        
    }

}
