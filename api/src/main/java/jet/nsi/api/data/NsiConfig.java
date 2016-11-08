package jet.nsi.api.data;

import java.util.Collection;
import java.util.Set;

import jet.nsi.api.model.MetaDict;

public interface NsiConfig {
    NsiConfigDict getDict(String name);
    MetaDict getMetaDict(String name);
    
    Collection<NsiConfigDict> getDicts();
    Collection<NsiConfigDict> getDicts(Set<String> labels);
    
    Collection<MetaDict> getMetaDicts();
    Collection<MetaDict> getMetaDicts(Set<String> labels);
}
