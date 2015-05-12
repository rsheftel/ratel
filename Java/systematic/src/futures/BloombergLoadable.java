package futures;

import java.util.*;

public interface BloombergLoadable {

	List<BloombergJobEntry> jobEntries(Date asOf, BloombergField field);

	BloombergJob job(BloombergField field);

	List<BloombergField> fields();

}