package jet.isur.nsi.generator.dictdata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DictDataObject {

    protected String dictName;

    protected Map<String, Collection<String>> fields = new HashMap<>();
    
    private int rowCount;

	private String parentFieldName;

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public Map<String, Collection<String>> getFields() {
        return fields;
    }

    public void setFields(Map<String, Collection<String>> fields) {
        this.fields = fields;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public String getParentFieldName() {
        return parentFieldName;
    }

    public void setParentFieldName(String parentFieldName) {
        this.parentFieldName = parentFieldName;
    }
}
