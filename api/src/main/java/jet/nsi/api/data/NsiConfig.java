package jet.nsi.api.data;

import java.util.Collection;

import jet.nsi.api.model.MetaDict;

public interface NsiConfig {
    NsiConfigDict getDict(String name);
    MetaDict getMetaDict(String name);
    Collection<NsiConfigDict> getDicts();
    Collection<MetaDict> getMetaDicts();
}
