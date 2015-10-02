package jet.isur.nsi.api.data;

import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.DictRow;

public class TypedDictRow extends DictRow {
    private final NsiConfigDict dict;

    public TypedDictRow(NsiConfigDict dict) {
        this.dict = dict;
    }

    public DictRowBuilder builder() {
        return new DictRowBuilder(dict, this);
    }
}
