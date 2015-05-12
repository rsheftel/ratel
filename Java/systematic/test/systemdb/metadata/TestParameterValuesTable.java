package systemdb.metadata;

import java.util.*;

import db.*;
import static systemdb.metadata.ParameterValuesTable.*;
import static util.Dates.*;

public class TestParameterValuesTable extends DbTestCase {

    public void testRecentParams() throws Exception {
        VALUES.insert("FaderClose", "Energy19796", "RiskDollars", "-10", now());
        Pv energy = new Pv("Energy19796");
        Map<String, String> params = VALUES.params("FaderClose", energy);
        assertEquals("-10", params.get("RiskDollars"));
        assertEquals("-10", VALUES.param("FaderClose", energy, "RiskDollars"));
        assertEquals("-10", VALUES.param("FaderClose", "Energy19796", "RiskDollars"));
    }
    
        
}
