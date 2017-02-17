package jet.nsi.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.model.DictRowAttr;
import jet.nsi.api.model.MetaAttrType;
import jet.nsi.api.model.MetaDict;
import jet.nsi.api.model.MetaSourceQuery;
import jet.nsi.api.model.RefAttrsType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class NsiConfigDict {

    private final String name;
    private final String caption;
    private final String table;
    private final String databaseName;
    private final boolean hidden;

    private NsiConfigAttr idAttr;
    private NsiConfigAttr parentAttr;
    private NsiConfigAttr isGroupAttr;
    private NsiConfigAttr ownerAttr;
    private NsiConfigAttr deleteMarkAttr;
    private NsiConfigAttr lastChangeAttr;
    private NsiConfigAttr lastUserAttr;
    private List<NsiConfigAttr> uniqueAttr;
    private NsiConfigAttr versionAttr;
    private List<NsiConfigAttr> captionAttrs = new ArrayList<>();
    private List<NsiConfigAttr> refObjectAttrs = new ArrayList<>();
    private List<NsiConfigAttr> loadDataAttrs = new ArrayList<>();
    private List<NsiConfigAttr> tableObjectAttrs = new ArrayList<>();
    private List<NsiConfigAttr> mergeExternalAttrs = new ArrayList<>();
    private final String mainDictName;
    private NsiConfigDict mainDict;

    private List<String> interceptors = new ArrayList<>();
    private Set<String> labels;

    private Map<String, NsiConfigAttr> attrNameMap = new TreeMap<>();
    private Map<String, NsiConfigField> fieldNameMap = new TreeMap<>();
    private Map<String, NsiConfigSourceQuery> sourceQueries = new TreeMap<>();
    private Map<String, NsiConfigAttr> owns = new TreeMap<>();



    private final NsiConfig config;
    public NsiConfigDict(@JsonProperty("name") String name, @JsonProperty("caption") String caption,
                         @JsonProperty("table") String table, @JsonProperty("databaseName") String databaseName,
                         @JsonProperty("hidden") boolean hidden, @JsonProperty("mainDictName") String mainDictName,
                         @JsonProperty("config") NsiConfig config) {
        this.name = name;
        this.caption = caption;
        this.table = table;
        this.databaseName = databaseName;
        this.hidden = hidden;
        this.mainDictName = mainDictName;
        this.config = config;
    }

    public NsiConfigDict(NsiConfig config, MetaDict metaDict) {
        this.config = config;
        name = metaDict.getName();
        caption = metaDict.getCaption();
        table = metaDict.getTable();
        hidden = metaDict.getHidden() == Boolean.TRUE ;
        if(metaDict.getSourceQueries() != null) {
            for ( Entry<String, MetaSourceQuery> q : metaDict.getSourceQueries().entrySet()) {
                sourceQueries.put(q.getKey(), new NsiConfigSourceQuery(q.getValue()));
            }
        }

        mainDictName = metaDict.getMainDict();
        databaseName = metaDict.getDatabaseName();
    }


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
        // Выставляем обязательность ID в false, чтобы при вставки новой записи фронт не требовал наличие идентификатора  
        idAttr.setRequired(false);
        this.idAttr = idAttr;
    }


    public List<NsiConfigAttr> getUniqueAttr() {
        return uniqueAttr;
    }

    public void setUniqueAttr(List<NsiConfigAttr> uniqueAttr) {
        this.uniqueAttr = uniqueAttr;
        for(NsiConfigAttr attr : uniqueAttr) {
            attr.setRequired(true);
        }
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
        return Collections.unmodifiableList(captionAttrs);
    }

    public List<NsiConfigAttr> getRefObjectAttrs() {
        return Collections.unmodifiableList(refObjectAttrs);
    }

    public List<NsiConfigAttr> getTableObjectAttrs() {
        return Collections.unmodifiableList(tableObjectAttrs);
    }

    public List<NsiConfigAttr> getMergeExternalAttrs() {
        return Collections.unmodifiableList(mergeExternalAttrs);
    }

    @JsonIgnore
    public Collection<NsiConfigField> getFields() {
        return Collections.unmodifiableCollection(fieldNameMap.values());
    }

    @JsonIgnore
    public Collection<NsiConfigAttr> getAttrs() {
        return Collections.unmodifiableCollection(attrNameMap.values());
    }

    public NsiConfigAttr getDeleteMarkAttr() {
        return deleteMarkAttr;
    }

    public void setDeleteMarkAttr(NsiConfigAttr deleteMarkAttr) {
        this.deleteMarkAttr = deleteMarkAttr;
    }

    public void addField(NsiConfigField nsiMetaField) {
        fieldNameMap.put(nsiMetaField.getName().toUpperCase(), nsiMetaField);
    }

    public void addAttr(NsiConfigAttr attr) {
        attrNameMap.put(attr.getName().toUpperCase(), attr);
    }

    public NsiConfigField getField(String fieldName) {
        return fieldNameMap.get(fieldName.toUpperCase());
    }

    public NsiConfigAttr getAttr(String attrName) {
        return attrNameMap.get(attrName.toUpperCase());
    }

    @JsonIgnore
    public String getSeq() {
        return "seq_" + getTable();
    }


    public boolean isHidden() {
        return hidden;
    }

    public boolean isAttrHasRefAttrs(NsiConfigAttr attr) {
        // убрал оптимизацию, изз того что фронту требуются refAttrs, owner и parent могут редактироваться теоретически
        // TODO: подумать как сделать ее правильно
        return attr.getType() == MetaAttrType.REF ;//&& attr != ownerAttr && attr != parentAttr;
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

    public void setCaptionAttrs(List<NsiConfigAttr> captionAttrs) {
        this.captionAttrs = captionAttrs;
    }

    public void setRefObjectAttrs(List<NsiConfigAttr> refObjectAttrs) {
        this.refObjectAttrs = refObjectAttrs;
    }

    public void setTableObjectAttrs(List<NsiConfigAttr> tableObjectAttrs) {
        this.tableObjectAttrs = tableObjectAttrs;
    }

    public void setMergeExternalAttrs(List<NsiConfigAttr> mergeExternalAttrs) {
        this.mergeExternalAttrs = mergeExternalAttrs;
    }

    public List<String> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<String> interceptors) {
        this.interceptors = interceptors;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public NsiConfigSourceQuery getSourceQuery(String name) {
        NsiConfigSourceQuery result = sourceQueries.get(name);
        Preconditions.checkNotNull(result, "dict %s source query %s not exists", getName(), name);
        return result;
    }

    public void setOwns(Map<String, NsiConfigAttr> owns) {
        this.owns = owns;
    }

    public NsiConfigAttr getOwnAttr(String name) {

        return owns.get(name);
    }

    public NsiConfigDict getMainDict() {
        return mainDict;
    }

    public void setMainDict(NsiConfigDict mainDict) {
        this.mainDict = mainDict;
    }

    public String getMainDictName() {
        return mainDictName;
    }

    public DictRowBuilder builder() {
        return new DictRowBuilder(this);
    }

    public DictRowBuilder builder(DictRow data) {
        return new DictRowBuilder(this, data);
    }

    public DictRow newDictRow() {
        return new DictRow(this);
    }

    public DictRow newDictRow(Map<String, DictRowAttr> attrs) {
        return new DictRow(this, attrs);
    }

    public NsiQuery query() {
        return new NsiQuery(config, this);
    }

    public BoolExpBuilder filter() {
        return new BoolExpBuilder(this);
    }

    public SortListBuilder sort() {
        return new SortListBuilder(this);
    }

    public MetaParamsBuilder params() {
        return new MetaParamsBuilder(this);
    }

    public List<NsiConfigAttr> getLoadDataAttrs() {
        return loadDataAttrs;
    }

    public void setLoadDataAttrs(List<NsiConfigAttr> loadDataAttrs) {
        this.loadDataAttrs = loadDataAttrs;
    }

    public NsiConfigAttr getVersionAttr() {
        return versionAttr;
    }

    public void setVersionAttr(NsiConfigAttr versionAttr) {
        this.versionAttr = versionAttr;
    }

    public List<NsiConfigAttr> getRefAttrs(RefAttrsType refAttrsType) {
        switch (refAttrsType) {
        case REF_OBJECT_ATTRS:
            return refObjectAttrs;
        case MERGE_EXTERNAL_ATTRS:
            return mergeExternalAttrs;
        default:
            throw new NsiServiceException("неподдерживаемый refAttrsType: " + refAttrsType);
        }
    }

    public static String formatAttrs(String dictName, List<NsiConfigAttr> attrs, String sep) {
        StringBuilder res = new StringBuilder();
        for(NsiConfigAttr attr : attrs) {
            res.append(dictName).append(".").append(attr.getName()).append(sep);
        }

        return res.toString();
    }

    public String getDatabaseName() {
        return databaseName;
    }

}
