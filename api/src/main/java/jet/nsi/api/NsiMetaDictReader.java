package jet.nsi.api;

import java.io.InputStream;

import jet.nsi.api.model.MetaDict;

public interface NsiMetaDictReader {
    MetaDict read(InputStream src);
}
