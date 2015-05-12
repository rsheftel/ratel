/**
 * JacobGen generated file --- do not edit
 * **** THIS FILE HAS BEEN MODIFIED ****
 * (http://www.sourceforge.net/projects/jacob-project */
package malbec.jacob.rediplus;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class RediPlusCacheControl extends Dispatch {

    // public static final String componentName = "RediLib.ICacheControl";
    public static final String componentName = "REDI.QUERY";

    public RediPlusCacheControl() {
        super(componentName);
    }

    /**
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it
     * must exist in every wrapper class whose instances may be returned from method calls wrapped in
     * VT_DISPATCH Variants.
     */
    public RediPlusCacheControl(Dispatch d) {
        // take over the IDispatch pointer
        m_pDispatch = d.m_pDispatch;
        // null out the input's pointer
        d.m_pDispatch = 0;
    }

    public RediPlusCacheControl(String compName) {
        super(compName);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param table_name
     *            an input-parameter of type Variant
     * @param where_clause
     *            an input-parameter of type Variant
     * @param errorCode
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant submit(Variant table_name, Variant where_clause, Variant errorCode) {
        return Dispatch.call(this, "Submit", table_name, where_clause, errorCode);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param errorCode
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant revoke(Variant errorCode) {
        return Dispatch.call(this, "Revoke", errorCode);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param row
     *            an input-parameter of type Variant
     * @param column
     *            an input-parameter of type Variant
     * @param cellValue
     *            an input-parameter of type Variant
     * @param errorCode
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant getCell(Variant row, Variant column, Variant cellValue, Variant errorCode) {
        return Dispatch.call(this, "GetCell", row, column, cellValue, errorCode);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaRow
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant cancel(Variant vaRow, Variant vaError) {
        return Dispatch.call(this, "Cancel", vaRow, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaRow
     *            an input-parameter of type Variant
     * @param vaStatus
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant getStatus(Variant vaRow, Variant vaStatus, Variant vaError) {
        return Dispatch.call(this, "GetStatus", vaRow, vaStatus, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaRow
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant confirmOut(Variant vaRow, Variant vaError) {
        return Dispatch.call(this, "ConfirmOut", vaRow, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaRow
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant confirmReceived(Variant vaRow, Variant vaError) {
        return Dispatch.call(this, "ConfirmReceived", vaRow, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaType
     *            an input-parameter of type Variant
     * @param vaArg1
     *            an input-parameter of type Variant
     * @param vaArg2
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant addWatch(Variant vaType, Variant vaArg1, Variant vaArg2, Variant vaError) {
        return Dispatch.call(this, "AddWatch", vaType, vaArg1, vaArg2, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaType
     *            an input-parameter of type Variant
     * @param vaArg1
     *            an input-parameter of type Variant
     * @param vaArg2
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant deleteWatch(Variant vaType, Variant vaArg1, Variant vaArg2, Variant vaError) {
        return Dispatch.call(this, "DeleteWatch", vaType, vaArg1, vaArg2, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param row
     *            an input-parameter of type Variant
     * @param cacheRowValue
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant getCacheRowNumber(Variant row, Variant cacheRowValue, Variant vaError) {
        return Dispatch.call(this, "GetCacheRowNumber", row, cacheRowValue, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaBrSeq
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant cancelByBranchSequence(Variant vaBrSeq, Variant vaError) {
        return Dispatch.call(this, "CancelByBranchSequence", vaBrSeq, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaUser
     *            an input-parameter of type Variant
     * @param vaKey
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant cancelByKey(Variant vaUser, Variant vaKey, Variant vaError) {
        return Dispatch.call(this, "CancelByKey", vaUser, vaKey, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param vaUser
     *            an input-parameter of type Variant
     * @param vaRefNum
     *            an input-parameter of type Variant
     * @param vaError
     *            an input-parameter of type Variant
     * @return the result is of type Variant
     */
    public Variant cancelByRefNum(Variant vaUser, Variant vaRefNum, Variant vaError) {
        return Dispatch.call(this, "CancelByRefNum", vaUser, vaRefNum, vaError);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type Variant
     */
    public Variant gettimeout() {
        return Dispatch.get(this, "timeout");
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type Variant
     */
    public void settimeout(Variant parmValue) {
        Dispatch.put(this, "timeout", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type String
     */
    public String getUserID() {
        return Dispatch.get(this, "UserID").toString();
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type String
     */
    public void setUserID(String parmValue) {
        Dispatch.put(this, "UserID", parmValue);
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @return the result is of type String
     */
    public String getPassword() {
        return Dispatch.get(this, "Password").toString();
    }

    /**
     * Wrapper for calling the ActiveX-Method with input-parameter(s).
     * 
     * @param parmValue
     *            an input-parameter of type String
     */
    public void setPassword(String parmValue) {
        Dispatch.put(this, "Password", parmValue);
    }

}
