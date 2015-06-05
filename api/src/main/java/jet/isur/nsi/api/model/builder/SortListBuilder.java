package jet.isur.nsi.api.model.builder;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.model.SortExp;

public class SortListBuilder {

    private final List<SortExp> sortList = new ArrayList<>();

    public SortListBuilder add(String key,boolean asc) {
        SortExp sortExp = new SortExp();
        sortExp.setKey(key);
        sortExp.setAsc(asc);
        sortList.add(sortExp);
        return this;
    }

    public List<SortExp> build() {
        return sortList;
    }
}
