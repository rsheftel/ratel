/**
 * JacobGen generated file --- do not edit
 *
 * (http://www.sourceforge.net/projects/jacob-project */
package malbec.jacob.rediplus;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class RediPlusSpreadPair extends Dispatch {

    public static final String componentName = "REDI.TFSpreadPair";

    // public static final String componentName = "RediLib.ITFSpreadPair";

    public RediPlusSpreadPair() {
        super(componentName);
    }

    /**
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it
     * must exist in every wrapper class whose instances may be returned from method calls wrapped in
     * VT_DISPATCH Variants.
     */
    public RediPlusSpreadPair(Dispatch d) {
        // take over the IDispatch pointer
        m_pDispatch = d.m_pDispatch;
        // null out the input's pointer
        d.m_pDispatch = 0;
    }

    public RediPlusSpreadPair(String compName) {
        super(compName);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getSymbol(int nLeg) {
        return Dispatch.call(this, "GetSymbol", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     * @return the result is of type boolean
     */
    public boolean setSymbol(int nLeg, Variant newValue) {
        return Dispatch.call(this, "SetSymbol", new Variant(nLeg), newValue).changeType(
                Variant.VariantBoolean).getBoolean();
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getAccount(int nLeg) {
        return Dispatch.call(this, "GetAccount", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     * @return the result is of type boolean
     */
    public boolean setAccount(int nLeg, Variant newValue) {
        return Dispatch.call(this, "SetAccount", new Variant(nLeg), newValue).changeType(
                Variant.VariantBoolean).getBoolean();
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getAccountNum(int nLeg) {
        return Dispatch.call(this, "GetAccountNum", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setAccountNum(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetAccountNum", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getCapacity(int nLeg) {
        return Dispatch.call(this, "GetCapacity", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     * @return the result is of type boolean
     */
    public boolean setCapacity(int nLeg, Variant newValue) {
        return Dispatch.call(this, "SetCapacity", new Variant(nLeg), newValue).changeType(
                Variant.VariantBoolean).getBoolean();
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getExchangeCost(int nLeg) {
        return Dispatch.call(this, "GetExchangeCost", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getInitiate(int nLeg) {
        return Dispatch.call(this, "GetInitiate", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setInitiate(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetInitiate", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getRoundLotsOnly(int nLeg) {
        return Dispatch.call(this, "GetRoundLotsOnly", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setRoundLotsOnly(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetRoundLotsOnly", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getManageOddLots(int nLeg) {
        return Dispatch.call(this, "GetManageOddLots", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setManageOddLots(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetManageOddLots", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getDestination(int nLeg) {
        return Dispatch.call(this, "GetDestination", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setDestination(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetDestination", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param nSide
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getSideAction(int nLeg, int nSide) {
        return Dispatch.call(this, "GetSideAction", new Variant(nLeg), new Variant(nSide));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param nSide
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setSideAction(int nLeg, int nSide, Variant newValue) {
        Dispatch.call(this, "SetSideAction", new Variant(nLeg), new Variant(nSide), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getLocBroker(int nLeg) {
        return Dispatch.call(this, "GetLocBroker", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setLocBroker(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetLocBroker", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getDividend(int nLeg) {
        return Dispatch.call(this, "GetDividend", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setDividend(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetDividend", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getVolumeToOpen(int nLeg) {
        return Dispatch.call(this, "GetVolumeToOpen", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setVolumeToOpen(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetVolumeToOpen", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getCancelReplaceIncrement(int nLeg) {
        return Dispatch.call(this, "GetCancelReplaceIncrement", new Variant(nLeg));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nLeg
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setCancelReplaceIncrement(int nLeg, Variant newValue) {
        Dispatch.call(this, "SetCancelReplaceIncrement", new Variant(nLeg), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getEnabled(int nSetupOrUnwind) {
        return Dispatch.call(this, "GetEnabled", new Variant(nSetupOrUnwind));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setEnabled(int nSetupOrUnwind, Variant newValue) {
        Dispatch.call(this, "SetEnabled", new Variant(nSetupOrUnwind), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getLimit(int nSetupOrUnwind) {
        return Dispatch.call(this, "GetLimit", new Variant(nSetupOrUnwind));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setLimit(int nSetupOrUnwind, Variant newValue) {
        Dispatch.call(this, "SetLimit", new Variant(nSetupOrUnwind), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getOutLimit(int nSetupOrUnwind) {
        return Dispatch.call(this, "GetOutLimit", new Variant(nSetupOrUnwind));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setOutLimit(int nSetupOrUnwind, Variant newValue) {
        Dispatch.call(this, "SetOutLimit", new Variant(nSetupOrUnwind), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getPositionObjective(int nSetupOrUnwind) {
        return Dispatch.call(this, "GetPositionObjective", new Variant(nSetupOrUnwind));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setPositionObjective(int nSetupOrUnwind, Variant newValue) {
        Dispatch.call(this, "SetPositionObjective", new Variant(nSetupOrUnwind), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @return the result is of type Variant
     */
    public Variant getOrderQuantity(int nSetupOrUnwind) {
        return Dispatch.call(this, "GetOrderQuantity", new Variant(nSetupOrUnwind));
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nSetupOrUnwind
     *            an input-parameter of type int
     * @param newValue
     *            an input-parameter of type Variant
     */
    public void setOrderQuantity(int nSetupOrUnwind, Variant newValue) {
        Dispatch.call(this, "SetOrderQuantity", new Variant(nSetupOrUnwind), newValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type boolean
     */
    public boolean isNewPair() {
        return Dispatch.call(this, "IsNewPair").changeType(Variant.VariantBoolean).getBoolean();
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type boolean
     */
    public boolean submit() {
        return Dispatch.call(this, "Submit").changeType(Variant.VariantBoolean).getBoolean();
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param lastError
     *            an input-parameter of type Variant
     */
    public void getLastError(Variant lastError) {
        Dispatch.call(this, "GetLastError", lastError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param nOnOff
     *            an input-parameter of type int
     * @param pairID
     *            an input-parameter of type Variant
     * @param acct
     *            an input-parameter of type Variant
     * @param userID
     *            an input-parameter of type Variant
     */
    public void setAutoTrade(int nOnOff, Variant pairID, Variant acct, Variant userID) {
        Dispatch.call(this, "SetAutoTrade", new Variant(nOnOff), pairID, acct, userID);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param pairID
     *            an input-parameter of type Variant
     * @param pairAcct
     *            an input-parameter of type Variant
     * @param uID
     *            an input-parameter of type Variant
     * @return the result is of type boolean
     */
    public boolean loadExistPair(Variant pairID, Variant pairAcct, Variant uID) {
        return Dispatch.call(this, "LoadExistPair", pairID, pairAcct, uID).changeType(Variant.VariantBoolean)
                .getBoolean();
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getPairID() {
        return Dispatch.get(this, "PairID");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setPairID(Variant parmValue) {
        Dispatch.put(this, "PairID", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getPairUserID() {
        return Dispatch.get(this, "PairUserID");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setPairUserID(Variant parmValue) {
        Dispatch.put(this, "PairUserID", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getPairUserName() {
        return Dispatch.get(this, "PairUserName");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setPairUserName(Variant parmValue) {
        Dispatch.put(this, "PairUserName", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getCurrencyConversionDirection() {
        return Dispatch.get(this, "CurrencyConversionDirection");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setCurrencyConversionDirection(Variant parmValue) {
        Dispatch.put(this, "CurrencyConversionDirection", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getCurrencyConversionRateType() {
        return Dispatch.get(this, "CurrencyConversionRateType");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setCurrencyConversionRateType(Variant parmValue) {
        Dispatch.put(this, "CurrencyConversionRateType", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getCCYRate() {
        return Dispatch.get(this, "CCYRate");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setCCYRate(Variant parmValue) {
        Dispatch.put(this, "CCYRate", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getSpreadRatio1() {
        return Dispatch.get(this, "SpreadRatio1");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setSpreadRatio1(Variant parmValue) {
        Dispatch.put(this, "SpreadRatio1", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getSpreadRatio2() {
        return Dispatch.get(this, "SpreadRatio2");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setSpreadRatio2(Variant parmValue) {
        Dispatch.put(this, "SpreadRatio2", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getCash() {
        return Dispatch.get(this, "Cash");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setCash(Variant parmValue) {
        Dispatch.put(this, "Cash", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getHedgeRatio() {
        return Dispatch.get(this, "HedgeRatio");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setHedgeRatio(Variant parmValue) {
        Dispatch.put(this, "HedgeRatio", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getHedgeType() {
        return Dispatch.get(this, "HedgeType");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setHedgeType(Variant parmValue) {
        Dispatch.put(this, "HedgeType", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getSpreadCalc() {
        return Dispatch.get(this, "SpreadCalc");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setSpreadCalc(Variant parmValue) {
        Dispatch.put(this, "SpreadCalc", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getAutoTradeMethod() {
        return Dispatch.get(this, "AutoTradeMethod");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setAutoTradeMethod(Variant parmValue) {
        Dispatch.put(this, "AutoTradeMethod", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getUserID() {
        return Dispatch.get(this, "UserID");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setUserID(Variant parmValue) {
        Dispatch.put(this, "UserID", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getPassword() {
        return Dispatch.get(this, "Password");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setPassword(Variant parmValue) {
        Dispatch.put(this, "Password", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getStrategy() {
        return Dispatch.get(this, "Strategy");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setStrategy(Variant parmValue) {
        Dispatch.put(this, "Strategy", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getStartTime() {
        return Dispatch.get(this, "StartTime");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setStartTime(Variant parmValue) {
        Dispatch.put(this, "StartTime", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getEndTime() {
        return Dispatch.get(this, "EndTime");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setEndTime(Variant parmValue) {
        Dispatch.put(this, "EndTime", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getInitiateRange() {
        return Dispatch.get(this, "InitiateRange");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setInitiateRange(Variant parmValue) {
        Dispatch.put(this, "InitiateRange", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getLiqConstraint() {
        return Dispatch.get(this, "LiqConstraint");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setLiqConstraint(Variant parmValue) {
        Dispatch.put(this, "LiqConstraint", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getRelPrxRng() {
        return Dispatch.get(this, "RelPrxRng");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setRelPrxRng(Variant parmValue) {
        Dispatch.put(this, "RelPrxRng", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getRelPrxRngTypePassive() {
        return Dispatch.get(this, "RelPrxRngTypePassive");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setRelPrxRngTypePassive(Variant parmValue) {
        Dispatch.put(this, "RelPrxRngTypePassive", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getMaxUnhedgedPct() {
        return Dispatch.get(this, "MaxUnhedgedPct");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setMaxUnhedgedPct(Variant parmValue) {
        Dispatch.put(this, "MaxUnhedgedPct", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getShortTickAllowance() {
        return Dispatch.get(this, "ShortTickAllowance");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setShortTickAllowance(Variant parmValue) {
        Dispatch.put(this, "ShortTickAllowance", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getOrderTimeInterval() {
        return Dispatch.get(this, "OrderTimeInterval");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setOrderTimeInterval(Variant parmValue) {
        Dispatch.put(this, "OrderTimeInterval", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getBuyActionType() {
        return Dispatch.get(this, "BuyActionType");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setBuyActionType(Variant parmValue) {
        Dispatch.put(this, "BuyActionType", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getBuyActionOffset() {
        return Dispatch.get(this, "BuyActionOffset");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setBuyActionOffset(Variant parmValue) {
        Dispatch.put(this, "BuyActionOffset", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getSellActionType() {
        return Dispatch.get(this, "SellActionType");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setSellActionType(Variant parmValue) {
        Dispatch.put(this, "SellActionType", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getSellActionOffset() {
        return Dispatch.get(this, "SellActionOffset");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setSellActionOffset(Variant parmValue) {
        Dispatch.put(this, "SellActionOffset", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getOBPriceIncrement() {
        return Dispatch.get(this, "OBPriceIncrement");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setOBPriceIncrement(Variant parmValue) {
        Dispatch.put(this, "OBPriceIncrement", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getOBQuantityFactor() {
        return Dispatch.get(this, "OBQuantityFactor");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setOBQuantityFactor(Variant parmValue) {
        Dispatch.put(this, "OBQuantityFactor", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getHKSSConfirmed() {
        return Dispatch.get(this, "HKSSConfirmed");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setHKSSConfirmed(Variant parmValue) {
        Dispatch.put(this, "HKSSConfirmed", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getDisableOpeningTest() {
        return Dispatch.get(this, "DisableOpeningTest");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setDisableOpeningTest(Variant parmValue) {
        Dispatch.put(this, "DisableOpeningTest", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant getMngBalShares() {
        return Dispatch.get(this, "MngBalShares");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void setMngBalShares(Variant parmValue) {
        Dispatch.put(this, "MngBalShares", parmValue);
    }

}
