package jet.isur.nsi.api.data;

import java.util.Map;

import jet.isur.nsi.api.model.MetaField;
import jet.isur.nsi.api.model.MetaFieldType;

public class NsiConfigField {

    private final String name;
    private final MetaFieldType type;
    private final Integer size;
    private final Integer precision;
    private final Map<String, String> enumValues;

    public NsiConfigField(MetaField metaField) {
        name = metaField.getName();
        type = metaField.getType();
        size = metaField.getSize();
        precision = metaField.getPrecision();
        enumValues = metaField.getEnumValues();
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

    @Override
    public String toString() {
        return "NsiConfigField [name=" + name + ", type=" + type.toString()
                + ", size=" + size + ", precision=" + precision + "]";
    }

    public Map<String, String> getEnumValues() {
        return enumValues;
    }

}
