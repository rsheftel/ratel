package amazon;

import amazon.MetaBucket.*;

public interface S3Cacheable<T> {

    Key key(MetaBucket bucket);
    T response();

}
