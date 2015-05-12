package schedule;

import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;

import java.util.*;

import schedule.JobTable.*;
import schedule.dependency.*;
import file.*;

class StatusReportFile {
    static final String GREEN = "lightgreen";
    static final String YELLOW = "#FFFF66";
    static final String RED = "red";
    static final String WHITE = "white";
    static final String CYAN = "#99FFFF";
    static final List<String> STATUS_PRIORITY = list(RED, YELLOW, CYAN, WHITE, GREEN);
    private static final String DEPENDENCY_COLOR = "#EEEEEE";
    private static final String JOB_COLOR = "#CCCCCC";
    private static final String SEA_GREEN = "#66FFCC";

    private final List<Job> jobs;
    private final Date asOf;
    private final QDirectory dir;
    private StringBuilder html = new StringBuilder();
    private int indent = 0;

    public StatusReportFile(QDirectory dir, List<Job> jobs, Date time) {
        this.dir = dir;
        this.jobs = jobs;
        this.asOf = midnight(time);
    }
    
    public void create() {
        html();
        QFile file = dir.file("status" + fileDate(asOf) + ".html");
        file.deleteIfExists();
        file.create(html.toString());
        info("wrote report file at " + file.path());
        if(asOf.equals(midnight(now()))) {
            QFile mainStatusFile = dir.file("status.html");
            mainStatusFile.deleteIfExists();
            mainStatusFile.create(html.toString());
            info("wrote report file at " + mainStatusFile.path());
        }
    }
    public String html() {
        html = new StringBuilder();
        addHeader();
        Collections.sort(jobs, new Comparator<Job>() {

            @Override public int compare(Job o1, Job o2) {
                Date d1 = o1.statusTime(asOf);
                Date d2 = o2.statusTime(asOf);
                if(d2 == null) return -1;
                if(d1 == null) return 1;
                return d2.compareTo(d1);
            } 
            
        });
        for (Job job : jobs) {
            indent("<tr bgcolor=" + JOB_COLOR + ">");
            td("" + job.id());
            td(a(job.name(), "file://///nyux51/data/logs/Scheduler/" + job.logDirName()));
            td(job.action().getClass().getSimpleName());
            td(job.deadlineTime());
            td(ymdHuman(job.statusTime(asOf)));
            JobStatus status = job.status(asOf);
            String statusColor = color(status);
            if (statusColor.equals(WHITE)) statusColor = JOB_COLOR;
            line("<td bgcolor=" + statusColor + " align=center>" + orNbsp(true, status.toString()) + "</td>");
            outdent("</tr>");
            addDependencies(job);
        }
        outdent("</table></html>");
        return html.toString();
    }

    private String allStatusColor() {
        String result = GREEN; 
        for (Job job : jobs) {
            JobStatus s = job.status(asOf);
            if (s.isSuccess()) continue;
            if (s.isHoliday()) continue;
            if (s.isFailed()) result = downgrade(result, RED);
            if (s.isLate()) result = downgrade(result, YELLOW);
            if (s.inProgress()) result = downgrade(result, CYAN);
            result = downgrade(result, WHITE);
        }
        return result;
    }

    static String downgrade(String cumulative, String current) {
        if (STATUS_PRIORITY.indexOf(cumulative) < STATUS_PRIORITY.indexOf(current)) return cumulative;
        return current;
    }

    private void addHeader() {
        String completed = completedString();
        String allStatusColor = allStatusColor();
        indent(
            "<html><meta http-equiv='refresh' content=30/>" +
            "<script>" +
                "function loadDate() {" +
                "    var box = document.getElementById(\'dateBox1\').value;" +
                "    if (!box.match(/^status/)) { box = 'status' + box.replace(/\\D+/g, '') + '.html'; }" +
                "    window.location = box;  " +
                "}" + 
            "</script>" + 
            "<table width=\'80%\' bgcolor='" + allStatusColor + "'>" +
                "<tr><td width='10%'><b>Data Date</b></td><td>&nbsp;</td><td>" + ymdHuman(midnight(asOf)) + "</td>" +
                    "<td align=right>" + previousTag() + "&nbsp;" + nextTag() +  "</td></tr>" +
                "<tr><td><b>Last Updated</b></td><td>&nbsp;</td><td>" + yyyyMmDdHhMmSs(now()) + "</td>" + 
                    "<td align=right>" + completed + " <input id='dateBox1' type='text' name='dateBox' value='" + ymdHuman(midnight(asOf)) + "'/><input type='submit' value='Go' onclick='loadDate();' id='goRef'/></td></tr>" + 
            "</table>" +
            "<a href='file://///nyux51/data/contactinfo.txt'>Emergency support info</a>" +
            "<hr align=left width='80%'/>" +
            "<table border=0 cellpadding=2 cellspacing=0 width=80%>"
        );
        addTableHeader();
    }


