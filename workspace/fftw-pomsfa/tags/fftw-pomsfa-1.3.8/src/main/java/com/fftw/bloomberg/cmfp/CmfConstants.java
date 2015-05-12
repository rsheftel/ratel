package com.fftw.bloomberg.cmfp;

public class CmfConstants
{

    private CmfConstants() {
        // Prevent extending
    }
    /**
     * Used for building empty TradeRecords
     */
    public static final String SPACES_50 = "                                                  ";
    
    public static final String SPACES_TWO = "  ";
    public static final String SPACES_FOUR = "    ";
//    public static final String FIVE_SPACES = "     ";
    public static final String SPACES_SIX = "      ";
    public static final String SPACES_EIGHT = "        ";

    // Message Type Constants
    
    public static final String TRADE = "01";
    public static final String TRADE_RECEIPT = "02";
    public static final String ACCEPT_REJECT = "03";
    public static final String HEARTBEAT = "04";
    public static final String HEARTBEAT_ACK = "05";
    
    // Trade Status Constants
    public static final int STATUS_NEW_TRADE = 1;
    public static final int STATUS_CANCEL_TRADE = 3;
    public static final int STATUS_CORRECT_TRADE = 5;
        
    public static final int SIDE_SELL = 1;
    public static final int SIDE_BUY = 2;
    public static final int SIDE_SELL_SHORT = 3;
    public static final int SIDE_BUY_COVER = 4;
    
    public static final int SC_ECN = 1600;
    
}
