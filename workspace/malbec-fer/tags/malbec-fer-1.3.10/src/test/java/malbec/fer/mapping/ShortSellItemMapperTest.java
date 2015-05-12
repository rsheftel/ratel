package malbec.fer.mapping;

import static org.testng.Assert.*;

import java.math.BigDecimal;

import malbec.AbstractBaseTest;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class ShortSellItemMapperTest extends AbstractBaseTest {

    @Test(groups = { "unittest" })
    public void testMapping() {
        ShortSellItemMapper ssim = new ShortSellItemMapper();

        int resultCount = ssim.initialize(new LocalDate(2009, 3, 19));

        assertTrue(resultCount > 0, "Failed to read mappings");

        BigDecimal shares = ssim.sharesToShort("MFPB", "JMP");

        assertNotNull(shares);
        assertEqualsBD(shares, 1027328);

        shares = ssim.sharesToShort("Test", "JMP");

        assertNotNull(shares);

        BigDecimal previousShares1 = ssim.add("TEST", "AAPL", new BigDecimal("1029384756"));
        BigDecimal previousShares2 = ssim.subtract("TEST", "AAPL", BigDecimal.valueOf(1000));
        assertEqualsBD(previousShares1, 0);
        assertEqualsBD(previousShares2, 1029384756);

        int count = ssim.reload(new LocalDate(2009, 3, 19));
        assertTrue(count > 0, "Failed to re-load");
    }
}
