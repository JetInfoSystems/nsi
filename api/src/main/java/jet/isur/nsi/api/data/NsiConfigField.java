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
    private boolean enableFts;
    private String defaultValue;
    
    
    public NsiConfigField(MetaField metaField) {
        name = metaField.getName();
        type = metaField.getType();
        size = metaField.getSize();
        precision = metaField.getPrecision();
        enumValues = metaField.getEnumValues();
        enableFts = metaField.isEnableFts();
        defaultValue = metaField.getDefaultValue();
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

    public boolean isEnableFts() {
        return enableFts;
    }

    public void setEnableFts(boolean enableFts) {
        this.enableFts = enableFts;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    
}
