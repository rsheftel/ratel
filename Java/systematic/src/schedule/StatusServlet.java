package schedule;

import static schedule.JobTable.*;
import static util.Dates.*;
import static util.Errors.*;

import java.io.*;

import javax.servlet.http.*;

public class StatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.getWriter().write(new StatusReportFile(null, JOBS.jobs(), midnight()).html());
        } catch (IOException e) {
            throw bomb("", e);
        }
    }
}
