package jet.isur.nsi.migrator.platform;

import java.io.OutputStream;

import jet.isur.nsi.api.data.NsiConfigDict;

public interface DictToHbmSerializer {
    void marshalTo(NsiConfigDict dict, OutputStream os);
}