    private String completedString() {
        int numDone = 0;
        for(Job j : jobs) 
            if (j.status(asOf).isSuccess() || j.status(asOf).isHoliday()) numDone++;
        return "completed " + numDone + (numDone == jobs.size() ? "" : " of " + jobs.size());
    }

    private String previousTag() {
        String fileName = "status" + fileDate(daysAgo(1, asOf)) +  ".html";
        String disabled = dir.file(fileName).exists() ? "" : " disabled=true ";
        return "<button " + disabled + "onClick=\"window.location='" + fileName + "'\">Previous</button>";
    }

    private String nextTag() {
        String fileName = "status" + fileDate(daysAhead(1, asOf)) +  ".html";
        String disabled = dir.file(fileName).exists() ? "" : " disabled=true ";
        return "<button " + disabled + "onClick=\"window.location='" + fileName + "'\">Next</button>";
    }
    
    private String fileDate(Date d) {
        return yyyyMmDd(d).replaceAll("\\D+", "");
    }

    private String a(String text, String link) {
        return "<a href=" + sQuote(link) + ">" + text + "</a>";
    }

    private void addTableHeader() {
        indent("<tr bgcolor=" + JOB_COLOR + ">");
        th("Job ID");
        th("Job Name");
        th("Action");
        th("Deadline");
        th("Last Update (Job)");
        thCenter("Job Status");
        outdent("</tr>");
        indent("<tr bgcolor=" + DEPENDENCY_COLOR +  ">");
        th("");
        th("");
        th("Dependency ID");
        th("Dependency Type");
        th("Last Update (Dependency)");
        thCenter("Dependency Status");
        outdent("</tr>");
        indent("<tr>");
        th("");
        th("");
        th("");
        th("Dependency Parameter");
        th("Parameter Value");
        th("");
        outdent("</tr>");
    }


    private void addDependencies(Job job) {
        List<Dependency> dependencies = job.dependencies();
        for(Dependency dep : dependencies) {
            indent("<tr bgcolor=" + DEPENDENCY_COLOR + ">");
            td("");
            td("");
            td("" + dep.id());
            td(dep.getClass().getSimpleName());
            String timeString = dep.hasStatusEntry(asOf) ? ymdHuman(dep.statusTime(asOf)) : "";
            td(timeString);
            JobStatus status = dep.status(asOf);
            String statusColor = color(status);
            if (statusColor.equals(WHITE)) statusColor = DEPENDENCY_COLOR;
            line("<td bgcolor=" + statusColor + " align=center>" + orNbsp(true, status.toString()) + "</td>");
            outdent("</tr>");
            addParameters(dep);
        }
    }

    private void addParameters(Dependency dep) {
        Map<String, String> parameters = dep.parameters();
        for(String name : parameters.keySet()) {
            indent("<tr>");
            td("");
            td("");
            td("");
            td(name);
            td(parameters.get(name));
            td("");
            outdent("</tr>");
        }
    }

    private void th(String fieldName) {
        line("<th align=left>" + fieldName + "</th>");
    }
    
    private void thCenter(String fieldName) {
        line("<th align=center>" + fieldName + "</th>");
    }
    
    private void td(String field) {
        line("<td>" + field + "</td>");
    }

    private String color(JobStatus status) {
        switch (status) {
            case SUCCESS:
            case NOT_BUSINESS_DAY:
                return GREEN;
            case IN_PROGRESS:
                return SEA_GREEN;
            case RETRY_NEXT_RUN:
                return YELLOW;
            case LATE:
            case IN_PROGRESS_LATE:
            case FAILED:
            case BLOCKED:
            case BLOCKED_LATE:
            case CANCELLED:
                return RED;
            case NOT_STARTED:
            case RESTART:
                return CYAN;
            default:
                return WHITE;
        }
    }

    private String orNbsp(boolean hadContent, String field) {
        return hadContent && isEmpty(field) ? "&nbsp;" : field;
    }

    private void outdent(String line) {
        indent--;
        line(line);
    }

    private void indent(String line) {
        line(line);
        indent++;
    }

    private void line(String line) {
        html.append(join("", Collections.nCopies(indent, "    ")) + line + "\n");
    }
    
    
}