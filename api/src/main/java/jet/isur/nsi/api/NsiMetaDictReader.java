package jet.isur.nsi.api;

import java.io.InputStream;

import jet.isur.nsi.api.model.MetaDict;

public interface NsiMetaDictReader {
    MetaDict read(InputStream src);
}
