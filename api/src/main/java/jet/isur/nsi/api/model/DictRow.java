package jet.isur.nsi.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * Строка справочника.
 */
public class DictRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Список значений атрибутов строки.
     */
    private List<DictRowAttr> attrs;

    public List<DictRowAttr> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<DictRowAttr> attrs) {
        this.attrs = attrs;
    }

}
