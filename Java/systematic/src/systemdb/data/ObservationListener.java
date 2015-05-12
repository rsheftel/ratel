package systemdb.data;

import java.util.*;

public interface ObservationListener {
    void onUpdate(Date date, double value);
}