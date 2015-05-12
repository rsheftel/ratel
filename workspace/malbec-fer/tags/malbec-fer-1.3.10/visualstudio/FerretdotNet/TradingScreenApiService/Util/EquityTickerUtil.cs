using System.Text.RegularExpressions;

namespace TradingScreenApiService.Util
{
    /// <summary>
    /// Utilities for manipulating equity tickers
    /// </summary>
    public static class EquityTickerUtil
    {
        static readonly Regex _ricSplitter = new Regex(@"\.");

        public static string ConvertRicToTicker(string ric) {
            var ticker = _ricSplitter.Split(ric);
            return ticker.Length > 1 ? ticker[0] : ric;
        }
    }
}
