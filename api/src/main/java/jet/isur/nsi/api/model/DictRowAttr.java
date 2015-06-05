package jet.isur.nsi.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * Значение атрибута справочника
 */
public class DictRowAttr implements Serializable {
    private static final long serialVersionUID = 1L;

    private String attrName;
    /**
     * Значение полей, если полей несколько то порядок значений соответствует порядку в описании атрибута в метаданных
     */
    private List<String> values;
    /**
     * Если атрибут является сслкой то для него будут предоставлены атрибуты представляющие refObject.
     */
    private List<DictRowAttr> refAttrs;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public List<DictRowAttr> getRefAttrs() {
        return refAttrs;
    }

    public void setRefAttrs(List<DictRowAttr> refAttrs) {
        this.refAttrs = refAttrs;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

}
