
namespace RediToActiveMQ
{
    static class RediTableConstants
    {

        internal static string[] MESSAGE_COLUMN_NAMES =
        {
            "Memo", "ClientData", "OrderRefKey", "Status", "ExecLeaves", "Time", "EXECQUANTITY",
            "ExecPrice", "BranchSequence", "OmsRefCorrId", "OmsRefLineId", "OmsRefLineSeq",
            "RefNum", "Side", "Symbol", "ENTRYUSERID", "EXCHANGEDATE", "ACCOUNT", "AVGEXECPRICE",
            "CURRENCY", "LASTMARKET", "ORDSTAT", "ORDERTYPE", "EXECUTIONTYPE", "ACCOUNTALIAS",
            "EXCHANGETYPE", "QUANTITY", "Broker", "MsgLine", "BID", "UnderlyingSymbol", "RIC"
        };

        internal static string[] POSITION_COLUMN_NAMES = {
            "Account", "BID", "Position", "Value", "BookType", "SharesBought", 
            "SharesSold", "UnderlyingSymbol", "RIC", "AveragePrice", "PandL"
        };

        internal const string MESSAGE_FILTER_COLUMN = "TYPE";
        internal const string POSITION_FILTER_COLUMN = "POSTYPE";

        internal const string EXECUTION_TYPE = "EXECUTION";
    }
}
