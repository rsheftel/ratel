package file;

import static util.Errors.*;

public class SplitCsv {

    public static void main(String[] args) {
        if(args.length != 3) usage();
        QFile file = new QFile(args[0]);
        Csv csv = new Csv(file, true);
        String prefix = file.basename();
        prefix = prefix.replaceAll(".csv$", "");
        csv.split(Integer.parseInt(args[1]), new QDirectory(args[2]), prefix);
    }

    private static void usage() {
        bomb("\nUsage: $0 <classname> <fileToSplit> <maxLinesPerFile> <targetDir>\n");
    }

}
