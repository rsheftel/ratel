package org.ratel.tsdb.loader;

import static org.ratel.mail.Email.*;
import static org.ratel.r.R.*;
import static org.ratel.schedule.JobStatus.*;
import static org.ratel.transformations.Constants.*;
import static org.ratel.util.Sequence.*;
import static org.ratel.util.Log.*;
import org.ratel.schedule.*;
import org.ratel.file.*;

public class TimeSeriesUploader {

    private final QDirectory dir;
    private final String failureAddress;

    public TimeSeriesUploader(QDirectory dir, String failureAddress) {
        this.dir = dir;
        this.failureAddress = failureAddress;
    }

    public JobStatus run() {
        r("failures <- TimeSeriesBatchUpload('" + dir.path().replaceAll("\\\\", "/") + "')$upload()");
        String[] errors = rStrings("if (exists('failures')) { strings(failures) } else { c() } ");
        String[] fileNames = errors.length == 0 ? new String[0] : rStrings("rownames(failures)");
        StringBuilder buf = new StringBuilder();
        for (int i : along(fileNames))
            buf.append(convertToWindows(fileNames[i]) + " : " + errors[i] + "\n\n");
        if (fileNames.length == 0) { info("no failures"); return SUCCESS; }
        problem("failure uploading to TSDB", buf.toString()).sendTo(failureAddress);
        return FAILED;
    }

    public static String convertToWindows(String fileName) {
        return fileName.replaceAll("^/data", "V:")
                       .replaceAll("Today", "Failures")
                       .replaceAll("/", "\\\\");
    }
    
    public static void main(String[] args) {
//        String address = "team,rokonowitz@fftw.com,klam@fftw.com,mfranz@fftw.com";
        String address = "team";
        JobStatus status = new TimeSeriesUploader(new QDirectory(dataDirectory() + "/TSDB_upload"), address ).run();
        status.exit();
    }

}
