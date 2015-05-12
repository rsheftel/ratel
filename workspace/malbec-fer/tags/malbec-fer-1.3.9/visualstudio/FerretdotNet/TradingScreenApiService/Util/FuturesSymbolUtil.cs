using System;
using System.Text;
using System.Text.RegularExpressions;

namespace TradingScreenApiService.Util {

    /// <summary>
    /// Utilities for manipulating futures symbols
    /// </summary>
    public static class FuturesSymbolUtil {
        static readonly log4net.ILog _log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        static readonly Regex _basicPattern = new Regex(@"^[A-Za-z|\d][A-Za-z ][A-Za-z]\d$");
        static readonly Regex _bloombergYellowKey = new Regex(@"[A-Za-z|\d][A-Za-z ][A-Za-z]\d[ ][A-Za-z]+");
        static readonly Regex _rootOnly = new Regex(@"^[A-Za-z|\d][A-Za-z ]$");
        static readonly Regex _ricPattern = new Regex(@"[A-Za-z|\d][A-Za-z][A-Za-z][A-Za-z]\d");
        static readonly Regex _optionPattern = new Regex(@"[A-Za-z|\d][A-Za-z][A-Za-z]\d[CPcp]");
        static readonly Regex _optionsRootPattern = new Regex(@"[A-Za-z|\d][A-Za-z][_][CPcp]");
        static readonly Regex _weirdOptionPattern = new Regex(@"[A-Za-z|\d][A-Za-z ][A-Za-z]\d[ ]\d\d");
        static readonly Regex _complexRic = new Regex(@"[A-Za-z|\d][A-Za-z ][A-Za-z]\d[:][A-Za-z][A-Za-z]");
        static readonly char[] _colonSeparator = new [] {':'};
        static readonly char[] _spaceSeparator = new[] { ' ' };

        /// <summary>
        /// Combine a symbol root with a maturity month and year.
        /// 
        /// The year can only be 1 digit as per Bloomberg convention.
        /// </summary>
        /// <param name="symbolRoot"></param>
        /// <param name="maturityMonthYear"></param>
        /// <returns></returns>
        public static string CombineRootMaturityMonthYear(string symbolRoot, string maturityMonthYear) {
            var yearString = maturityMonthYear.Substring(0, 4);
            var year = Convert.ToInt32(yearString.Substring(2));

            // Bloomberg requires single digit years
            if (year >= 10) year = year - (year / 10) * 10;

            var month = Convert.ToInt32(maturityMonthYear.Substring(4, 2));
            var monthCode = MonthToMonthCode(month);

            // Handle RIC root that specify the exchange - really for UX (VX:VE) only
            var parts = symbolRoot.Split(_colonSeparator);

            if (parts[0].Length == 1) {
                parts[0] = parts[0] + " ";
            }
            if (parts.Length == 2) {
                var sb = new StringBuilder(50);
                sb.Append(parts[0]).Append(monthCode).Append(year).Append(":").Append(parts[1]);

                return sb.ToString();
            }
            return parts[0] + monthCode + year;
        }

        /// <summary>
        /// Convert the futures character code to a numeric number for the month.
        /// </summary>
        /// <param name="monthCode"></param>
        /// <returns></returns>
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

        /// <summary>
        /// Convert the numeric month to a futures character code.
        /// </summary>
        /// <param name="month"></param>
        /// <returns></returns>
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

