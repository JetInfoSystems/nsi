package jet.isur.nsi.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.isur.nsi.api.model.MetaDict;

public class NsiConfigDict {

    private final String name;
    private final String caption;
    private final String table;
    private final boolean hidden;

    private NsiConfigAttr idAttr;
    private NsiConfigAttr parentAttr;
    private NsiConfigAttr isGroupAttr;
    private NsiConfigAttr ownerAttr;
    private NsiConfigAttr deleteMarkAttr;
    private NsiConfigAttr lastChangeAttr;
    private NsiConfigAttr lastUserAttr;
    private List<NsiConfigAttr> captionAttrs = new ArrayList<>();
    private List<NsiConfigAttr> refObjectAttrs = new ArrayList<>();
    private List<NsiConfigAttr> tableObjectAttrs = new ArrayList<>();

    public NsiConfigDict(MetaDict metaDict) {
        name = metaDict.getName();
        caption = metaDict.getCaption();
        table = metaDict.getTable();
        hidden = metaDict.getHidden() == Boolean.TRUE ;
    }

    private List<NsiConfigField> fields = new ArrayList<>();
    private List<NsiConfigAttr> attrs = new ArrayList<>();
    private Map<String, NsiConfigAttr> attrNameMap = new HashMap<>();
    private Map<String, NsiConfigField> fieldNameMap = new HashMap<>();

    public Map<String, NsiConfigAttr> getAttrNameMap() {
        return attrNameMap;
    }

    public Map<String, NsiConfigField> getFieldNameMap() {
        return fieldNameMap;
    }

    public String getName() {
        return name;
    }

    public String getCaption() {
        return caption;
    }

    public String getTable() {
        return table;
    }

    public NsiConfigAttr getIdAttr() {
        return idAttr;
    }

    public void setIdAttr(NsiConfigAttr idAttr) {
        this.idAttr = idAttr;
    }

    public NsiConfigAttr getParentAttr() {
        return parentAttr;
    }

    public void setParentAttr(NsiConfigAttr parentAttr) {
        this.parentAttr = parentAttr;
    }

    public NsiConfigAttr getIsGroupAttr() {
        return isGroupAttr;
    }

    public void setIsGroupAttr(NsiConfigAttr isGroupAttr) {
        this.isGroupAttr = isGroupAttr;
    }

    public NsiConfigAttr getOwnerAttr() {
        return ownerAttr;
    }

    public void setOwnerAttr(NsiConfigAttr ownerAttr) {
        this.ownerAttr = ownerAttr;
    }

    public NsiConfigAttr getLastChangeAttr() {
        return lastChangeAttr;
    }

    public void setLastChangeAttr(NsiConfigAttr lastChangeAttr) {
        this.lastChangeAttr = lastChangeAttr;
    }

    public NsiConfigAttr getLastUserAttr() {
        return lastUserAttr;
    }

    public void setLastUserAttr(NsiConfigAttr lastUserAttr) {
        this.lastUserAttr = lastUserAttr;
    }

    public List<NsiConfigAttr> getCaptionAttrs() {
        return captionAttrs;
    }

    public List<NsiConfigAttr> getRefObjectAttrs() {
        return refObjectAttrs;
    }

    public List<NsiConfigAttr> getTableObjectAttrs() {
        return tableObjectAttrs;
    }

    public List<NsiConfigField> getFields() {
        return fields;
    }

    public List<NsiConfigAttr> getAttrs() {
        return attrs;
    }

    public NsiConfigAttr getDeleteMarkAttr() {
        return deleteMarkAttr;
    }

    public void setDeleteMarkAttr(NsiConfigAttr deleteMarkAttr) {
        this.deleteMarkAttr = deleteMarkAttr;
    }

    public void addField(NsiConfigField nsiMetaField) {
        fieldNameMap.put(nsiMetaField.getName().toUpperCase(), nsiMetaField);
        fields.add(nsiMetaField);
    }

    public void addAttr(NsiConfigAttr attr) {
        attrs.add(attr);
        attrNameMap.put(attr.getName().toUpperCase(), attr);
    }

    public NsiConfigField getField(String fieldName) {
        return fieldNameMap.get(fieldName.toUpperCase());
    }

    public NsiConfigAttr getAttr(String attrName) {
        return attrNameMap.get(attrName.toUpperCase());
    }

    public String getSeq() {
        return "seq_" + getTable();
    }


    public boolean isHidden() {
        return hidden;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NsiConfigDict other = (NsiConfigDict) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NsiConfigDict [name=" + name + "]";
    }

}
