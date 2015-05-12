
namespace TradingScreenApiService.Database.Mappings
{
    public class FuturesSymbolMapping
    {
        public virtual long Id { get; set; }
        public virtual string PlatformId { get; set; }
        public virtual string BloombergSymbolRoot { get; set; }
        public virtual string PlatformReceivingSymbolRoot { get; set; }
        public virtual string PlatformSendingSymbolRoot { get; set; }
        public virtual decimal PriceMultiplier { get; set; }
    }
}
