package jet.isur.nsi.api.data;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.model.SortExp;

public class SortListBuilder {

    private final List<SortExp> sortList = new ArrayList<>();
    private final NsiConfigDict dict;

    SortListBuilder(NsiConfigDict dict) {
        this.dict = dict;
    }

    public SortListBuilder add(String key) {
        return add(key, true);
    }

    public SortListBuilder add(String key,boolean asc) {
        return add(getDictAttr(key),asc);
    }

    private NsiConfigAttr getDictAttr(String key) {
        NsiConfigAttr result = dict.getAttr(key);
        if(result == null) {
            throw new NsiServiceException("dict %s has not attr %s",dict.getName(),key);
        }
        return result;
    }

    public SortListBuilder add(NsiConfigAttr attr) {
        return add(attr, true);
    }

    public SortListBuilder add(NsiConfigAttr attr,boolean asc) {
        SortExp sortExp = new SortExp();
        sortExp.setKey(attr.getName());
        sortExp.setAsc(asc);
        sortList.add(sortExp);
        return this;
    }

    public List<SortExp> build() {
        return sortList;
    }
}
