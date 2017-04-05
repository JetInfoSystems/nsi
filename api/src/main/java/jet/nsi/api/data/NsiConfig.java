package jet.nsi.api.data;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import jet.nsi.api.model.MetaDict;

public interface NsiConfig {
    NsiConfigDict getDict(String name);
    MetaDict getMetaDict(String name);

    void addDict(MetaDict metaDict);
    MetaDict addDictNew(MetaDict metaDict);
    void postCheck();

    Path getMetaDictPath(String name);
    
    Collection<NsiConfigDict> getDicts();
    Collection<NsiConfigDict> getDicts(Set<String> labels);
    
    Collection<MetaDict> getMetaDicts();
    Collection<MetaDict> getMetaDicts(Set<String> labels);

}
