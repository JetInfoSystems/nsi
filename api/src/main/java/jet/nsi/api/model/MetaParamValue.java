package jet.nsi.api.model;

import java.io.Serializable;

public class MetaParamValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private MetaFieldType type = MetaFieldType.VARCHAR;
    private String value;

    public MetaParamValue() {

    }

    public MetaParamValue(MetaFieldType type, String value) {
        this.type = type;
        this.value = value;
    }

    public MetaFieldType getType() {
        return type;
    }
    public void setType(MetaFieldType type) {
        this.type = type;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

}
