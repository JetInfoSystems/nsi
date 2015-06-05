package jet.isur.nsi.api.data;

import java.util.Collection;

public interface NsiConfig {
    NsiConfigDict getDict(String name);
    Collection<NsiConfigDict> getDicts();
}
