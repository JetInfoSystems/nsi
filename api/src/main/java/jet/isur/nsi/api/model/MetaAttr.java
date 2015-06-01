package jet.isur.nsi.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * Описание атрибута справочника.
 */
public class MetaAttr implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * Тип атрибута значение или ссылка: value | ref.
     */
    private String attrType;
    public static final String ATTR_TYPE_VALUE = "value";
    public static final String ATTR_TYPE_REF = "ref";
    /**
     * Тип значения, используется в тех случаях когда набор полей описывает один атрибут, например адрес.
     */
    private String valueType;
    /**
     * Уникальное имя атрибута в справочнике.
     */
    private String name;
    /**
     * Заголовок атрибута
     */
    private String caption;
    /**
     * Список наименований.
     */
    private List<String> fields;
    /**
     * Наименование словаря на который ссылается атрибут.
     */
    private String refDict;
    /**
     * Атрибут является скрытым.
     */
    private Boolean hidden;
    public String getAttrType() {
        return attrType;
    }
    public void setAttrType(String attrType) {
        this.attrType = attrType;
    }
    public String getValueType() {
        return valueType;
    }
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }
    public List<String> getFields() {
        return fields;
    }
    public void setFields(List<String> fields) {
        this.fields = fields;
    }
    public String getRefDict() {
        return refDict;
    }
    public void setRefDict(String refDict) {
        this.refDict = refDict;
    }
    public Boolean getHidden() {
        return hidden;
    }
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }


}
