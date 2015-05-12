using System;
using file;
using java.io;
using mail;
using systemdb.portfolio;
using util;

namespace Q.Simulator {
    public class PortfolioDaily : Util.Objects {
        const string PRODUCTION = @"V:\STProcess\RightEdge\Portfolio";

        public static int Main(string[] args) {
            Log.doNotDebugSqlForever();
            var arguments = Arguments.arguments(args, jStrings("dir", "lastDate", "generateCurves", "overwrite"));
            var end = date( arguments.get("lastDate"));
            var portfolioDir = new QDirectory(arguments.get("dir", PRODUCTION));
            var generateCurves = arguments.get("generateCurves", true);
            portfolioDir.createIfMissing();
            var newDirectory = dateDirectory(portfolioDir, end);
            if (generateCurves) {
                var overwrite = arguments.get("overwrite", false);
                Portfolio.Main(new[] {
                        "-group", "AllSystemsQ",
                        "-end", ymdHuman(end),
                        "-asOf", ymdHuman(end),
                        "-dir", newDirectory,
                        "-metricSource", "internal",
                        "-overwrite", "" + overwrite
                    });
            }
            var prior = date(Dates.businessDaysAgo(1, jDate(end), "nyb"));
            var bytes = new ByteArrayOutputStream();
            Log.setBothStreams(new PrintStream(bytes));
            var rc = PortfolioTieOut.runReport(new[] {
                "-old", dateDirectory(portfolioDir, prior),
                "-new", newDirectory,
                "-ignoreLast", "true",
                "-summary", "true"
            });
            if(rc != 0) Email.problem("daily portfolio yesterday != today", bytes.ToString()).sendTo("team");
            return rc;
        }

        static string dateDirectory(QDirectory portfolioDir, DateTime end) {
            var result = portfolioDir.directory(new [] { "" + Dates.asLong(jDate(end)), "curves"});
            result.createIfMissing();
            return result.path();
        }
    }
}