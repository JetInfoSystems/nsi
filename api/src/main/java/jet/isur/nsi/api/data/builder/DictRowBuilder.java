package jet.isur.nsi.api.data.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.ConvertUtils;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;

import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class DictRowBuilder {

    private final NsiQuery query;
    private DictRowAttrBuilder attrBuilder;
    private DictRow prototype;


    public DictRowBuilder(NsiQuery query) {
        this.query = query;
        attrBuilder = new DictRowAttrBuilder(this);
    }

    public DictRowBuilder(NsiQuery query, DictRow data) {
        this(query);
        setPrototype(data);
    }

    public NsiQuery getQuery() {
        return query;
    }

    public static DictRow cloneRow(DictRow src) {
        if(src == null) {
            return null;
        }
        DictRow dst = new DictRow();
        dst.setAttrs(cloneAttrs(src.getAttrs()));
        return dst;
    }

    private static Map<String,DictRowAttr> cloneAttrs(Map<String, DictRowAttr> srcAttrs) {
        if(srcAttrs == null) {
            return null;
        }
        Map<String, DictRowAttr> dstAttrs = new HashMap<>(srcAttrs.size());
        for (String k : srcAttrs.keySet()) {
            DictRowAttr srcAttr = srcAttrs.get(k);
            dstAttrs.put(k,cloneAttr(srcAttr));
        }
        return dstAttrs;
    }

    private static DictRowAttr cloneAttr(DictRowAttr srcAttr) {
        if(srcAttr == null) {
            return null;
        }
        DictRowAttr dstAttr = new DictRowAttr();
        dstAttr.setRefAttrs(cloneAttrs(srcAttr.getRefAttrs()));
        dstAttr.setValues(cloneValues(srcAttr.getValues()));
        return dstAttr;
    }


    private static List<String> cloneValues(List<String> srcValues) {
        if(srcValues == null) {
            return null;
        }
        return new ArrayList<>(srcValues);
    }

    public void setPrototype(DictRow data) {
        this.prototype = data;
    }

    DictRow getPrototype() {
        if(prototype==null) {
            prototype = new DictRow();
            prototype.setAttrs(new HashMap<String, DictRowAttr>(query.getAttrs().size()));
        }
        return prototype;
    }

    public DictRowAttrBuilder attr(String name) {
        return attr(name,1);
    }

    public DictRowAttrBuilder attr(NsiQueryAttr queryAttr) {
        NsiConfigAttr attr = queryAttr.getAttr();
        return attr(attr.getName(),attr.getFields().size());
    }

    private DictRowAttrBuilder attr(String name, int valuesSize) {
        DictRowAttr attrPrototype = getPrototype().getAttrs().get(name);
        if(attrPrototype == null) {
            attrPrototype = new DictRowAttr();
            attrPrototype.setValues(new ArrayList<String>(valuesSize));
        }
        attrBuilder.setPrototype(name, attrPrototype);
        return attrBuilder;
    }

    public DictRowAttrBuilder idAttr() {
        NsiConfigAttr a = query.getDict().getIdAttr();
        return attr(a);
    }

    private List<String> createNullList(NsiConfigAttr attr) {
        List<String> result = new ArrayList<>();
        for ( NsiConfigField field : attr.getFields()) {
            result.add(null);
        }
        return result;
    }

    public DictRowBuilder idAttrNull() {
        return attrNull(query.getDict().getIdAttr().getName());
    }

    public DictRowAttrBuilder attr(NsiConfigAttr dictAttr) {
        NsiQueryAttr queryAttr = query.getAttr(dictAttr.getName());
        if(queryAttr == null) {
            throw new NsiServiceException("attr not in query: " + dictAttr.getName());
        }
        return attr(queryAttr);
    }

    public DictRowBuilder idAttr(Long value) {
        return idAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowAttrBuilder deleteMarkAttr() {
        NsiConfigAttr a = query.getDict().getDeleteMarkAttr();
        if(a == null) {
            throw new NsiServiceException("deleteMarkAttr not exists: " + query.getDict().getName());
        }
        return attr(a);
    }

    public DictRowBuilder deleteMarkAttr(boolean value) {
        return deleteMarkAttr().value(ConvertUtils.boolToString(value)).add();
    }

    public DictRowAttrBuilder isGroupAttr() {
        NsiConfigAttr a = query.getDict().getIsGroupAttr();
        if(a == null) {
            throw new NsiServiceException("isGroupAttr not exists");
        }
        return attr(a);
    }

    public DictRowBuilder isGroupAttr(boolean value) {
        return isGroupAttr().value(ConvertUtils.boolToString(value)).add();
    }

    public DictRowAttrBuilder lastChangeAttr() {
        NsiConfigAttr a = query.getDict().getLastChangeAttr();
        if(a == null) {
            throw new NsiServiceException("lastChangeAttr not exists");
        }
        return attr(a);
    }

    public DictRowBuilder lastChangeAttr(DateTime value) {
        return lastChangeAttr().value(ConvertUtils.dateTimeToString(value)).add();
    }

    public DictRowAttrBuilder lastUserAttr() {
        NsiConfigAttr a = query.getDict().getLastUserAttr();
        if(a == null) {
            throw new NsiServiceException("lastUserAttr not exists");
        }
        return attr(a);
    }

    public DictRowBuilder lastUserAttr(Long value) {
        return lastUserAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowAttrBuilder parentAttr() {
        NsiConfigAttr a = query.getDict().getParentAttr();
        if(a == null) {
            throw new NsiServiceException("parentAttr not exists");
        }
        return attr(a);
    }

    public DictRowBuilder parentAttr(Long value) {
        return parentAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowAttrBuilder ownerAttr() {
        NsiConfigAttr a = query.getDict().getOwnerAttr();
        if(a == null) {
            throw new NsiServiceException("ownerAttr not exists");
        }
        return attr(a);
    }

    public DictRowBuilder builder(NsiConfigDict dict) {
        return new DictRowBuilder(new NsiQuery(query.getConfig(), dict).addAttrs());
    }

    public DictRowBuilder builder(NsiConfigDict dict, DictRow data) {
        DictRowBuilder result = builder(dict);
        result.setPrototype(data);
        return result;
    }

    public DictRowBuilder ownerAttr(Long value) {
        return ownerAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowBuilder attr(String name, Boolean value) {
        return attr(name,1).value(ConvertUtils.boolToString(value)).add();
    }

    private NsiQueryAttr queryGetAttr(String name) {
        NsiQueryAttr result = query.getAttr(name);
        if(result == null) {
            throw new NsiServiceException("attr {} not found in query from dict {}", name, query.getDict().getName());
        }
        return result;
    }

    public DictRowBuilder attr(String name, DictRow data) {
        NsiConfigDict refDict = queryGetAttr(name).getAttr().getRefDict();
        if(refDict == null) {
            throw new NsiServiceException("dict {} attr {} is not ref", query.getDict().getName(), name);
        }
        if(data != null) {
            return attr(name,builder(refDict, data));
        } else {
            return attrNull(name);
        }
    }

    public DictRowBuilder attrNull(String name) {
        return attr(name,1).value(createNullList(queryGetAttr(name).getAttr())).add();
    }

    public DictRowBuilder attr(String name, DictRowBuilder valueBuilder) {
        DictRowAttrBuilder builder = attr(name,1).value(valueBuilder.getIdAttr().getValues());
        NsiConfigDict dict = query.getDict();
        NsiConfigAttr attr = dict.getAttr(name);
        if(dict.isAttrHasRefAttrs(attr)) {
            List<NsiConfigAttr> refObjectAttrs = valueBuilder.getQuery().getDict().getRefObjectAttrs();
            if(refObjectAttrs.size() > 0) {
                Map<String, DictRowAttr> refAttrMap = new HashMap<>(refObjectAttrs.size());
                for (NsiConfigAttr refAttr : refObjectAttrs ) {
                    // получаю значение атрибута, из него нужно взять только values
                    DictRowAttr refAttrValue = valueBuilder.getAttr(refAttr.getName());
                    DictRowAttr tmp = new DictRowAttr();
                    tmp.setValues(refAttrValue.getValues());
                    refAttrMap.put(refAttr.getName(),tmp );
                }
                builder.refAttrs(refAttrMap);
            }
        }

        return builder.add();
    }

    public DictRowBuilder attr(String name, DictRowAttr value) {
        DictRowAttrBuilder builder = attr(name,1).value(value.getValues());
        NsiConfigDict dict = query.getDict();
        NsiConfigAttr attr = dict.getAttr(name);
        if(value.getRefAttrs() != null && dict.isAttrHasRefAttrs(attr)) {
            builder.refAttrs(Maps.newHashMap(value.getRefAttrs()));
        }
        return builder.add();
    }

    public DictRowBuilder attr(String name, Long value) {
        return attr(name,1).value(ConvertUtils.longToString(value)).add();
    }

    public DictRowBuilder attr(String name, String value) {
        return attr(name,1).value(value).add();
    }

    public DictRowBuilder attr(String name, DateTime value) {
        return attr(name,1).value(ConvertUtils.dateTimeToString(value)).add();
    }

    public DictRow build() {
        DictRow result = getPrototype();
        prototype = null;
        return result;
    }

    public DictRowAttr getAttr(String attrName) {
        NsiQueryAttr queryAttr = query.getAttr(attrName);
        if(queryAttr == null) {
            throw new NsiServiceException("attr not in query: " + attrName);
        }
        return getPrototype().getAttrs().get(attrName);
    }

    public DictRowAttr getIdAttr() {
        return getAttr(query.getDict().getIdAttr().getName());
    }

    public String getString(String attrName) {
        DictRowAttr attrValue = getAttr(attrName);
        if(attrValue != null && attrValue.getValues() != null) {
            return attrValue.getValues().get(0);
        } else {
            return null;
        }
    }

    public Long getLong(String attrName) {
        DictRowAttr attrValue = getAttr(attrName);
        if(attrValue != null && attrValue.getValues() != null) {
            return ConvertUtils.stringToLong(attrValue.getValues().get(0));
        } else {
            return null;
        }
    }

    public Long getLongIdAttr() {
        return getLong(query.getDict().getIdAttr().getName());
    }

    public Boolean getDeleteMarkAttr() {
        return getBool(query.getDict().getDeleteMarkAttr().getName());
    }

    public DateTime getDateTime(String attrName) {
        DictRowAttr attrValue = getAttr(attrName);
        if(attrValue != null && attrValue.getValues() != null) {
            return ConvertUtils.stringToDateTime(attrValue.getValues().get(0));
        } else {
            return null;
        }
    }

    public Boolean getBool(String attrName) {
        DictRowAttr attrValue = getAttr(attrName);
        if(attrValue != null && attrValue.getValues() != null) {
            return ConvertUtils.stringToBool(attrValue.getValues().get(0));
        } else {
            return null;
        }
    }

}
