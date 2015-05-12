package malbec.util;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuturesSymbolUtil {

    public static final Logger log = LoggerFactory.getLogger(FuturesSymbolUtil.class);

    public static String extractSymbolRoot(String futuresSymbol) {

        // [Root][Root|Space][Month][Year]
        if (futuresSymbol.matches("[A-Za-z|\\d][A-Za-z ][A-Za-z]\\d")) {
            // System.err.println("We have a futures symbol: " + futuresSymbol);
            return removeMonthAndYear(futuresSymbol);
        } // [Root][Root|Space][Month][Year][Space][Yellow Key]
        else if (futuresSymbol.matches("[A-Za-z|\\d][A-Za-z ][A-Za-z]\\d[ ][A-Za-z]+")) {
            // System.err.println("We have a futures symbol with yellow key: " + futuresSymbol);
            int lastSpace = futuresSymbol.lastIndexOf(' ');
            if (lastSpace > 1) {
                return removeMonthAndYear(futuresSymbol.substring(0, lastSpace));
            } else {
                log.error("Identified as futures root with Yellow Key but unable to extract root: "
                    + futuresSymbol);
                return futuresSymbol;
            }
        } // [Root][Root|Space]
        else if (futuresSymbol.matches("[A-Za-z|\\d][A-Za-z ]*")) {
            // System.err.println("We have a futures symbol root: " + futuresSymbol);
            return futuresSymbol.trim();
        } // [Root][Root][Root][Month][Year] -- this is a 3 letter RIC root
        else if (futuresSymbol.matches("[A-Za-z|\\d][A-Za-z][A-Za-z][A-Za-z]\\d")) {
            // System.err.println("We have a futures 3 letter RIC symbol : " + futuresSymbol);
            return removeMonthAndYear(futuresSymbol);
        } // [Root][Root][Month][Year][Option] -- Futures option
        else if (futuresSymbol.matches("[A-Za-z|\\d][A-Za-z][A-Za-z]\\d[CPcp]")) {
            // System.err.println("We have a futures option symbol : " + futuresSymbol);
            // Save the option part to add to the root
            String option = futuresSymbol.substring(futuresSymbol.length() - 1, futuresSymbol.length());
            String root = removeMonthAndYear(futuresSymbol.substring(0, futuresSymbol.length() - 1));

            return root + "_" + option;
        } // [Root][Root][_][Option] -- Our version of the futures options root
        else if (futuresSymbol.matches("[A-Za-z|\\d][A-Za-z][_][CPcp]")) {
            // System.err.println("We have a futures option root : " + futuresSymbol);
            return futuresSymbol.trim();
        } // // [Root][Root][Month][Year]_[\\d][\\d] -- Weird option
        else if (futuresSymbol.matches("[A-Za-z|\\d][A-Za-z ][A-Za-z]\\d[ ]\\d\\d")) {
            int lastSpace = futuresSymbol.lastIndexOf(' ');
            if (lastSpace > 1) {
                return removeMonthAndYear(futuresSymbol.substring(0, lastSpace));
            } else {
                log.error("Identified as weird futures root but unable to extract root: " + futuresSymbol);
                return futuresSymbol;
            }
        }
        log.warn("Unknown futures symbol: " + futuresSymbol + " using entire symbol");
        return futuresSymbol;
    }

    private static String removeMonthAndYear(String futuresSymbol) {
        return futuresSymbol.substring(0, Math.max(2, futuresSymbol.length() - 2)).trim();
    }

    /**
     * TYM9 Comdty TYM9 P M9
     * 
     * @param symbol
     * @return
     */
    public static String bloombergToMaturityMonthYear(String symbol) {
        if (symbol.trim().length() < 4) {
            throw new IllegalArgumentException("Futures symbol is too small to contain valid symbol");
        }

        String tmpSymbol = symbol.trim().toUpperCase();
        int lastSpace = tmpSymbol.lastIndexOf(' ');
        if (lastSpace > 1) {
            // remove the yellow key
            tmpSymbol = tmpSymbol.substring(0, lastSpace);
        }

        int strLength = tmpSymbol.length();
        char yearStr = tmpSymbol.charAt(strLength - 1);
        char monthCode = tmpSymbol.charAt(strLength - 2);

        LocalDate today = new LocalDate();

        int shortYear = yearStr - '0';
        int year = shortYear + 2000;

        if (year < today.getYear()) {
            int adjustment = (today.getYear() - year) / 10 + shortYear + 1;
            year = today.getYear() + adjustment;
        }
        int month = codeToMonth(monthCode);

        LocalDate ld = new LocalDate(year, month, 1);

        return ld.toString("yyyyMM");
    }

    public static String extractMaturityMonthFromSymbol(String symbol) {
        String[] symbolParts = symbol.split(" ");
        String firstPart = symbolParts[0];

        String monthStr = firstPart.substring(firstPart.length() - 2, firstPart.length() - 1);
        String month = String.format("%02d", codeToMonth(monthStr.charAt(0)));
        String yearStr = firstPart.substring(firstPart.length() - 1, firstPart.length());

        int year = 2000 + Integer.parseInt(yearStr);

        return String.valueOf(year) + month;
    }

    public static String combineRootMaturityMonthYear(String symbolRoot, String maturityMonthYear) {
        LocalDate ld = DateTimeUtil.getLocalDate(maturityMonthYear + "01");

        int year = ld.getYearOfCentury();
        // Bloomberg requires single digit years
        if (year >= 10) {
            year = year - (year / 10) * 10;
        }
        int month = ld.getMonthOfYear();
        char monthCode = monthToCode(month);

        if (symbolRoot.length() == 1) {
            symbolRoot = symbolRoot + " ";
        }
        return symbolRoot + monthCode + year;
    }

    public static int codeToMonth(char monthCode) {
        switch (monthCode) {
            case 'F': // January
                return 1;
            case 'G': // February
                return 2;
            case 'H': // March
                return 3;
            case 'J': // April
                return 4;
            case 'K': // May
                return 5;
            case 'M': // June
                return 6;
            case 'N': // July
                return 7;
            case 'Q': // August
                return 8;
            case 'U': // September
                return 9;
            case 'V': // October
                return 10;
            case 'X': // November
                return 11;
            case 'Z': // December
                return 12;
            default:
                throw new IllegalArgumentException("Invalid trade month code for futures trade: " + monthCode);
        }
    }

    public static char monthToCode(int month) {
        switch (month) {
            case 1: // January
                return 'F';
            case 2: // February
                return 'G';
            case 3: // March
                return 'H';
            case 4: // April
                return 'J';
            case 5: // May
                return 'K';
            case 6: // June
                return 'M';
            case 7: // July
                return 'N';
            case 8: // August
                return 'Q';
            case 9: // September
                return 'U';
            case 10: // October
                return 'V';
            case 11: // November
                return 'X';
            case 12: // December
                return 'Z';
            default:
                throw new IllegalArgumentException("Invalid trade month for futures trade: " + month);
        }
    }

}
