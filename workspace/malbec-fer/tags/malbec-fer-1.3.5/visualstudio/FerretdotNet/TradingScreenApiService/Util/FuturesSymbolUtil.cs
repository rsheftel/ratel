using System;

namespace TradingScreenApiService.Util {

    /// <summary>
    /// Utilities for manipulating futures symbols
    /// </summary>
    public static class FuturesSymbolUtil {

        public static string CombineRootMaturityMonthYear(string symbolRoot, string maturityMonthYear) {
            var yearString = maturityMonthYear.Substring(0, 4);
            var year = Convert.ToInt32(yearString.Substring(2));

            // Bloomberg requires single digit years
            if (year >= 10) year = year - (year / 10) * 10;

            var month = Convert.ToInt32(maturityMonthYear.Substring(4, 2));
            var monthCode = MonthToMonthCode(month);

            if (symbolRoot.Length == 1) symbolRoot = symbolRoot + " ";

            return symbolRoot + monthCode + year;
        }

        public static int MonthCodeToMonth(char monthCode) {
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
                    throw new ArgumentException("Invalid month code: " + monthCode, "monthCode");
            }
        }

        public static char MonthToMonthCode(int month) {
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
                    throw new ArgumentException("Invalid month: " + month, "month");
            }
        }
    }
}