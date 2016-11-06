package jet.nsi.api;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.model.MetaDict;

public interface NsiConfigManager {

    NsiConfig getConfig();
    NsiConfig reloadConfig();
    void checkoutNewConfig(String from);
    void createOrUpdateConfig(MetaDict metaDict);
}
