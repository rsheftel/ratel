package malbec.fer;

import java.util.Collection;

import edu.emory.mathcs.backport.java.util.Arrays;

public enum FerretState {
    Active, Reject, Stage, Ticket, DMA, Inactive;

    @SuppressWarnings("unchecked")
    public static FerretState highest(FerretState[] statesToOrder) {
        return highest((Collection<FerretState>) Arrays.asList(statesToOrder));
    }

    public static FerretState highest(Collection<FerretState> statesToOrder) {

        FerretState highest = null;
        boolean foundActive = false;
        boolean foundInactive = false;
        for (FerretState ss : statesToOrder) {
            if (highest == null || ss.ordinal() > highest.ordinal()) {
                highest = ss;
            }

            if (ss == Active) {
                foundActive = true;
            }

            if (ss == Inactive) {
                foundInactive = true;
            }
        }

        if (foundActive && foundInactive) {
            throw new IllegalArgumentException("Cannot have both Active and Inactive in list");
        }

        return highest;
    }

    public static FerretState min(FerretState a, FerretState b) {
        if (a.ordinal() <= b.ordinal()) {
            return a;
        } else {
            return b;
        }
    }

}
