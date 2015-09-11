package jet.isur.nsi.api.model.builder;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.model.MetaParamValue;

public class MetaParamsBuilder {

    private List<MetaParamValue> prototype;


    private List<MetaParamValue> getPrototype() {
        if(prototype == null) {
            prototype = new ArrayList<MetaParamValue>();
        }
        return prototype;
    }

    public MetaParamsBuilder add(MetaFieldType type, String value) {
        getPrototype().add(new MetaParamValue(type, value));
        return this;
    }

    public List<MetaParamValue> build() {
        List<MetaParamValue> result = getPrototype();
        prototype = null;
        return result;
    }

}
