using O=Q.Util.Objects;
using System.IO;
using amazon;
using file;
using NUnit.Framework;
using Q.Util;

namespace Q.Trading.Results {
    [TestFixture] 
    public class TestCurveFiles : DbTestCase {
        const string PATH = @".\foo";

        public override void setUp() {
            base.setUp();
            new QFile(PATH).deleteIfExists();
        }

        [Test]
        public void testCanWriteOneAndGetCorrectRowCountBack() {
            CurveFiles.writeOne(PATH, 7);
            AreEqual(3 * 7 * 8, new QFile(PATH).size());
        }

        [Test]
        public void testCanWriteBinaryFiles() {
            var dates = O.list(
                date("2005/11/10"),
                date("2005/11/14"),
                date("2005/11/15")
                );
            var pnls = O.list(1.0, 0.0, -1.0);
            var positions = O.list(0.0, 2.0, 3.0);
            CurveFiles.writeOne(PATH, dates, pnls, positions);
            AreEqual(File.ReadAllBytes(@"..\..\..\..\R\src\STO\inst\testdata\SimpleCurves\ABC_1_daily_mkt1\run_1.bin"), File.ReadAllBytes(PATH));
        }

        [Test]
        public void testCanWriteCurveFileToS3() {
            var dates = O.list(
                date("2005/11/10"),
                date("2005/11/14"),
                date("2005/11/15")
                );
            var pnls = O.list(1.0, 0.0, -1.0);
            var positions = O.list(0.0, 2.0, 3.0);

            var id = 5203;
            new MetaBucket("quantys-5203").create();
            var marketName = "RE.TEST.TY.1C";
            var runNumber = 1;
            CurveFiles.writeToS3(id, marketName, runNumber, dates, pnls, positions);
            CurveFiles.readFromS3(id, marketName, runNumber, PATH);
            AreEqual(File.ReadAllBytes(@"..\..\..\..\R\src\STO\inst\testdata\SimpleCurves\ABC_1_daily_mkt1\run_1.bin"), File.ReadAllBytes(PATH));
        }




    }
}