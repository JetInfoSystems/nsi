package jet.isur.nsi.api.data;

import java.io.Serializable;

import jet.isur.nsi.api.model.MetaFieldType;

public class NsiParamValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private MetaFieldType type;
    private String value;
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
