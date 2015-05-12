package systemdb.metadata;

import java.util.*;

import systemdb.data.*;
import util.*;

public interface IntradaySource {

    List<Bar> bars(Range range, Interval interval);

}