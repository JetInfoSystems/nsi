package jet.isur.nsi.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.model.DictRowAttr;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.MetaFieldType;

import org.joda.time.DateTime;

public class DictRow {
    private final NsiConfigDict dict;
    private final Map<String, DictRowAttr> attrs;

    DictRow(NsiConfigDict dict, Map<String, DictRowAttr> attrs) {
        this.dict = dict;
        this.attrs = attrs;
    }

    DictRow(NsiConfigDict dict) {
        this(dict, new HashMap<String, DictRowAttr>(dict.getAttrs().size()));
    }

    DictRow(NsiConfigDict dict, DictRow data) {
        this(dict, data.getAttrs());
    }

    public DictRowBuilder builder() {
        return new DictRowBuilder(dict, this);
    }

    private NsiConfigAttr getDictAttr(String name) {
        NsiConfigAttr result = dict.getAttr(name);
        if(result == null) {
            throw new NsiServiceException("dict %s attr %s not found", dict.getName(), name);
        }
        return result;
    }

    DictRowAttr getAttr(NsiConfigAttr a) {
        return getAttrs().get(a.getName());
    }

    public DictRowAttr getAttr(String name) {
        return getAttr(getDictAttr(name));
    }

    void setAttr(NsiConfigAttr a, DictRowAttr value) {
        if(value == null) {
            value = createNullValue(a);
        }
        getAttrs().put(a.getName(), value);
    }

    void cleanAttr(NsiConfigAttr a) {
        setAttr(a, (DictRowAttr) null);
    }

    public void cleanAttr(String name) {
        cleanAttr(getDictAttr(name));
    }

    public void setAttr(String name, DictRowAttr value) {
        setAttr(getDictAttr(name), value);
    }

    DictRowAttr createNullValue(NsiConfigAttr a) {
        DictRowAttr result = new DictRowAttr();
        List<String> values = new ArrayList<>(a.getFields().size());
        for ( int i=0;i< a.getFields().size();i++) {
            values.add(null);
        }
        result.setValues(values);
        return result;
    }

    public void setAttr(String name, DictRow value) {
       NsiConfigAttr a = getDictAttr(name);
       if(value == null) {
           setAttr(a, createNullValue(a));
       } else {
           if(a.getType() != MetaAttrType.REF) {
               throw new NsiServiceException("dict %s attr %s is not ref",dict.getName(), a.getName());
           }
           if(a.getRefDict() != value.getDict()) {
               throw new NsiServiceException("dict %s setAttr %s type mismatch %s",dict.getName(), name, value.getDict().getName());
           }

           DictRowAttr refValue = new DictRowAttr();
           refValue.setValues(value.getIdAttr().getValues());

           if(dict.isAttrHasRefAttrs(a)) {
               List<NsiConfigAttr> refObjectAttrs = a.getRefDict().getRefObjectAttrs();
               if(refObjectAttrs.size() > 0) {
                   Map<String, DictRowAttr> refAttrMap = new HashMap<>(refObjectAttrs.size());
                   for (NsiConfigAttr refAttr : refObjectAttrs ) {
                       // получаю значение атрибута, из него нужно взять только values
                       DictRowAttr refAttrValue = value.getAttr(refAttr.getName());
                       DictRowAttr tmp = new DictRowAttr();
                       tmp.setValues(refAttrValue.getValues());
                       refAttrMap.put(refAttr.getName(),tmp );
                   }
                   refValue.setRefAttrs(refAttrMap);
               }
           }
           setAttr(a, refValue);


       }
    }

    public NsiConfigDict getDict() {
        return dict;
    }

    NsiConfigAttr checkDictAttrNotNull(NsiConfigAttr attr, String name) {
        if(attr == null) {
            throw new NsiServiceException("dict %s has not %s",dict.getName(), name);
        }
        return attr;
    }

    NsiConfigAttr checkDictAttrOneField(NsiConfigAttr attr) {
        if(attr.getFields().size() != 1) {
            throw new NsiServiceException("dict %s contain many fields",dict.getName(), attr.getName());
        }
        return attr;
    }

    private void checkAttrPrecision0(NsiConfigAttr a) {
        if(a.getFields().get(0).getPrecision() > 0) {
            throw createAttrTypeIncopatibleError(a, MetaFieldType.NUMBER);
        }
    }

    NsiConfigAttr getDictDeleteMarkAttr() {
        return checkDictAttrNotNull(dict.getDeleteMarkAttr(),"deleteMarkAttr");
    }

    public DictRowAttr getDeleteMarkAttr() {
        return getAttr(getDictDeleteMarkAttr().getName());
    }

    public void setDeleteMarkAttr(DictRowAttr value) {
        setAttr(getDictDeleteMarkAttr(), value);
    }

    NsiConfigAttr getDictIdAttr() {
        return checkDictAttrNotNull(dict.getIdAttr(),"idAttr");
    }

    public DictRowAttr getIdAttr() {
        return getAttr(getDictIdAttr().getName());
    }

