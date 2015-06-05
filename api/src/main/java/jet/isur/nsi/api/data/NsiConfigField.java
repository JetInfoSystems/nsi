package jet.isur.nsi.api.data;

import jet.isur.nsi.api.model.MetaField;
import jet.isur.nsi.api.model.MetaFieldType;

public class NsiConfigField {

    private final String name;
    private final MetaFieldType type;
    private final Integer size;
    private final Integer precision;

    public NsiConfigField(MetaField metaField) {
        name = metaField.getName();
        type = metaField.getType();
        size = metaField.getSize();
        precision = metaField.getPrecision();
    }

    public String getName() {
        return name;
    }

    public MetaFieldType getType() {
        return type;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getPrecision() {
        return precision;
    }


}
