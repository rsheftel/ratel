package malbec.jacob.rediplus;

import java.math.BigDecimal;

import malbec.fer.rediplus.RediExchange;
import malbec.jacob.AbstractBaseCom;

import com.jacob.com.ComException;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class RediPlusOrder extends AbstractBaseCom {

    public RediPlusOrder() {
        super();
    }
    
    /* 
     * (non-Javadoc)
     * 
     * @see redi.AbstractBaseCom#getProgramID()
     */
    @Override
    protected String getProgramID() {
        return "REDI.ORDER";
    }

    /**
     * 
     * @return Side
     */
    public String getSide() throws ComException {
        return getAsString("Side");
    }

    /**
     * 
     * @param Side
     */
    public void setSide(String newSide) throws ComException {
        put("Side", newSide);
    }

    /**
     * 
     * @return Symbol
     */
    public String getSymbol() throws ComException {
        return getAsString("Symbol");
    }

    /**
     * 
     * @param Symbol
     */
    public void setSymbol(String newSymbol) throws ComException {
        put("Symbol", newSymbol);

    }

    /**
     * 
     * @return Exchange
     */
    public RediExchange getExchange() throws ComException {
        return RediExchange.valueFor(getAsString("Exchange"));
    }

    /**
     * 
     * @param Exchange
     */
    public void setExchange(RediExchange newExchange) throws ComException {
        put("Exchange", newExchange.toString());
    }

    /**
     * 
     * @return Quantity
     */
    public BigDecimal getQuantity() throws ComException {
        return getAsBigDecimal("Quantity");
    }

    
    public void setQuantity(long newQuantity) throws ComException {
        put("Quantity", newQuantity);
    }

    /**
     * 
     * @param Quantity
     */
    public void setQuantity(BigDecimal newQuantity) throws ComException {
        put("Quantity", newQuantity);

    }

    /**
     * 
     * @return Price
     */
    public BigDecimal getPrice() throws ComException {
        return getAsBigDecimal("Price");
    }

    /**
     * 
     * @param Price
     */
    public void setPrice(BigDecimal newPrice) throws ComException {
        put("Price", newPrice.toPlainString());
    }

    /**
     * 
     * @return PriceType
     */
    public String getPriceType() throws ComException {
        return getAsString("PriceType");
    }

    /**
     * 
     * @param PriceType
     */
    public void setPriceType(String newPriceType) throws ComException {
        put("PriceType", newPriceType);
    }

    /**
     * 
     * @return StopPrice
     */
    public BigDecimal getStopPrice() throws ComException {
        return getAsBigDecimal("StopPrice");
    }

    /**
     * 
     * @param StopPrice
     */
    public void setStopPrice(BigDecimal newStopPrice) throws ComException {
        put("StopPrice", newStopPrice);
    }

    /**
     * 
     * @return TIF
     */
    public String getTIF() throws ComException {
        return getAsString("TIF");
    }

    /**
     * 
     * @param TIF
     */
    public void setTIF(String newTIF) throws ComException {
        put("TIF", newTIF);
    }

    /**
     * 
     * @return Account
     */
    public String getAccount() throws ComException {
        return getAsString("Account");
    }

    /**
     * 
     * @param Account
     */
    public void setAccount(String newAccount) throws ComException {
        put("Account", newAccount);
    }

    /**
     * 
     * @return Memo
     */
    public String getMemo() throws ComException {
        return getAsString("Memo");
    }

    /**
     * 
     * @param Memo
     */
    public void setMemo(String newMemo) throws ComException {
        put("Memo", newMemo);
    }

    /**
     * 
     * @return PrefMM
     */
    public Variant getPrefMM() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param PrefMM
     */
    public void setPrefMM(Variant newPrefMM) throws ComException {

        put("PrefMM", newPrefMM);

    }

    /**
     * 
     * @return timeout
     */
    public Variant gettimeout() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param timeout
     */
    public void settimeout(Variant newtimeout) throws ComException {

        put("timeout", newtimeout);

    }

    /**
     * 
     * @return Display
     */
    public Variant getDisplay() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param Display
     */
    public void setDisplay(Variant newDisplay) throws ComException {

        put("Display", newDisplay);

    }

    /**
     * 
     * @return Warning
     */
    public Boolean getWarning() throws ComException {
        return getAsBoolean("Warning");
    }

    /**
     * 
     * @param Warning
     */
    public void setWarning(Boolean newWarning) throws ComException {
        put("Warning", newWarning);
    }

    /**
     * 
     * @return UserID
     */
    public String getUserID() throws ComException {
        return getAsString("UserID");
    }

    /**
     * 
     * @param UserID
     */
    public void setUserID(String newUserID) throws ComException {
        put("UserID", newUserID);
    }

    /**
     * 
     * @return Password
     */
    public String getPassword() throws ComException {
        return getAsString("Password");
    }

    /**
     * 
     * @param Password
     */
    public void setPassword(String newPassword) throws ComException {
        put("Password", newPassword);
    }

    /**
     * 
     * @return DisplayQuantity
     */
    public BigDecimal getDisplayQuantity() throws ComException {
        return getAsBigDecimal("DisplayQuantity");
    }

    /**
     * 
     * @param DisplayQuantity
     */
    public void setDisplayQuantity(BigDecimal newDisplayQuantity) throws ComException {
        put("DisplayQuantity", newDisplayQuantity);
    }

    /**
     * 
     * @return Agency
     */
    public Variant getAgency() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param Agency
     */
    public void setAgency(Variant newAgency) throws ComException {

        put("Agency", newAgency);

    }

    /**
     * 
     * @return OrderDlgToken
     */
    public Variant getOrderDlgToken() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param OrderDlgToken
     */
    public void setOrderDlgToken(Variant newOrderDlgToken) throws ComException {

        put("OrderDlgToken", newOrderDlgToken);

    }

    /**
     * 
     * @return MontageToken
     */
    public Variant getMontageToken() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param MontageToken
     */
    public void setMontageToken(Variant newMontageToken) throws ComException {

        put("MontageToken", newMontageToken);

    }

    /**
     * 
     * @return Ticket
     */
    public Variant getTicket() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param Ticket
     */
    public void setTicket(Variant newTicket) throws ComException {

        put("Ticket", newTicket);

    }

    /**
     * 
     * @return ByPassTicket
     */
    public Variant getByPassTicket() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param ByPassTicket
     */
    public void setByPassTicket(Variant newByPassTicket) throws ComException {

        put("ByPassTicket", newByPassTicket);

    }

    /**
     * 
     * @return AllOrNone
     */
    public Variant getAllOrNone() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param AllOrNone
     */
    public void setAllOrNone(Variant newAllOrNone) throws ComException {

        put("AllOrNone", newAllOrNone);

    }

    /**
     * 
     * @return CrossSession
     */
    public Variant getCrossSession() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param CrossSession
     */
    public void setCrossSession(Variant newCrossSession) throws ComException {

        put("CrossSession", newCrossSession);

    }

    /**
     * 
     * @return Hidden
     */
    public Variant getHidden() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param Hidden
     */
    public void setHidden(Variant newHidden) throws ComException {

        put("Hidden", newHidden);

    }

    /**
     * 
     * @return SweepReserve
     */
    public Variant getSweepReserve() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param SweepReserve
     */
    public void setSweepReserve(Variant newSweepReserve) throws ComException {

        put("SweepReserve", newSweepReserve);

    }

    /**
     * 
     * @return DNRDNI
     */
    public Variant getDNRDNI() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param DNRDNI
     */
    public void setDNRDNI(Variant newDNRDNI) throws ComException {

        put("DNRDNI", newDNRDNI);

    }

    /**
     * 
     * @return ReferencePrice
     */
    public Variant getReferencePrice() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param ReferencePrice
     */
    public void setReferencePrice(Variant newReferencePrice) throws ComException {

        put("ReferencePrice", newReferencePrice);

    }

    /**
     * 
     * @return SI
     */
    public Variant getSI() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param SI
     */
    public void setSI(Variant newSI) throws ComException {

        put("SI", newSI);

    }

    /**
     * renamed since this was extending {@code Thread}
     * @return Priority
     */
    public Variant getPriority() throws ComException {
        return null; // Variant not implemented
    }

    /**
     * 
     * @param Priority
     */
    public void setPriority(Variant newPriority) throws ComException {
        put("Priority", newPriority);
    }

    /**
     * 
     * @return NonAttributable
     */
    public Variant getNonAttributable() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param NonAttributable
     */
    public void setNonAttributable(Variant newNonAttributable) throws ComException {

        put("NonAttributable", newNonAttributable);

    }

    /**
     * 
     * @return PNP
     */
    public Variant getPNP() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param PNP
     */
    public void setPNP(Variant newPNP) throws ComException {

        put("PNP", newPNP);

    }

    /**
     * 
     * @return LocateBroker
     */
    public Variant getLocateBroker() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param LocateBroker
     */
    public void setLocateBroker(Variant newLocateBroker) throws ComException {

        put("LocateBroker", newLocateBroker);

    }

    /**
     * 
     * @return Comm
     */
    public Variant getComm() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param Comm
     */
    public void setComm(Variant newComm) throws ComException {

        put("Comm", newComm);

    }

    /**
     * 
     * @return CommType
     */
    public Variant getCommType() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param CommType
     */
    public void setCommType(Variant newCommType) throws ComException {

        put("CommType", newCommType);

    }

    /**
     * 
     * @return MAQ
     */
    public Variant getMAQ() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param MAQ
     */
    public void setMAQ(Variant newMAQ) throws ComException {

        put("MAQ", newMAQ);

    }

    /**
     * 
     * @return PegPrice
     */
    public Variant getPegPrice() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param PegPrice
     */
    public void setPegPrice(Variant newPegPrice) throws ComException {

        put("PegPrice", newPegPrice);

    }

    /**
     * 
     * @return PegSign
     */
    public Variant getPegSign() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param PegSign
     */
    public void setPegSign(Variant newPegSign) throws ComException {

        put("PegSign", newPegSign);

    }

    /**
     * 
     * @return MarketMaker
     */
    public Variant getMarketMaker() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param MarketMaker
     */
    public void setMarketMaker(Variant newMarketMaker) throws ComException {

        put("MarketMaker", newMarketMaker);

    }

    /**
     * 
     * @return AutoMarketMaker
     */
    public Variant getAutoMarketMaker() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param AutoMarketMaker
     */
    public void setAutoMarketMaker(Variant newAutoMarketMaker) throws ComException {

        put("AutoMarketMaker", newAutoMarketMaker);

    }

    /**
     * 
     * @return TryToStop
     */
    public Variant getTryToStop() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param TryToStop
     */
    public void setTryToStop(Variant newTryToStop) throws ComException {

        put("TryToStop", newTryToStop);

    }

    /**
     * 
     * @return AuctionType
     */
    public Variant getAuctionType() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param AuctionType
     */
    public void setAuctionType(Variant newAuctionType) throws ComException {

        put("AuctionType", newAuctionType);

    }

    /**
     * 
     * @return MinDisp
     */
    public Variant getMinDisp() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param MinDisp
     */
    public void setMinDisp(Variant newMinDisp) throws ComException {

        put("MinDisp", newMinDisp);

    }

    /**
     * 
     * @return MaxDisp
     */
    public Variant getMaxDisp() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param MaxDisp
     */
    public void setMaxDisp(Variant newMaxDisp) throws ComException {

        put("MaxDisp", newMaxDisp);

    }

    /**
     * 
     * @return Discretion
     */
    public Variant getDiscretion() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param Discretion
     */
    public void setDiscretion(Variant newDiscretion) throws ComException {

        put("Discretion", newDiscretion);

    }

    /**
     * 
     * @return HardLimit
     */
    public Variant getHardLimit() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param HardLimit
     */
    public void setHardLimit(Variant newHardLimit) throws ComException {

        put("HardLimit", newHardLimit);

    }

    /**
     * 
     * @return MinDiscExecQty
     */
    public Variant getMinDiscExecQty() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param MinDiscExecQty
     */
    public void setMinDiscExecQty(Variant newMinDiscExecQty) throws ComException {

        put("MinDiscExecQty", newMinDiscExecQty);

    }

    /**
     * 
     * @return TicketTag
     */
    public Variant getTicketTag() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param TicketTag
     */
    public void setTicketTag(Variant newTicketTag) throws ComException {

        put("TicketTag", newTicketTag);

    }

    /**
     * 
     * @return StartTime
     */
    public Variant getStartTime() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param StartTime
     */
    public void setStartTime(Variant newStartTime) throws ComException {

        put("StartTime", newStartTime);

    }

    /**
     * 
     * @return EndTime
     */
    public Variant getEndTime() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param EndTime
     */
    public void setEndTime(Variant newEndTime) throws ComException {

        put("EndTime", newEndTime);

    }

    /**
     * 
     * @return VolLimitType
     */
    public Variant getVolLimitType() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param VolLimitType
     */
    public void setVolLimitType(Variant newVolLimitType) throws ComException {

        put("VolLimitType", newVolLimitType);

    }

    /**
     * 
     * @return VolLimit
     */
    public Variant getVolLimit() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param VolLimit
     */
    public void setVolLimit(Variant newVolLimit) throws ComException {

        put("VolLimit", newVolLimit);

    }

    /**
     * 
     * @return PriceLimitType
     */
    public Variant getPriceLimitType() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param PriceLimitType
     */
    public void setPriceLimitType(Variant newPriceLimitType) throws ComException {

        put("PriceLimitType", newPriceLimitType);

    }

    /**
     * 
     * @return BuyPriorityFactor
     */
    public Variant getBuyPriorityFactor() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param BuyPriorityFactor
     */
    public void setBuyPriorityFactor(Variant newBuyPriorityFactor) throws ComException {

        put("BuyPriorityFactor", newBuyPriorityFactor);

    }

    /**
     * 
     * @return SellPriorityFactor
     */
    public Variant getSellPriorityFactor() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param SellPriorityFactor
     */
    public void setSellPriorityFactor(Variant newSellPriorityFactor) throws ComException {

        put("SellPriorityFactor", newSellPriorityFactor);

    }

    /**
     * 
     * @return LegRatio
     */
    public Variant getLegRatio() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param LegRatio
     */
    public void setLegRatio(Variant newLegRatio) throws ComException {

        put("LegRatio", newLegRatio);

    }

    /**
     * 
     * @return MaxOutstandingMoney
     */
    public Variant getMaxOutstandingMoney() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param MaxOutstandingMoney
     */
    public void setMaxOutstandingMoney(Variant newMaxOutstandingMoney) throws ComException {

        put("MaxOutstandingMoney", newMaxOutstandingMoney);

    }

    /**
     * 
     * @return MaxOutstandingMoneyCoef
     */
    public Variant getMaxOutstandingMoneyCoef() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param MaxOutstandingMoneyCoef
     */
    public void setMaxOutstandingMoneyCoef(Variant newMaxOutstandingMoneyCoef) throws ComException {

        put("MaxOutstandingMoneyCoef", newMaxOutstandingMoneyCoef);

    }

    /**
     * 
     * @return TobPrice
     */
    public Variant getTobPrice() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param TobPrice
     */
    public void setTobPrice(Variant newTobPrice) throws ComException {

        put("TobPrice", newTobPrice);

    }

    /**
     * 
     * @return TobQuantity
     */
    public Variant getTobQuantity() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param TobQuantity
     */
    public void setTobQuantity(Variant newTobQuantity) throws ComException {

        put("TobQuantity", newTobQuantity);

    }

    /**
     * 
     * @return UnitDepthSize
     */
    public Variant getUnitDepthSize() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param UnitDepthSize
     */
    public void setUnitDepthSize(Variant newUnitDepthSize) throws ComException {

        put("UnitDepthSize", newUnitDepthSize);

    }

    /**
     * 
     * @return UnitDepthQuantity
     */
    public Variant getUnitDepthQuantity() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param UnitDepthQuantity
     */
    public void setUnitDepthQuantity(Variant newUnitDepthQuantity) throws ComException {

        put("UnitDepthQuantity", newUnitDepthQuantity);

    }

    /**
     * 
     * @return NumActiveDepths
     */
    public Variant getNumActiveDepths() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param NumActiveDepths
     */
    public void setNumActiveDepths(Variant newNumActiveDepths) throws ComException {

        put("NumActiveDepths", newNumActiveDepths);

    }

    /**
     * 
     * @return RiskPref
     */
    public Variant getRiskPref() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param RiskPref
     */
    public void setRiskPref(Variant newRiskPref) throws ComException {

        put("RiskPref", newRiskPref);

    }

    /**
     * 
     * @return ReferencePriceOffset
     */
    public Variant getReferencePriceOffset() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param ReferencePriceOffset
     */
    public void setReferencePriceOffset(Variant newReferencePriceOffset) throws ComException {

        put("ReferencePriceOffset", newReferencePriceOffset);

    }

    /**
     * 
     * @return PairID
     */
    public Variant getPairID() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param PairID
     */
    public void setPairID(Variant newPairID) throws ComException {

        put("PairID", newPairID);

    }

    /**
     * 
     * @return ClientData
     */
    public String getClientData() throws ComException {
        return getAsString("ClientData");
    }

    /**
     * 
     * @param ClientData
     */
    public void setClientData(String newClientData) throws ComException {
        put("ClientData", newClientData);
    }

    /**
     * 
     * @return BookType
     */
    public Variant getBookType() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param BookType
     */
    public void setBookType(Variant newBookType) throws ComException {

        put("BookType", newBookType);

    }

    /**
     * 
     * @return SSEReason
     */
    public Variant getSSEReason() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param SSEReason
     */
    public void setSSEReason(Variant newSSEReason) throws ComException {

        put("SSEReason", newSSEReason);

    }

    /**
     * 
     * @return Aggression
     */
    public Variant getAggression() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param Aggression
     */
    public void setAggression(Variant newAggression) throws ComException {

        put("Aggression", newAggression);

    }

    /**
     * 
     * @return CleanupPrice
     */
    public Variant getCleanupPrice() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param CleanupPrice
     */
    public void setCleanupPrice(Variant newCleanupPrice) throws ComException {

        put("CleanupPrice", newCleanupPrice);

    }

    /**
     * 
     * @return CompleteOrder
     */
    public Variant getCompleteOrder() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param CompleteOrder
     */
    public void setCompleteOrder(Variant newCompleteOrder) throws ComException {

        put("CompleteOrder", newCompleteOrder);

    }

    /**
     * 
     * @return ShortVerbiage
     */
    public Variant getShortVerbiage() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param ShortVerbiage
     */
    public void setShortVerbiage(Variant newShortVerbiage) throws ComException {

        put("ShortVerbiage", newShortVerbiage);

    }

    /**
     * 
     * @return CustomerIndicator
     */
    public Variant getCustomerIndicator() throws ComException {

        return null; // Variant not implemented

    }

    /**
     * 
     * @param CustomerIndicator
     */
    public void setCustomerIndicator(Variant newCustomerIndicator) throws ComException {

        put("CustomerIndicator", newCustomerIndicator);

    }

    /**
     * Submit the order.
     * 
     * @return boolean
     */
    public boolean submit(StringBuilder err) throws ComException {
        
        // We need to send a Variant with a Variant.  To do that we create a variant and 
        // then specify to put another variant within it.  However, we must pass in an
        // object (can NOT be Object - the JVM will crash).
        Variant vt = new Variant();
        vt.putVariant("");
        
        Dispatch sc = getDispatch(); 
        Variant rt = Dispatch.call(sc, "Submit", vt);
        
        // add the result to the SB.  Should be null/empty unless the return
        // type is false
        Object rtObject = vt.toJavaObject();
        err.append(rtObject.toString());
        
        return rt.changeType(Variant.VariantBoolean).getBoolean();
    }
    
    /**
     * 
     * 
     * @param x
     * @param y
     * @return void
     */
    public void displayOrderDlg(Variant x, Variant y) throws ComException {

        invokeN("DisplayOrderDlg", new Object[] { x, y });

    }

    /**
     * 
     * 
     * @param x
     * @param y
     * @return void
     */
    public void displayMontage(Variant x, Variant y) throws ComException {

        invokeN("DisplayMontage", new Object[] { x, y });

    }

    /**
     * 
     * 
     * @param OrderCacheRow
     * @return void
     */
    public void setOrder(Variant OrderCacheRow) throws ComException {

        invokeN("SetOrder", new Object[] { OrderCacheRow });

    }

    /**
     * 
     * 
     * @param UserID
     * @param OrderRefKey
     * @return void
     */
    public void setOrderKey(Variant UserID, Variant OrderRefKey) throws ComException {

        invokeN("SetOrderKey", new Object[] { UserID, OrderRefKey });

    }

    /**
     * 
     * 
     * @return boolean
     */
    public boolean submit2(Variant[] transID, Variant[] err) throws ComException {

        return ((Boolean) invokeN("Submit2", new Object[] { transID, err })).booleanValue();

    }

    /**
     * 
     * 
     * @param transID
     * @return void
     */
    public void getTransError(Variant transID, Variant[] err) throws ComException {

        invokeN("GetTransError", new Object[] { transID, err });

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object getExchangeCount(Variant[] pnCount) throws ComException {

        return invokeN("GetExchangeCount", new Object[] { pnCount });

    }

    /**
     * 
     * 
     * @param nExchangeNum
     * @return Variant
     */
    public Object getExchangeX(Variant nExchangeNum, Variant[] Exchange) throws ComException {

        return invokeN("GetExchangeX", new Object[] { nExchangeNum, Exchange });

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object getPriceTypeCount(Variant[] pnCount) throws ComException {

        return invokeN("GetPriceTypeCount", new Object[] { pnCount });

    }

    /**
     * 
     * 
     * @param nPriceTypeNum
     * @return Variant
     */
    public Object getPriceTypeX(Variant nPriceTypeNum, Variant[] PriceType) throws ComException {

        return invokeN("GetPriceTypeX", new Object[] { nPriceTypeNum, PriceType });

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object getTIFCount(Variant[] pnCount) throws ComException {

        return invokeN("GetTIFCount", new Object[] { pnCount });

    }

    /**
     * 
     * 
     * @param nTIFNum
     * @return Variant
     */
    public Object getTIFX(Variant nTIFNum, Variant[] TIF) throws ComException {

        return invokeN("GetTIFX", new Object[] { nTIFNum, TIF });

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object getAccountCount(Variant[] pnCount) throws ComException {

        return invokeN("GetAccountCount", new Object[] { pnCount });

    }

    /**
     * 
     * 
     * @param nAccountNum
     * @return Variant
     */
    public Object getAccountX(Variant nAccountNum, Variant[] Account) throws ComException {

        return invokeN("GetAccountX", new Object[] { nAccountNum, Account });

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isVWAPEnabled() throws ComException {

        return invokeN("IsVWAPEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isAccountEnabled() throws ComException {

        return invokeN("IsAccountEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isAllOrNoneEnabled() throws ComException {

        return invokeN("IsAllOrNoneEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isCrossSessionEnabled() throws ComException {

        return invokeN("IsCrossSessionEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isPNPEnabled() throws ComException {

        return invokeN("IsPNPEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isLocateBrokerEnabled() throws ComException {

        return invokeN("IsLocateBrokerEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isHiddenEnabled() throws ComException {

        return invokeN("IsHiddenEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isSweepReserveEnabled() throws ComException {

        return invokeN("IsSweepReserveEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isDNRDNIEnabled() throws ComException {

        return invokeN("IsDNRDNIEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isDisplayEnabled() throws ComException {

        return invokeN("IsDisplayEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isExchangeEnabled() throws ComException {

        return invokeN("IsExchangeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isMAQEnabled() throws ComException {

        return invokeN("IsMAQEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isCommEnabled() throws ComException {

        return invokeN("IsCommEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isCommTypeEnabled() throws ComException {

        return invokeN("IsCommTypeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isMemoEnabled() throws ComException {

        return invokeN("IsMemoEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isSIEnabled() throws ComException {

        return invokeN("IsSIEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isHeldNotHeldEnabled() throws ComException {

        return invokeN("IsHeldNotHeldEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isPriorityEnabled() throws ComException {

        return invokeN("IsPriorityEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isNonAttributableEnabled() throws ComException {

        return invokeN("IsNonAttributableEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isQuantityEnabled() throws ComException {

        return invokeN("IsQuantityEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isAgencyEnabled() throws ComException {

        return invokeN("IsAgencyEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isPriceEnabled() throws ComException {

        return invokeN("IsPriceEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isPegPriceEnabled() throws ComException {

        return invokeN("IsPegPriceEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isPriceTypeEnabled() throws ComException {

        return invokeN("IsPriceTypeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isMarketMakerEnabled() throws ComException {

        return invokeN("IsMarketMakerEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isSideEnabled() throws ComException {

        return invokeN("IsSideEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isStopPriceEnabled() throws ComException {

        return invokeN("IsStopPriceEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isSymbolEnabled() throws ComException {

        return invokeN("IsSymbolEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isTIFEnabled() throws ComException {

        return invokeN("IsTIFEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isDisplayQuantityEnabled() throws ComException {

        return invokeN("IsDisplayQuantityEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isTryToStopEnabled() throws ComException {

        return invokeN("IsTryToStopEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isTicketEnabled() throws ComException {

        return invokeN("IsTicketEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isRandomDisplayQuantityEnabled() throws ComException {

        return invokeN("IsRandomDisplayQuantityEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isDiscretionEnabled() throws ComException {

        return invokeN("IsDiscretionEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isDiscretionLimitEnabled() throws ComException {

        return invokeN("IsDiscretionLimitEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isHardLimitEnabled() throws ComException {

        return invokeN("IsHardLimitEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isStartTimeEnabled() throws ComException {

        return invokeN("IsStartTimeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isEndTimeEnabled() throws ComException {

        return invokeN("IsEndTimeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isVolLimitTypeEnabled() throws ComException {

        return invokeN("IsVolLimitTypeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isVolLimitEnabled() throws ComException {

        return invokeN("IsVolLimitEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isPriceLimitTypeEnabled() throws ComException {

        return invokeN("IsPriceLimitTypeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isBuyPriorityFactorEnabled() throws ComException {

        return invokeN("IsBuyPriorityFactorEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isSellPriorityFactorEnabled() throws ComException {

        return invokeN("IsSellPriorityFactorEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isLegRatioEnabled() throws ComException {

        return invokeN("IsLegRatioEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isMaxOutstandingMoneyEnabled() throws ComException {

        return invokeN("IsMaxOutstandingMoneyEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isMaxOutstandingMoneyCoefEnabled() throws ComException {

        return invokeN("IsMaxOutstandingMoneyCoefEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isTobPriceEnabled() throws ComException {

        return invokeN("IsTobPriceEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isTobQuantityEnabled() throws ComException {

        return invokeN("IsTobQuantityEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isUnitDepthSizeEnabled() throws ComException {

        return invokeN("IsUnitDepthSizeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isUnitDepthQuantityEnabled() throws ComException {

        return invokeN("IsUnitDepthQuantityEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isNumActiveDepthsEnabled() throws ComException {

        return invokeN("IsNumActiveDepthsEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isRiskPrefEnabled() throws ComException {

        return invokeN("IsRiskPrefEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isReferencePriceEnabled() throws ComException {

        return invokeN("IsReferencePriceEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isReferencePriceOffsetEnabled() throws ComException {

        return invokeN("IsReferencePriceOffsetEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object isAuctionTypeEnabled() throws ComException {

        return invokeN("IsAuctionTypeEnabled", new Object[] {});

    }

    /**
     * 
     * 
     * @return boolean
     */
    public boolean submit3(Variant[] err) throws ComException {

        return ((Boolean) invokeN("Submit3", new Object[] { err })).booleanValue();

    }

    /**
     * 
     * 
     * @param vToken
     * @return Variant
     */
    public Object reloadMontage(Variant vToken) throws ComException {

        return invokeN("ReloadMontage", new Object[] { vToken });

    }

    /**
     * 
     * 
     * @param vTicket
     * @return Variant
     */
    public Object linkTicket(Variant vTicket) throws ComException {

        return invokeN("LinkTicket", new Object[] { vTicket });

    }

    /**
     * 
     * 
     * @param vTFList
     * @return void
     */
    public void setTFList(Variant vTFList) throws ComException {

        invokeN("SetTFList", new Object[] { vTFList });

    }

    /**
     * 
     * 
     * @param vTFUser
     * @return void
     */
    public void setTFUser(Variant vTFUser) throws ComException {

        invokeN("SetTFUser", new Object[] { vTFUser });

    }

    /**
     * 
     * 
     * @param vAllocAccount
     * @return void
     */
    public void setAllocAccount(Variant vAllocAccount) throws ComException {

        invokeN("SetAllocAccount", new Object[] { vAllocAccount });

    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("ProgID=").append(getProgramID());
        sb.append(", Symbol=").append(getSymbol());
        sb.append(", Side=").append(getSide());
        sb.append(", PriceType=").append(getPriceType());
        sb.append(", TIF=").append(getTIF());
        sb.append(", Exchange=").append(getExchange());
        
        return sb.toString();
    }
}
