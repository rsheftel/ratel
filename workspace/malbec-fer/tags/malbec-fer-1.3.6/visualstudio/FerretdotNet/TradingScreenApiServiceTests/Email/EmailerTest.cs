using NUnit.Framework;
using TradingScreenApiService.Email;

namespace TradingScreenApiServiceTests.Email
{
    [TestFixture]
    public class EmailerTest
    {
        [Test]
        public void TestSending() {
            Emailer.ToAddress = "Michael Franz<mfranz@fftw.com>";

            Emailer.Send("Test email", "The test body");
        }
    }
}
