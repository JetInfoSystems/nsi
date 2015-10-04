package jet.isur.nsi.api.data;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.model.MetaParamValue;

public class MetaParamsBuilder {

    private final List<MetaParamValue> prototype = new ArrayList<MetaParamValue>();
    private final NsiConfigDict dict;

    MetaParamsBuilder(NsiConfigDict dict) {
        this.dict  = dict;
    }

    public MetaParamsBuilder add(MetaFieldType type, String value) {
        prototype.add(new MetaParamValue(type, value));
        return this;
    }

    public List<MetaParamValue> build() {
        return prototype;
    }

}
