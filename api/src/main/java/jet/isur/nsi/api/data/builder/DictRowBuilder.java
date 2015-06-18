package jet.isur.nsi.api.data.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.ConvertUtils;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.DictRowAttr;

import org.joda.time.DateTime;

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

    public DictRowBuilder idAttrNull() {
        idAttr();
        for ( NsiConfigField field : query.getDict().getIdAttr().getFields()) {
            attrBuilder.value(null);
        }
        attrBuilder.add();
        return this;
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
            throw new NsiServiceException("deleteMarkAttr not exists");
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

    public DictRowBuilder ownerAttr(Long value) {
        return ownerAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowBuilder attr(String name, Boolean value) {
        return attr(name,1).value(ConvertUtils.boolToString(value)).add();
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
        NsiQueryAttr queryAttr = query.getAttr(attrName);
        DictRowAttr attrValue = getAttr(attrName);
        return attrValue.getValues().get(0);
    }

    public Long getLong(String attrName) {
        NsiQueryAttr queryAttr = query.getAttr(attrName);
        DictRowAttr attrValue = getAttr(attrName);
        return ConvertUtils.stringToLong(attrValue.getValues().get(0));
    }

    public Long getLongIdAttr() {
        return getLong(query.getDict().getIdAttr().getName());
    }

    public DateTime getDateTime(String attrName) {
        NsiQueryAttr queryAttr = query.getAttr(attrName);
        DictRowAttr attrValue = getAttr(attrName);
        return ConvertUtils.stringToDateTime(attrValue.getValues().get(0));
    }

    public Boolean getBool(String attrName) {
        NsiQueryAttr queryAttr = query.getAttr(attrName);
        DictRowAttr attrValue = getAttr(attrName);
        return ConvertUtils.stringToBool(attrValue.getValues().get(0));
    }

}