    public void setIdAttr(DictRowAttr value) {
        setAttr(getDictIdAttr(), value);
    }

    NsiConfigAttr getDictIsGroupAttr() {
        return checkDictAttrNotNull(dict.getIsGroupAttr(),"isGroupAttr");
    }

    public DictRowAttr getIsGroupAttr() {
        return getAttr(getDictIsGroupAttr().getName());
    }

    public void setIsGroupAttr(DictRowAttr value) {
        setAttr(getDictIsGroupAttr(), value);
    }

    NsiConfigAttr getDictLastChangeAttr() {
        return checkDictAttrNotNull(dict.getLastChangeAttr(),"lastChangeAttr");
    }

    public DictRowAttr getLastChangeAttr() {
        return getAttr(getDictLastChangeAttr().getName());
    }

    public void setLastChangeAttr(DictRowAttr value) {
        setAttr(getDictLastChangeAttr(), value);
    }

    NsiConfigAttr getDictLastUserAttr() {
        return checkDictAttrNotNull(dict.getLastUserAttr(),"lastUserAttr");
    }

    public DictRowAttr getLastUserAttr() {
        return getAttr(getDictLastUserAttr().getName());
    }

    public void setLastUserAttr(DictRowAttr value) {
        setAttr(getDictLastUserAttr(), value);
    }

    NsiConfigAttr getDictOwnerAttr() {
        return checkDictAttrNotNull(dict.getOwnerAttr(),"ownerAttr");
    }

    public DictRowAttr getOwnerAttr() {
        return getAttr(getDictOwnerAttr().getName());
    }

    public void setOwnerAttr(DictRowAttr value) {
        setAttr(getDictOwnerAttr(), value);
    }

    NsiConfigAttr getDictParentAttr() {
        return checkDictAttrNotNull(dict.getParentAttr(),"parentAttr");
    }

    public DictRowAttr getParentAttr() {
        return getAttr(getDictParentAttr().getName());
    }

    public void setParentAttr(DictRowAttr value) {
        setAttr(getDictParentAttr(), value);
    }

    String getOneValue(DictRowAttr value) {
        if(value == null) {
            return null;
        } else {
            if(value.getValues().size() != 1) {
                throw new NsiServiceException("dict %s row must have one value, actual: %s", dict.getName(), value.getValues());
            }
            return value.getValues().get(0);
        }
    }

    NsiServiceException createAttrTypeIncopatibleError(NsiConfigAttr a, MetaFieldType fieldType) {
        return new NsiServiceException("dict %s attr %s type incapatible with %s", dict.getName(), a.getName(), fieldType);
    }

