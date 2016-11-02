package jet.nsi.api.data;

import java.util.Collection;

import jet.nsi.api.model.MetaDict;

public interface NsiConfig {
    NsiConfigDict getDict(String name);
    MetaDict getMetaDict(String name);
    
    Collection<NsiConfigDict> getDicts();
    Collection<NsiConfigDict> getDicts(Collection<String> labels);
    
    Collection<MetaDict> getMetaDicts();
    Collection<MetaDict> getMetaDicts(Collection<String> labels);
}
