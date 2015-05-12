package malbec.util;

import org.joda.time.LocalDate;

public class FuturesSymbolUtil {

    
    public static String extractSymbolRoot(String futuresSymbol) {
        // Strip off the Bloomberg Yellow key
        String tmpSymbol = futuresSymbol.trim().toUpperCase();
        int lastSpace = tmpSymbol.lastIndexOf(' ');
        if (lastSpace > 1) {
            // remove the month, year and yellow key
            tmpSymbol = tmpSymbol.substring(0, lastSpace-2);
        } else if (tmpSymbol.length() > 1) {
            // throw away the month and year
            tmpSymbol = tmpSymbol.substring(0, Math.max(2, tmpSymbol.length() - 2)).trim();
        } else {
            tmpSymbol = tmpSymbol.substring(0, Math.max(1, tmpSymbol.length() - 2)).trim();
        }
        
        return tmpSymbol;
        // in case we have a 1 character symbols, use the length
        //return futuresSymbol.substring(0, Math.min(2, futuresSymbol.length())).toUpperCase().trim();
    }
    
    /**
     * TYM9 Comdty
     * TYM9
     * P M9
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
        String yearStr = firstPart.substring(firstPart.length()-1, firstPart.length());
        
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
        return symbolRoot+monthCode+ year;
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
                throw new IllegalArgumentException("Invalid trade trade month for futures trade: " + month);
        }
    }

}
