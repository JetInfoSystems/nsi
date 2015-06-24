package jet.isur.nsi.api.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Строка справочника.
 */
public class DictRow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Список значений атрибутов строки.
     */
    private Map<String, DictRowAttr> attrs;

    public Map<String, DictRowAttr> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, DictRowAttr> attrs) {
        this.attrs = attrs;
    }

    @Override
    public String toString() {
        return "DictRow [attrs=" + attrs + "]";
    }
}
