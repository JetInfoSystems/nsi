package jet.isur.nsi.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.isur.nsi.api.model.MetaAttrType;

public class NsiQuery {
    public static final String MAIN_ALIAS = "m";
    public static final String MAIN_QUERY = "MAIN";
    private final NsiConfig config;
    private final NsiConfigDict dict;
    private int aliasIndex = 0;
    private List<NsiQueryAttr> attrs = new ArrayList<>();
    private Map<String,Map<String,NsiQueryAttr>> aliasNameMap = new HashMap<>();

    private String getNextAlias() {
        aliasIndex ++ ;
        return "a" + aliasIndex;
    }

    NsiQuery(NsiConfig config, NsiConfigDict dict) {
        this.config = config;
        this.dict = dict;
    }

    NsiQuery(NsiConfig config, String dictName) {
        this(config, config.getDict(dictName));
    }

    public NsiQuery addAttr(String alias, NsiConfigAttr attr) {
        Map<String,NsiQueryAttr> attrMap = getAttrMap(alias);
        String attrName = attr.getName();
        if(attrMap.containsKey(attrName)) {
            return this;
        }
        String refAlias = null;
        if(attr.getType() == MetaAttrType.REF) {
            refAlias = getNextAlias();
        }
        int index = attrs.size();
        NsiQueryAttr queryAttr = new NsiQueryAttr(alias, attr, refAlias, index);
        attrMap.put(attrName, queryAttr);
        attrs.add(queryAttr);
        return this;
    }

    public NsiQuery addAttr(String name) {
        return addAttr(MAIN_ALIAS, dict.getAttr(name));
    }

    public Map<String, NsiQueryAttr> getAttrMap(String alias) {
        if(!aliasNameMap.containsKey(alias)) {
            aliasNameMap.put(alias, new HashMap<String,NsiQueryAttr>());
        }
        return aliasNameMap.get(alias);
    }

    public NsiQuery addId() {
        return addAttr(MAIN_ALIAS, dict.getIdAttr());
    }

    public NsiQuery addDeleteMark() {
        return addAttr(MAIN_ALIAS, dict.getDeleteMarkAttr());
    }

    public NsiQuery addIsGroup() {
        return addAttr(MAIN_ALIAS, dict.getIsGroupAttr());
    }

    public NsiQuery addLastChange() {
        return addAttr(MAIN_ALIAS, dict.getLastChangeAttr());
    }

    public NsiQuery addLastUser() {
        return addAttr(MAIN_ALIAS, dict.getLastUserAttr());
    }

    public NsiQuery addOwner() {
        return addAttr(MAIN_ALIAS, dict.getOwnerAttr());
    }

    public NsiQuery addParent() {
        return addAttr(MAIN_ALIAS, dict.getParentAttr());
    }

    public NsiQuery addVersion() {
        return addAttr(MAIN_ALIAS, dict.getVersionAttr());
    }
    
    public NsiQuery addStdAttrs() {
        if(dict.getIdAttr() != null) addId();
        if(dict.getDeleteMarkAttr() != null) addDeleteMark();
        if(dict.getIsGroupAttr() != null) addIsGroup();
        if(dict.getLastChangeAttr() != null) addLastChange();
        if(dict.getLastUserAttr() != null) addLastUser();
        if(dict.getOwnerAttr() != null) addOwner();
        if(dict.getParentAttr() != null) addParent();
        if(dict.getVersionAttr() != null) addVersion();
        return this;
    }

    public NsiQuery addAttrs() {
        addAttrs(dict.getAttrs());
        return this;
    }

    public NsiQuery addAttrs(DictRow row) {
        for (String attrName : row.getAttrs().keySet()) {
            NsiConfigAttr attr = dict.getAttr(attrName);
            // добавляем только те атрибуты, которые есть в модели
            if(attr != null) {
                addAttr(MAIN_ALIAS, attr);
            }
        }

        return this;
    }
    
    public NsiQuery addDefaultAttrs() {
        for (NsiConfigAttr attr : dict.getAttrs()) {
            // добавляем только те атрибуты, которые есть в модели и дл которых назначено значение по умолчанию
            if(attr != null && attr.getDefaultValue() != null) {
                addAttr(MAIN_ALIAS, attr);
            }
        }

        return this;
    }
    
    public NsiQuery addRefObjectAttrs() {
        addStdAttrs();
        addAttrs(dict.getRefObjectAttrs());
        return this;
    }

    public NsiQuery addTableObjectAttrs() {
        addStdAttrs();
        addAttrs(dict.getTableObjectAttrs());
        return this;
    }

    private NsiQuery addAttrs(Collection<NsiConfigAttr> attrs) {
        for ( NsiConfigAttr attr : attrs) {
            addAttr(MAIN_ALIAS, attr);
        }
        return this;
    }

    public NsiConfigDict getDict() {
        return dict;
    }

    public List<NsiQueryAttr> getAttrs() {
        return attrs;
    }

    public NsiQueryAttr getAttr(String alias, String name) {
        return getAttrMap(alias).get(name);
    }

    public NsiQueryAttr getAttr(String name) {
        return getAttrMap(MAIN_ALIAS).get(name);
    }

    @Override
    public String toString() {
        return "NsiQuery [dict=" + dict + ", aliasIndex=" + aliasIndex
                + ", attrs=" + attrs + ", aliasNameMap=" + aliasNameMap + "]";
    }

    public NsiConfig getConfig() {
        return config;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dict == null) ? 0 : dict.hashCode());
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
        NsiQuery other = (NsiQuery) obj;
        if (dict == null) {
            if (other.dict != null)
                return false;
        } else if (!dict.equals(other.dict))
            return false;
        return true;
    }

}
