package tsdb;

import static util.Errors.*;
import static util.Objects.*;

import java.util.*;

import util.*;

public class ObservationsMap<T> implements Iterable<T>, Sizable {
  
    Map<T, Observations> observations = emptyMap();

    public ObservationsMap() {}

    public boolean has(T s) {
        return observations.containsKey(s);
    }

    public void add(T s) {
        add(s, new Observations());
    }

    public Observations get(T s) {
        return bombNull(observations.get(s), s + "not in observations map");
    }

    public boolean isEmpty() {
        return observations.isEmpty();
    }

    public Observations only() {
        return the(observations.values());
    }

    public int size() {
        return observations.size();
    }

    @Override public Iterator<T> iterator() {
        return observations.keySet().iterator();
    }

    public void add(T ss, Observations o) {
        bombIf(has(ss), "already have " + ss + " in map");
        observations.put(ss, o);
    }

    public Collection<Observations> observationses() {
        return observations.values();
    }
    
    public void add(ObservationsMap<T> obs) {
        for (T ss : obs)
            add(ss, obs.get(ss));
    }

    public int totalRows() {
        int sum = 0;
        for (T ss : this)
            sum += get(ss).size();
        return sum;
    }
    public Double valueMaybe(T ss, Date date) {
        return has(ss) ? get(ss).valueOrNull(date) : null; 
    }

    public double value(T ss, Date date) {
        return get(ss).value(date);
    }

    public long longValue(T ss, Date date) {
        return (long) value(ss, date);
    }

    public Long longValueMaybe(T ss, Date date) {
        Double result = valueMaybe(ss, date);
        return result == null ? null : result.longValue();
    }

}
