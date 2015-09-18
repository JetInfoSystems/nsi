package jet.isur.nsi.generator.data;

public class Reference {
    private String dictName;
    private String fieldName;
    
    private String refAttrName;

    public Reference() {}

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }
    
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getRefAttrName() {
        return refAttrName;
    }

    public void setRefAttrName(String refAttrName) {
        this.refAttrName = refAttrName;
    }
}