        /// <summary>
        /// Extract the root for this symbol.
        /// 
        /// This handles the custom futures option root pattern of [Root][Root]_[Option] that
        /// we use for aggregating positions by root.
        /// 
        /// </summary>
        /// <param name="futuresSymbol"></param>
        /// <returns></returns>
        public static string ExtractSymbolRoot(String futuresSymbol)
        {
            // [Root][Root|Space][Month][Year][:][Root][Root]
            if (_complexRic.IsMatch(futuresSymbol)) {
                var colonPos = futuresSymbol.LastIndexOf(':');
                if (colonPos > 1) {
                    return RemoveMonthAndYear(futuresSymbol.Substring(0, colonPos));
                }
                _log.Error("Identified as RIC with colon (:) but unable to extract root: " + futuresSymbol);
                return futuresSymbol;
            }

            // [Root][Root|Space][Month][Year]
            if (_basicPattern.IsMatch(futuresSymbol)) {
                return RemoveMonthAndYear(futuresSymbol);
            } 
            
            // [Root][Root|Space][Month][Year][Space][Yellow Key]
            if (_bloombergYellowKey.IsMatch(futuresSymbol)) {
                var lastSpace = futuresSymbol.LastIndexOf(' ');
                if (lastSpace > 1) {
                    return RemoveMonthAndYear(futuresSymbol.Substring(0, lastSpace));
                }
//                _log.Error("Identified as futures root with Yellow Key but unable to extract root: " + futuresSymbol);
                Console.WriteLine("Identified as futures root with Yellow Key but unable to extract root: " + futuresSymbol);

                return futuresSymbol;
            } 
            
            // [Root][Root|Space]
            if (_rootOnly.IsMatch(futuresSymbol)) {
                return futuresSymbol.Trim();
            } 
            
            // [Root][Root][Root][Month][Year] -- this is a 3 letter RIC root
            if (_ricPattern.IsMatch(futuresSymbol)) {
                return RemoveMonthAndYear(futuresSymbol);
            } 
            
            // [Root][Root][Month][Year][Option] -- Futures option
            if (_optionPattern.IsMatch(futuresSymbol)) {
                // System.err.println("We have a futures option symbol : " + futuresSymbol);
                // Save the option part to add to the root
                var option = futuresSymbol.Substring(futuresSymbol.Length - 1, 1);
                var root = RemoveMonthAndYear(futuresSymbol.Substring(0, futuresSymbol.Length - 1));

                return root + "_" + option;
            } 
            
            // [Root][Root][_][Option] -- Our version of the futures options root
            if (_optionsRootPattern.IsMatch(futuresSymbol)) {
                return futuresSymbol.Trim();
            } 
            
            // [Root][Root][Month][Year]_[\d][\d] -- Weird option
            if (_weirdOptionPattern.IsMatch(futuresSymbol)) {
                var lastSpace = futuresSymbol.LastIndexOf(' ');
                if (lastSpace > 1) {
                    return RemoveMonthAndYear(futuresSymbol.Substring(0, lastSpace));
                }
//                _log.Error("Identified as weird futures root but unable to extract root: " + futuresSymbol);
                Console.WriteLine("Identified as weird futures root but unable to extract root: " + futuresSymbol);
                return futuresSymbol;
            }
//            _log.Warn("Unknown futures symbol: " + futuresSymbol + " using entire symbol");
            Console.WriteLine("Unknown futures symbol: " + futuresSymbol + " using entire symbol");
            return futuresSymbol;
        }

        public static string ExtractMaturityMonthFromSymbol(string symbol)
        {
            // These is probably a better way to do this...
            var colonParts = symbol.Split(_colonSeparator);
            symbol = colonParts[0];

            var symbolParts = symbol.Split(_spaceSeparator);
            var firstPart = symbolParts[0];

            // We had a single letter root with a space
            if (firstPart.Length == 1 && symbolParts.Length > 1) {
                firstPart = symbolParts[0] + " " + symbolParts[1];
            }

            var monthStr = firstPart.Substring(firstPart.Length - 2, 1);
            var month = string.Format("{0:00}", MonthCodeToMonth(monthStr[0]));
            var yearStr = firstPart.Substring(firstPart.Length - 1, 1);


            var year = 2000 + Convert.ToInt32(yearStr);
            var currentYear = DateTime.Now.Year;

            while (year < currentYear) {
                year += 10;
            }

            return Convert.ToString(year) + month;
        }

        private static String RemoveMonthAndYear(string futuresSymbol)
        {
            return futuresSymbol.Substring(0, Math.Max(2, futuresSymbol.Length - 2)).Trim();
        }
    }
}