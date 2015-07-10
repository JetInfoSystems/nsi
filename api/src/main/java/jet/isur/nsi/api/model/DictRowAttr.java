package jet.isur.nsi.api.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Значение атрибута справочника
 */
public class DictRowAttr implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Значение полей, если полей несколько то порядок значений соответствует порядку в описании атрибута в метаданных
     */
    private List<String> values;
    /**
     * Если атрибут является сслкой то для него будут предоставлены атрибуты представляющие refObject.
     */
    private Map<String, DictRowAttr> refAttrs;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void addValue(String value) {
        if(values == null) {
            values = new LinkedList<>();
        }
        values.add(value);
    }

    public String getFirstValue() {
        if(values != null && !values.isEmpty()) {
            return values.iterator().next();
        }
        return null;
    }

    public Map<String, DictRowAttr> getRefAttrs() {
        return refAttrs;
    }

    public void setRefAttrs(Map<String, DictRowAttr> refAttrs) {
        this.refAttrs = refAttrs;
    }

    @Override
    public String toString() {
        return "DictRowAttr [values=" + values + ", refAttrs=" + refAttrs + "]";
    }

}
