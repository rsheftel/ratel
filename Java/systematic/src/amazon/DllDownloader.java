package amazon;

import static util.Objects.*;
import file.*;
import util.*;

public class DllDownloader {

	public static void main(String[] args) {
		Arguments arguments = Arguments.arguments(args, list("systemId", "dir"));
		EC2Runner runner = new EC2Runner(arguments.string("systemId"));
		QDirectory dir = new QDirectory(arguments.string("dir"));
		dir.createIfMissing();
		runner.downloadJarsAndQRunFromS3(dir);

	}

}
