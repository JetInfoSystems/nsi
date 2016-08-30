package jet.nsi.migrator.platform;

import java.io.OutputStream;

import jet.nsi.api.data.NsiConfigDict;

public interface DictToHbmSerializer {
    void marshalTo(NsiConfigDict dict, OutputStream os);
}
