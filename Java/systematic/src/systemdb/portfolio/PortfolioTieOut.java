package systemdb.portfolio;

import static util.Arguments.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Objects.*;
import static util.Strings.*;
import static util.Errors.*;
import java.util.*;

import file.*;

import sto.*;
import tsdb.*;
import util.*;


public class PortfolioTieOut {


    private Curve oldCurve;
    private Curve newCurve;
    private Observations oldPnl;
    private Observations newPnl;
    private Observations oldPosition;
    private Observations newPosition;
    private String name;

    public PortfolioTieOut(String oldFileName, String newFileName) {
        name = paren(new QFile(newFileName).name());
        oldCurve = new Curve(oldFileName);
        newCurve = new Curve(newFileName);
        oldPnl = oldCurve.pnlObservations();
        newPnl = newCurve.pnlObservations();
        oldPosition = oldCurve.positionObservations();
        newPosition = newCurve.positionObservations();
    }
    
    public static void main(String[] args) {
        System.exit(runReport(args));
    }

	public static int runReport(String[] args) {
		Arguments arguments = arguments(args, list("old", "new", "firstTrade", "delta", "summary", "pattern", "pnlFilter", "positionFilter", "ignoreLast"));
        QDirectory oldDir = new QDirectory(arguments.get("old"));
        QDirectory newDir = new QDirectory(arguments.get("new"));
        boolean firstTrade = arguments.get("firstTrade", false); 
        boolean delta = arguments.get("delta", false);
        boolean summary = arguments.get("summary", false);
        double pnlFilter = arguments.get("pnlFilter", 0.01);
        int positionFilter = arguments.get("positionFilter", 0);
        boolean ignoreLast = arguments.get("ignoreLast", false);
        String pattern = arguments.get("pattern", "");
        boolean allSuccessful = true;
        List<QFile> matchingFiles = newDir.files(".*" + pattern + ".*");
        bombIf(matchingFiles.isEmpty(), "no files matching .*" + pattern + ".*" + " in \n" + newDir.path());
        
		for (QFile newFile : matchingFiles) {
		    if (newFile.name().equals("group.xml")) continue;
		    QFile oldFile = null;
			String oldFileName = newFile.basename().replaceAll("\\.bin", "");
			if (oldDir.exists(oldFileName + ".bin"))
				oldFile = oldDir.file(oldFileName + ".bin");
			else if (oldDir.exists(oldFileName + ".csv"))
				oldFile = oldDir.file(oldFileName + ".csv");
			else {
        		lineEnd("firstBadTrade:         NA total diff:      MISSING " + oldDir.file(oldFileName).name() + " does not exist [no csv or bin file].");
        		allSuccessful = false;
        		continue;
        	}
            PortfolioTieOut tie = new PortfolioTieOut(oldFile.path(), newFile.path());
            allSuccessful &= tie.report(firstTrade, delta, summary, pnlFilter, positionFilter, ignoreLast);
        }
        return allSuccessful ? 0 : -1;
	}

    private boolean report(
        boolean firstTrade, 
        boolean delta, 
        boolean summary, 
        double pnlFilter, 
        int positionFilter,
        boolean ignoreLast
    ) {
        Range covered = oldPnl.dateRange().union(newPnl.dateRange());
        if(ignoreLast) covered = new Range(covered.start(), daysAgo(1, covered.end()));
        Date badTradeDate = null;
        int context = 10;
        double pnlDelta = 0;

        for(Date d : covered) {
            if (!oldPnl.has(d) && !newPnl.has(d)) continue;
            if (
                oldPnl.has(d) && newPnl.has(d) && 
                oldPnl.value(d) == newPnl.value(d) && 
                oldPosition.value(d) == newPosition.value(d)) continue;
            
            String message = "";
            if (!oldPnl(d).equals(newPnl(d))) {
                if (newPnl.has(d) && oldPnl.has(d)) {
                    double currentDelta = newPnl.value(d) - oldPnl.value(d);
                    pnlDelta += currentDelta;
                    if (delta && Math.abs(currentDelta) > pnlFilter ) 
                    	message +=    " PNL delta: " + leftSpacePad(20, nDecimals(2, currentDelta)) + leftSpacePad(5, "");
                } else if (delta) message += " PNL delta: NA" + leftSpacePad(23, "");
                if (!delta) message +=       " PNL O:" + oldPnl(d) + " N:" + newPnl(d);
            }

            if (!oldPosition(d).equals(newPosition(d))) {
                if (!oldPosition.has(d) || !newPosition.has(d) || Math.abs(oldPosition.value(d) - newPosition.value(d)) > positionFilter) {
                    message += " POS O:" + oldPosition(d) + " N:" + newPosition(d);
                    if (oldPosition.has(d) && newPosition.has(d) && badTradeDate == null) 
                        badTradeDate = d;
                }
            }
            if (!summary && hasContent(message)) info(ymdHuman(d) + message);
            if (firstTrade && badTradeDate != null && context-- < 0) break;
        }
        if (!firstTrade) {
        	String pnlDeltaMessage = "";
        	String badTradeMessage = "";
        	if (!summary || Math.abs(pnlDelta) > 1) pnlDeltaMessage = sprintf("total diff: %12.2f", pnlDelta);
        	if (badTradeDate != null) badTradeMessage = "firstBadTrade: " + ymdHuman(badTradeDate);
        	if (hasContent(badTradeMessage) || hasContent(pnlDeltaMessage)) {
        		lineEnd(badTradeMessage + " " + pnlDeltaMessage + " " + name);
        	}
        }
        return badTradeDate == null && pnlDelta < 1;
    }

    private String oldPnl(Date d) {
        return pnl(oldPnl, d);
    }

    private String oldPosition(Date d) {
        return position(oldPosition, d);
    }
    
    private String newPosition(Date d) {
        return position(newPosition, d);
    }
    
    private String newPnl(Date d) {
        return pnl(newPnl, d);
    }
    
    private String pnl(Observations o, Date d) {
        String string = o.has(d) ? nDecimals(2, o.value(d)) : "missing";
        return leftSpacePad(20, string);
    }


    private String position(Observations o, Date d) {
        String string = o.has(d) ? nDecimals(0, o.value(d)) : "NA";
        return leftSpacePad(4, string);
    }
    
}
