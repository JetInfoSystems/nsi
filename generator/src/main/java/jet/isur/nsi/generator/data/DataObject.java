package jet.isur.nsi.generator.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

public abstract class DataObject {
    protected String dictName;
    
    protected DataObjectType type;

    protected Map<String, Collection<String>> fields = new HashMap<>();
    
    protected int rowCount;
    
    protected JsonObject obj;
    
    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public DataObjectType getType() {
        return type;
    }

    public void setType(DataObjectType type) {
        this.type = type;
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

    public JsonObject getObj() {
        return obj;
    }

    public void setObj(JsonObject obj) {
        this.obj = obj;
    }

    public enum DataObjectType {
        simple,row;
    }
}
