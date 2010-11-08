package org.ratel.schedule.dependency;

import static org.ratel.util.Objects.*;
import static org.ratel.util.Range.*;
import static org.ratel.util.Strings.*;

import java.util.*;

import org.ratel.schedule.JobTable.*;
import org.ratel.systemdb.data.*;

public class AllSymbolsReady extends Dependency {
    List<Symbol> symbols = empty();

    public AllSymbolsReady(Integer id, Map<String, String> parameters) {
        super(id);
        List<String> symbolNames = split(",", parameters.get("symbols"));
        for(String name : symbolNames) symbols.add(new Symbol(name));
    }
    
    public static void create(List<String> symbolNames, Job item) {
        item.insertDependency(AllSymbolsReady.class, map(
            "symbols", join(",", symbolNames) 
        ));
    }

    @Override public String explain(Date asOf) {
        StringBuilder buf = new StringBuilder();
        buf.append("AllSymbolsReady:\n");
        for(Symbol symbol : symbols)
            if(symbol.observations(onDayOf(asOf)).isEmpty()) 
                buf.append(symbol.name() + " missing\n");
        return buf.toString();
    }

    @Override public boolean isIncomplete(Date asOf) {
        for(Symbol symbol : symbols) 
            if(symbol.observations(onDayOf(asOf)).isEmpty()) 
                return true;
        return false;
    }
    
}