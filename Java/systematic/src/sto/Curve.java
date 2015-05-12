package sto;

import java.util.*;

import tsdb.*;
import util.*;
import file.*;
import static util.Dates.*;
import static util.Log.*;
import static util.Sequence.*;
import static util.Strings.*;

public class Curve {

    public CurveFile data;

    public Curve(String fileName) {
        this(new CurveFile(new QFile(fileName)));
    }

    public Curve(CurveFile data) {
        this.data = data;
        data.load();
    }
    
    public static void main(String[] args) {
        Curve curve = new Curve(args[0]);
        Observations pnl = curve.pnlObservations();
        Observations position = curve.positionObservations();
        for(Date d : pnl) {
            info(
                ymdHuman(d) + " " + 
                leftSpacePad(10, nDecimals(2, pnl.value(d))) + " " + 
                leftSpacePad(5, nDecimals(0, position.value(d))));
        }
    }

    public Observations pnlObservations() {
        Observations result = new Observations();
        for(int i : zeroTo(data.count()))
            result.set(data.jDate(i), data.pnl(i));
        return result;
    }
    
    public Observations positionObservations() {
        Observations result = new Observations();
        for(int i : zeroTo(data.count()))
            result.set(data.jDate(i), data.position(i));
        return result;
    }

    public long dateCount() {
        return data.count();
    }

    public Range dateRange() {
        return pnlObservations().dateRange();
    }
    

}
