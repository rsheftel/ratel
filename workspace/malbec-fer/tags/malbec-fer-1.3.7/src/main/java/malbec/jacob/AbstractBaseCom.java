package malbec.jacob;

import java.math.BigDecimal;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;

public abstract class AbstractBaseCom {

    private Dispatch dispatch = null;

    abstract protected String getProgramID();

    protected AbstractBaseCom() {
        super();
        dispatch = new Dispatch(getProgramID());
    }

    protected Dispatch getDispatch() {
        return dispatch;
    }

    // TODO Remove this method
    protected Object invokeN(String s, Object[] oa) {
        throw new UnsupportedOperationException("Implement me!");
    }

    // TODO Remove this method
    protected void put(String string, Variant newExchange) {
        throw new UnsupportedOperationException("Implement me!");
    }

    protected void put(String methodName, Boolean boolValue) {
        Dispatch sc = getDispatch();
        Dispatch.put(sc, methodName, new Variant(boolValue));
    }

    protected void put(String methodName, String strValue) {
        Dispatch sc = getDispatch();
        Dispatch.put(sc, methodName, new Variant(strValue));
    }
    
    protected void putByRef(String methodName, String strValue) {
        Dispatch sc = getDispatch();
        Variant variant = new Variant();
                
        variant.putVariant(strValue);
        
        Dispatch.put(sc, methodName, variant);
    }
    
    protected void put(String methodName, String[] strArray) {
        Dispatch sc = getDispatch();
        
        SafeArray safeArray = new SafeArray(Variant.VariantObject, strArray.length);
//        safeArray.fromStringArray(strArray);
        
//        for (int i = 0; i < strArray.length; i++) {
//            safeArray.setString(i, strArray[0]);
//        }
        
        Dispatch.put(sc, methodName, safeArray);
        
//        List<String> strList = Arrays.asList(strArray);
//        Dispatch.put(sc, methodName, strList);
    }

    protected void put(String methodName, BigDecimal bdValue) {
        Dispatch sc = getDispatch();
        Dispatch.put(sc, methodName, new Variant(bdValue));
    }

    protected void put(String methodName, long bdValue) {
        Dispatch sc = getDispatch();
        Dispatch.put(sc, methodName, new Variant(bdValue));
    }

    protected String getAsString(String methodName) {
        Dispatch sc = getDispatch();
        Variant rv = Dispatch.get(sc, methodName);

        return rv.isNull() ? null : rv.getString();
    }

    protected Boolean getAsBoolean(String methodName) {
        Dispatch sc = getDispatch();
        Variant rv = Dispatch.get(sc, methodName);

        return rv.isNull() ? null : rv.getBoolean();
    }

    protected BigDecimal getAsBigDecimal(String methodName) {
        Dispatch sc = getDispatch();
        Variant rv = Dispatch.get(sc, methodName);

        return rv.isNull() ? null : new BigDecimal(rv.getString());
    }
}
