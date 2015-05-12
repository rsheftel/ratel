using System;
using System.Collections.Generic;
using System.Text;

namespace TradingScreenApiService.Position
{
    /// <summary>
    /// A cache of the current positions.
    /// </summary>
    public class PositionCache
    {
        readonly IDictionary<string, IDictionary<string, string>> _positionsByStrategy = new Dictionary<string, IDictionary<string, string>>();
        public int Count {
            get {
                lock(_positionsByStrategy) {
                    return _positionsByStrategy.Count;
                }
            }
        }
        public ICollection<IDictionary<string, string>> Positions
        {
            get { return _positionsByStrategy.Values; }
        }

        /// <summary>
        /// Update the corresponding position with the new trade information
        /// </summary>
        /// <param name="tradeRecord"></param>
        /// <returns></returns>
        public IDictionary<string, string> Update(IDictionary<string, string> tradeRecord) {
            // find the position that matches this trade and update the position

            var positionKey = BuildPositionKey(tradeRecord);
            lock (_positionsByStrategy) {
                if (!_positionsByStrategy.ContainsKey(positionKey)) {

                    // we have a new record
                    var newPosition = new Dictionary<string, string>();
                    newPosition["BID"] = tradeRecord["BID"];
                    newPosition["Strategy"] = tradeRecord["Strategy"];
                    newPosition["SharesBought"] = "0";
                    newPosition["SharesSold"] = "0";
                    newPosition["Position"] = "0";

                    _positionsByStrategy.Add(positionKey, newPosition);
                }

                var position = _positionsByStrategy[positionKey];

                switch (tradeRecord["Side"]) {
                    case "Buy":
                        position["SharesBought"] = AddStrings(position["SharesBought"], tradeRecord["Quantity"]);
                        break;
                    case "Sell":
                        position["SharesSold"] = AddStrings(position["SharesSold"], tradeRecord["Quantity"]);
                        break;
                    default:
                        throw new ArgumentException("Cannot determine side: " + tradeRecord["Side"]);
                }
                position["Position"] = SubtractStrings(position["SharesBought"], position["SharesSold"]);

                return new Dictionary<string, string>(position);
            }
        }

        static string AddStrings(string addend, string augend)
        {
            // the string numbers that we get from TradingScreen are decimals (real)

            return Convert.ToString((long)(Convert.ToDecimal(addend) + Convert.ToDecimal(augend)));
        }

        static string SubtractStrings(string minuend, string subtrahend)
        {
            return Convert.ToString((long)(Convert.ToDecimal(minuend) - Convert.ToDecimal(subtrahend)));
        }

        static string BuildPositionKey(IDictionary<string, string> tradeRecord) {
            var key = new StringBuilder();
            key.Append(tradeRecord["Strategy"]).Append("-").Append(tradeRecord["BID"]);

            return key.ToString();
        }

        public void Clear() {
            lock (_positionsByStrategy) {
                _positionsByStrategy.Clear();
            }
        }
    }
}
