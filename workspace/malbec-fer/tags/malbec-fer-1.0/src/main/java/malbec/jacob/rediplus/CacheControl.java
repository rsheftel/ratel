package malbec.jacob.rediplus;

import malbec.jacob.AbstractBaseCom;

import com.jacob.com.ComException;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class CacheControl extends AbstractBaseCom {

    public CacheControl() {
        super();
    }
    
    @Override
    protected String getProgramID() {
        return "REDI.QUERY";
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
     * 
     * @param table_name
     * @param where_clause
     * @return Variant
     */
    public Object submit(String tableName, String whereClause, StringBuilder ErrorCode) throws ComException {

        Variant err = new Variant();
        err.putVariant("");

        Dispatch sc = getDispatch();
        Variant rt = Dispatch.call(sc, "Submit", new Variant(tableName), new Variant(whereClause), err);

        return rt;
    }

    /**
     * 
     * 
     * @return Variant
     */
    public Object revoke(Variant[] ErrorCode) throws ComException {

        return invokeN("Revoke", new Object[] { ErrorCode });

    }

    /**
     * 
     * 
     * @param row
     * @param column
     * @return Variant
     */
    public Object getCell(Variant row, Variant column, Variant[] CellValue, Variant[] ErrorCode) throws ComException {

        return invokeN("GetCell", new Object[] { row, column, CellValue, ErrorCode });

    }

    /**
     * 
     * 
     * @param vaRow
     * @return Variant
     */
    public Object cancel(Variant vaRow, Variant[] vaError) throws ComException {

        return invokeN("Cancel", new Object[] { vaRow, vaError });

    }

    /**
     * 
     * 
     * @param vaRow
     * @return Variant
     */
    public Object getStatus(Variant vaRow, Variant[] vaStatus, Variant[] vaError) throws ComException {

        return invokeN("GetStatus", new Object[] { vaRow, vaStatus, vaError });

    }

    /**
     * 
     * 
     * @param vaRow
     * @return Variant
     */
    public Object confirmOut(Variant vaRow, Variant[] vaError) throws ComException {

        return invokeN("ConfirmOut", new Object[] { vaRow, vaError });

    }

    /**
     * 
     * 
     * @param vaRow
     * @return Variant
     */
    public Object confirmReceived(Variant vaRow, Variant[] vaError) throws ComException {

        return invokeN("ConfirmReceived", new Object[] { vaRow, vaError });

    }

    /**
     * 
     * 
     * @param vaType
     * @param vaArg1
     * @param vaArg2
     * @return Variant
     */
    public Object addWatch(Variant vaType, Variant vaArg1, Variant vaArg2, Variant[] vaError) throws ComException {

        return invokeN("AddWatch", new Object[] { vaType, vaArg1, vaArg2, vaError });

    }

    /**
     * 
     * 
     * @param vaType
     * @param vaArg1
     * @param vaArg2
     * @return Variant
     */
    public Object deleteWatch(Variant vaType, Variant vaArg1, Variant vaArg2, Variant[] vaError) throws ComException {

        return invokeN("DeleteWatch", new Object[] { vaType, vaArg1, vaArg2, vaError });

    }

    /**
     * 
     * 
     * @param row
     * @return Variant
     */
    public Object getCacheRowNumber(Variant row, Variant[] CacheRowValue, Variant[] vaError) throws ComException {

        return invokeN("GetCacheRowNumber", new Object[] { row, CacheRowValue, vaError });

    }

    /**
     * 
     * 
     * @param vaBrSeq
     * @return Variant
     */
    public Object cancelByBranchSequence(Variant vaBrSeq, Variant[] vaError) throws ComException {

        return invokeN("CancelByBranchSequence", new Object[] { vaBrSeq, vaError });

    }

    /**
     * 
     * 
     * @param vaUser
     * @param vaKey
     * @return Variant
     */
    public Object cancelByKey(Variant vaUser, Variant vaKey, Variant[] vaError) throws ComException {

        return invokeN("CancelByKey", new Object[] { vaUser, vaKey, vaError });

    }

    /**
     * 
     * 
     * @param vaUser
     * @param vaRefNum
     * @return Variant
     */
    public Object cancelByRefNum(Variant vaUser, Variant vaRefNum, Variant[] vaError) throws ComException {

        return invokeN("CancelByRefNum", new Object[] { vaUser, vaRefNum, vaError });

    }

}
