package malbec.jacob;

import com.jacob.com.Variant;

public class JacobUtil {

    private JacobUtil() {
        // prevent
    }
    
    /**
     * Create a <code>Variant</code> that can be used to pass a value back when using
     * a variant as a parameter.
     * 
     * @return
     */
    public static Variant createRefVariant() {
        Variant variant = new Variant();
        variant.putVariant("");
     
        return variant;
    }
}
