using System.Text.RegularExpressions;

namespace TradingScreenApiService.Util
{
    /// <summary>
    /// Utilities for manipulating equity tickers
    /// </summary>
    public static class EquityTickerUtil
    {
        static Regex ricSplitter = new Regex(@"\.");

        public static string ConvertRicToTicker(string ric) {
            var ticker = ricSplitter.Split(ric);
            return ticker.Length > 1 ? ticker[0] : ric;
        }
    }
}
