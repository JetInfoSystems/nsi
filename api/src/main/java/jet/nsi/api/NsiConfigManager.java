package jet.nsi.api;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.model.MetaDict;

public interface NsiConfigManager {

    NsiConfig getConfig();
    void createOrUpdateConfig(MetaDict metaDict);
}
