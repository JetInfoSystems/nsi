package jet.isur.nsi.api.data;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.model.MetaAttr;
import jet.isur.nsi.api.model.MetaAttrType;

public class NsiConfigAttr {

    private final MetaAttrType type;
    private final String valueType;
    private final String name;
    private final String caption;
    private final Boolean hidden;
    private final String refDictName;
    private final boolean required;

    private List<NsiConfigField> fields = new ArrayList<>();
    private NsiConfigDict refDict;

    public NsiConfigAttr(MetaAttr metaAttr) {
        type = metaAttr.getType();
        valueType = metaAttr.getValueType();
        name = metaAttr.getName();
        caption = metaAttr.getCaption();
        hidden = metaAttr.getHidden();
        refDictName = metaAttr.getRefDict();
        required = metaAttr.isRequired();
    }
    public MetaAttrType getType() {
        return type;
    }
    public String getValueType() {
        return valueType;
    }
    public String getName() {
        return name;
    }
    public String getCaption() {
        return caption;
    }
    public Boolean getHidden() {
        return hidden;
    }
    public List<NsiConfigField> getFields() {
        return fields;
    }
    public void setFields(List<NsiConfigField> fields) {
        this.fields = fields;
    }
    public NsiConfigDict getRefDict() {
        return refDict;
    }
    public void setRefDict(NsiConfigDict refDict) {
        this.refDict = refDict;
    }
    public String getRefDictName() {
        return refDictName;
    }
    public void addField(NsiConfigField field) {
        fields.add(field);
    }
    public boolean isRequired() {
        return required;
    }
    @Override
    public String toString() {
        return "NsiConfigAttr [type=" + type.toString() + ", valueType=" + valueType
                + ", name=" + name + ", caption=" + caption + ", hidden="
                + hidden + ", refDictName=" + refDictName + ", required="
                + required + ", fields=" + fields + ", refDict=" + refDict
                + "]";
    }


}
