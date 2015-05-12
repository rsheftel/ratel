package systemdb.metadata;

import systemdb.data.*;

public interface LiveDataSource {

    void subscribe(ObservationListener listener);
    void subscribe(TickListener listener);

}