    Boolean getBoolean(NsiConfigAttr a) {
        switch (a.getFields().get(0).getType()) {
        case BOOLEAN:
            return ConvertUtils.stringToBool(getOneValue(getAttr(a)));
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.BOOLEAN);
        }
    }

    public Boolean getBoolean(String name) {
        return getBoolean(checkDictAttrOneField(getDictAttr(name)));
    }

    String getString(NsiConfigAttr a) {
        return getOneValue(getAttr(a));
    }

    public String getString(String name) {
        return getString(checkDictAttrOneField(getDictAttr(name)));
    }

    DateTime getDateTime(NsiConfigAttr a) {
        switch (a.getFields().get(0).getType()) {
        case DATE_TIME:
            return ConvertUtils.stringToDateTime(getOneValue(getAttr(a)));
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.DATE_TIME);
        }
    }

    public DateTime getDateTime(String name) {
        return getDateTime(checkDictAttrOneField(getDictAttr(name)));
    }

    Long getLong(NsiConfigAttr a) {
        switch (a.getFields().get(0).getType()) {
        case NUMBER:
        case CHAR:
        case VARCHAR:
            checkAttrPrecision0(a);
            return ConvertUtils.stringToLong(getOneValue(getAttr(a)));
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.NUMBER);
        }
    }

    public Long getLong(String name) {
        return getLong(checkDictAttrOneField(getDictAttr(name)));
    }

    Integer getInteger(NsiConfigAttr a) {
        switch (a.getFields().get(0).getType()) {
        case NUMBER:
        case CHAR:
        case VARCHAR:
            checkAttrPrecision0(a);
            return ConvertUtils.stringToInteger(getOneValue(getAttr(a)));
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.NUMBER);
        }
    }

    public Integer getInteger(String name) {
        return getInteger(checkDictAttrOneField(getDictAttr(name)));
    }

    Double getDouble(NsiConfigAttr a) {
        switch (a.getFields().get(0).getType()) {
        case NUMBER:
            return ConvertUtils.stringToDouble(getOneValue(getAttr(a)));
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.NUMBER);
        }
    }

    public Double getDouble(String name) {
        return getDouble(checkDictAttrOneField(getDictAttr(name)));
    }

    void setAttr(NsiConfigAttr a, String value) {
        setAttr(a, DictRowAttrBuilder.from(value));
    }

    public void setAttr(String name, Boolean value) {
        NsiConfigAttr a = checkDictAttrOneField(getDictAttr(name));
        setAttr(a, DictRowAttrBuilder.from(value));
    }

    void setAttr(NsiConfigAttr a, Boolean value) {
        switch (a.getFields().get(0).getType()) {
        case BOOLEAN:
            setAttr(a, ConvertUtils.boolToString(value));
            break;
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.BOOLEAN);
        }
    }

    public void setAttr(String name, String value) {
        NsiConfigAttr a = checkDictAttrOneField(getDictAttr(name));
        setAttr(a, DictRowAttrBuilder.from(value));
    }

    void setAttr(NsiConfigAttr a, DateTime value) {
        switch (a.getFields().get(0).getType()) {
        case DATE_TIME:
            setAttr(a, ConvertUtils.dateTimeToString(value));
            break;
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.DATE_TIME);
        }
    }

    public void setAttr(String name, DateTime value) {
        setAttr(checkDictAttrOneField(getDictAttr(name)), value);
    }

    void setAttr(NsiConfigAttr a, Long value) {
        switch (a.getFields().get(0).getType()) {
        case NUMBER:
        case CHAR:
        case VARCHAR:
            checkAttrPrecision0(a);
            setAttr(a, ConvertUtils.longToString(value));
            break;
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.NUMBER);
        }
    }

    public void setAttr(String name, Long value) {
        setAttr(checkDictAttrOneField(getDictAttr(name)), value);
    }

    void setAttr(NsiConfigAttr a, Integer value) {
        switch (a.getFields().get(0).getType()) {
        case NUMBER:
        case CHAR:
        case VARCHAR:
            checkAttrPrecision0(a);
            setAttr(a, ConvertUtils.integerToString(value));
            break;
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.NUMBER);
        }
    }

    public void setAttr(String name, Integer value) {
        setAttr(checkDictAttrOneField(getDictAttr(name)), value);
    }

    void setAttr(NsiConfigAttr a, Double value) {
        switch (a.getFields().get(0).getType()) {
        case NUMBER:
            setAttr(a, ConvertUtils.doubleToString(value));
            break;
        default:
            throw createAttrTypeIncopatibleError(a, MetaFieldType.NUMBER);
        }
    }

    public void setAttr(String name, Double value) {
        setAttr(checkDictAttrOneField(getDictAttr(name)), value);
    }

    public String getIdAttrString() {
        return getString(getDictIdAttr());
    }

    public Long getIdAttrLong() {
        return getLong(getDictIdAttr());
    }

    public void cleanIdAttr() {
        cleanAttr(getDictIdAttr());
    }

    public void setIdAttr(String value) {
        setAttr(getDictIdAttr(), value);
    }

    public void setIdAttr(Long value) {
        setAttr(getDictIdAttr(), value);
    }

    public Boolean getDeleteMarkAttrBoolean() {
        return getBoolean(getDictDeleteMarkAttr());
    }

    public void setDeleteMarkAttr(Boolean value) {
        setAttr(getDictDeleteMarkAttr(), value);
    }

    public Boolean getIsGroupAttrBoolean() {
        return getBoolean(getDictIsGroupAttr());
    }

    public void setIsGroupMarkAttr(Boolean value) {
        setAttr(getDictIsGroupAttr(), value);
    }

    public DateTime getLastChangeDateTime() {
        return getDateTime(getDictLastChangeAttr());
    }

    public void setLastChangeAttr(DateTime value) {
        setAttr(getDictLastChangeAttr(), value);
    }

    public Long getOwnerLong() {
        return getLong(getDictOwnerAttr());
    }

    public void cleanOwnerAttr() {
        cleanAttr(getDictOwnerAttr());
    }

    public void setOwnerAttr(Long value) {
        setAttr(getDictOwnerAttr(), value);
    }

    public String getOwnerString() {
        return getString(getDictOwnerAttr());
    }

    public void setOwnerAttr(String value) {
        setAttr(getDictOwnerAttr(), value);
    }

    public Long getParentLong() {
        return getLong(getDictParentAttr());
    }

    public void cleanParentAttr() {
        cleanAttr(getDictParentAttr());
    }

    public void setParentAttr(Long value) {
        setAttr(getDictParentAttr(), value);
    }

    public String getParentString() {
        return getString(getDictParentAttr());
    }

    public void setParentAttr(String value) {
        setAttr(getDictParentAttr(), value);
    }

    public Long getLastUserLong() {
        return getLong(getDictLastUserAttr());
    }

    public void cleanLastUserAttr() {
        cleanAttr(getDictLastUserAttr());
    }

    public void setLastUserAttr(Long value) {
        setAttr(getDictLastUserAttr(), value);
    }

    public String getLastUserString() {
        return getString(getDictLastUserAttr());
    }

    public void setLastUserAttr(String value) {
        setAttr(getDictLastUserAttr(), value);
    }

    public Map<String, DictRowAttr> getAttrs() {
        return attrs;
    }

}
