package com.fftw.bloomberg.types;

/**
 * Represent the Bloomberg security types.
 * 
 */
public enum BBSecurityIDFlag {
    Unknown(0), Cusip(1), Sedol2(2), Cedel(3), Aibd(4), EuroClearNumber(5), Wpk(6), Rga(7), Isin(8),
    EuroClear(9), Valoren(10), Belgian(11), Dutch(12), Danish(13), Austrian(14), Luxembourg(15),
    MiscDomestic(16), Norway(17), Japan(18), Spain(19), Italy(20), Sweden(21), JapaneseCN(22),
    French(23), Cins(24), Sedol1(25), Singapore(26), BelgianLoan(27), UKEpic(28), HongKong(29),
    BBID(30), BBUID(31), MortgagePool(93), FX(94), SyntheticMortgage(95), MLID(96),
    MoneyMarket(97), Equity(98), FutureOption(99);


    private int idFlag;


    private BBSecurityIDFlag(int idFlag) {
        this.idFlag = idFlag;
    }

    public int getSecurityIDFlag() {
        return idFlag;
    }

    public static BBSecurityIDFlag valueOf(int code) {
        if (code < 32) {
            return values()[code];
        } else {
            switch (code) {
                case 69: // reserved
                    throw new IllegalArgumentException("69 is reserved!");
                case 93:
                    return MortgagePool;
                case 94:
                    return FX;
                case 95:
                    return SyntheticMortgage;
                case 96:
                    return MLID;
                case 97:
                    return MoneyMarket;
                case 98:
                    return Equity;
                case 99:
                    return FutureOption;
                default:
                    return Unknown;
            }
        }

    }
}
