package jet.nsi.api;

/**
 * Created by akatkevich on 20.10.2016.
 */
import jet.nsi.api.model.MetaDict;

import java.io.FileWriter;

public interface NsiMetaDictWriter {
    void write (MetaDict dict, FileWriter fileWriter);
}
