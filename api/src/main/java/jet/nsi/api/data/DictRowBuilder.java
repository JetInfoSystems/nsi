package jet.nsi.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jet.nsi.api.NsiServiceException;
import jet.nsi.api.model.DictRowAttr;
import jet.nsi.api.model.MetaAttrType;

import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class DictRowBuilder {

    private final NsiConfigDict dict;
    private DictRowAttrBuilder attrBuilder;
    private DictRow prototype;

    DictRowBuilder(NsiConfigDict dict) {
        this(dict, null);
    }

    DictRowBuilder(NsiConfigDict dict, DictRow data) {
        this.dict = dict;
        this.attrBuilder = new DictRowAttrBuilder(this);
        prototype(data);
    }

    public static DictRow cloneRow(DictRow src) {
        if(src == null) {
            return null;
        }
        return new DictRow(src.getDict(),cloneAttrs(src.getAttrs()));
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

    public DictRowBuilder prototype(DictRow data) {
        if(data != null) {
            if(dict != data.getDict()) {
                throw new NsiServiceException("builder with type %s cannot use data type %s",dict.getName(),data.getDict().getName());
            }
        }
        this.prototype = data;
        return this;
    }

    DictRow getPrototype() {
        if(prototype==null) {
            prototype = new DictRow(dict);
        }
        return prototype;
    }

    public DictRowAttrBuilder attr(String name) {
        return attr(name,1);
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

    public DictRowAttrBuilder attr(NsiConfigAttr dictAttr) {
        return attr(getDictAttr(dictAttr.getName()).getName(), dictAttr.getFields().size());
    }

    private List<String> createNullList(NsiConfigAttr attr) {
        List<String> result = new ArrayList<>();
        for(int i=0;i<attr.getFields().size();i++) {
            result.add(null);
        }
        return result;
    }

    public DictRowAttrBuilder idAttr() {
        NsiConfigAttr a = dict.getIdAttr();
        return attr(a);
    }

    public DictRowBuilder idAttrNull() {
        return attrNull(dict.getIdAttr().getName());
    }

    public DictRowBuilder idAttr(Long value) {
        return idAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowBuilder idAttr(String value) {
        return idAttr().value(value).add();
    }

    public DictRowAttrBuilder deleteMarkAttr() {
        NsiConfigAttr a = dict.getDeleteMarkAttr();
        if(a == null) {
            throw new NsiServiceException("deleteMarkAttr not exists in dict %s", dict.getName());
        }
        return attr(a);
    }

    public DictRowBuilder deleteMarkAttr(boolean value) {
        return deleteMarkAttr().value(ConvertUtils.boolToString(value)).add();
    }

    public DictRowAttrBuilder isGroupAttr() {
        NsiConfigAttr a = dict.getIsGroupAttr();
        if(a == null) {
            throw new NsiServiceException("isGroupAttr not exists in dict %s", dict.getName());
        }
        return attr(a);
    }

    public DictRowBuilder isGroupAttr(boolean value) {
        return isGroupAttr().value(ConvertUtils.boolToString(value)).add();
    }

    public DictRowAttrBuilder lastChangeAttr() {
        NsiConfigAttr a = dict.getLastChangeAttr();
        if(a == null) {
            throw new NsiServiceException("lastChangeAttr not exists in dict %s", dict.getName());
        }
        return attr(a);
    }

    public DictRowBuilder lastChangeAttr(DateTime value) {
        return lastChangeAttr().value(ConvertUtils.dateTimeToString(value)).add();
    }

    public DictRowAttrBuilder lastUserAttr() {
        NsiConfigAttr a = dict.getLastUserAttr();
        if(a == null) {
            throw new NsiServiceException("lastUserAttr not exists in dict %s", dict.getName());
        }
        return attr(a);
    }

    public DictRowBuilder lastUserAttr(Long value) {
        return lastUserAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowAttrBuilder parentAttr() {
        NsiConfigAttr a = dict.getParentAttr();
        if(a == null) {
            throw new NsiServiceException("parentAttr not exists in dict %s", dict.getName());
        }
        return attr(a);
    }

    public DictRowBuilder parentAttr(Long value) {
        return parentAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowAttrBuilder ownerAttr() {
        NsiConfigAttr a = dict.getOwnerAttr();
        if(a == null) {
            throw new NsiServiceException("ownerAttr not exists in dict %s", dict.getName());
        }
        return attr(a);
    }

    public DictRowBuilder ownerAttr(Long value) {
        return ownerAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowBuilder versionAttr(Long value) {
        return versionAttr().value(ConvertUtils.longToString(value)).add();
    }

    public DictRowBuilder versionAttr(Integer value) {
        return versionAttr().value(ConvertUtils.integerToString(value)).add();
    }

    public DictRowBuilder versionAttr(String value) {
        return versionAttr().value(value).add();
    }

    public DictRowAttrBuilder versionAttr() {
        NsiConfigAttr a = dict.getVersionAttr();
        if(a == null) {
            throw new NsiServiceException("versionAttr not exists in dict %s", dict.getName());
        }
        return attr(a);
    }
    
    public DictRowBuilder attr(String name, Boolean value) {
        return attr(name,1).value(ConvertUtils.boolToString(value)).add();
    }

    private NsiConfigAttr getDictAttr(String name) {
        NsiConfigAttr result = dict.getAttr(name);
        if(result == null) {
            throw new NsiServiceException("attr %s not found in dict %s", name, dict.getName());
        }
        return result;
    }

    public DictRowBuilder attrNull(String name) {
        return attr(name,1).value(createNullList(getDictAttr(name))).add();
    }

    public DictRowBuilder attr(String name, DictRowBuilder valueBuilder) {
        DictRowAttrBuilder builder = attr(name,1).value(valueBuilder.getPrototype().getIdAttr().getValues());
        NsiConfigAttr attr = dict.getAttr(name);
        if(dict.isAttrHasRefAttrs(attr)) {
            List<NsiConfigAttr> refObjectAttrs = valueBuilder.getDict().getRefObjectAttrs();
            if(refObjectAttrs.size() > 0) {
                Map<String, DictRowAttr> refAttrMap = new HashMap<>(refObjectAttrs.size());
                for (NsiConfigAttr refAttr : refObjectAttrs ) {
                    // получаю значение атрибута, из него нужно взять только values
                    DictRowAttr refAttrValue = valueBuilder.getPrototype().getAttr(refAttr.getName());
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
        NsiConfigAttr attr = dict.getAttr(name);
        if(value.getRefAttrs() != null && dict.isAttrHasRefAttrs(attr)) {
            builder.refAttrs(Maps.newHashMap(value.getRefAttrs()));
        }
        return builder.add();
    }

    public DictRowBuilder attr(String name, DictRow data) {
        NsiConfigAttr attr = dict.getAttr(name);
        if(attr.getType() != MetaAttrType.REF) {
            throw new NsiServiceException("attr %s dict %s is not ref", name, dict.getName());
        }
        NsiConfigDict refDict = attr.getRefDict();
        if(data != null) {
            return attr(name, new DictRowBuilder(refDict, data));
        } else {
            return attrNull(name);
        }
    }

    public DictRowBuilder attr(String name, Long value) {
        return attr(name,1).value(ConvertUtils.longToString(value)).add();
    }
    
    public DictRowBuilder attr(String name, Double value) {
        return attr(name,1).value(ConvertUtils.doubleToString(value)).add();
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

    public NsiConfigDict getDict() {
        return dict;
    }

}
