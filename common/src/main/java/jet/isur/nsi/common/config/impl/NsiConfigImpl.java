
package jet.isur.nsi.common.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.model.MetaAttr;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.MetaDict;
import jet.isur.nsi.api.model.MetaField;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.api.model.MetaOwn;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class NsiConfigImpl implements NsiConfig {

    private static final CharMatcher NAME_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT.or(new OneCharMatcher('_'));

    private Map<String,NsiConfigDict> dictMap = new TreeMap<>();

    private final NsiConfigParams params;

    public NsiConfigImpl(NsiConfigParams params) {
        this.params = params;
    }

    public void addDict(MetaDict metaDict) {
        NsiConfigDict dict = new NsiConfigDict(this, metaDict);
        preCheckDict(dict);
        String dictName = metaDict.getName();
        if(dictMap.containsKey(dictName)) {
            throwDictException(dict, "dict already exists");
        }
        // обрабатываем поля
        for (MetaField metaField : metaDict.getFields()) {
            preCheckField(dict, metaField);
            String fieldName = metaField.getName();
            if(dict.getFieldNameMap().containsKey(fieldName)) {
                throwDictException(dict, "field already exists", fieldName);
            }
            dict.addField(new NsiConfigField(metaField));
        }

        // обрабатываем атрибуты
        for (MetaAttr metaAttr : metaDict.getAttrs()) {
            String attrName = metaAttr.getName();
            if(dict.getAttrNameMap().containsKey(attrName)) {
                throwDictException(dict, "attr already exists", attrName);
            }
            dict.addAttr(createAttr(dict, metaAttr));
        }

        // обрабатываем служебные атрибуты
        if(metaDict.getIdAttr() != null) {
            dict.setIdAttr(checkAttrExists(dict, metaDict.getIdAttr()));
            dict.getIdAttr().setReadonly(true);
        }
        if(metaDict.getIsGroupAttr()!=null) {
            dict.setIsGroupAttr(checkAttrExists(dict,metaDict.getIsGroupAttr()));
        }
        if(metaDict.getLastUserAttr()!=null) {
            dict.setLastUserAttr(checkAttrExists(dict,metaDict.getLastUserAttr()));
            dict.getLastUserAttr().setReadonly(true);
        }
        if(metaDict.getLastChangeAttr()!=null) {
            dict.setLastChangeAttr(checkAttrExists(dict,metaDict.getLastChangeAttr()));
            dict.getLastChangeAttr().setReadonly(true);
        }
        if(metaDict.getParentAttr()!=null) {
            dict.setParentAttr(checkAttrExists(dict,metaDict.getParentAttr()));
            dict.getParentAttr().setReadonly(true);
        }
        if(metaDict.getOwnerAttr()!=null) {
            dict.setOwnerAttr(checkAttrExists(dict,metaDict.getOwnerAttr()));
            dict.getOwnerAttr().setReadonly(true);
            dict.getOwnerAttr().setRequired(true);
        }
        if(metaDict.getDeleteMarkAttr()!=null) {
            dict.setDeleteMarkAttr(checkAttrExists(dict,metaDict.getDeleteMarkAttr()));
            dict.getDeleteMarkAttr().setReadonly(true);
        }

        // обрабатываем списки атрибутов
        //dict.getCaptionAttrs().addAll(createFieldList(dict,metaDict.getCaptionAttrs()));
        dict.setRefObjectAttrs(createFieldList(dict,metaDict.getRefObjectAttrs()));
        dict.setLoadDataAttrs(createFieldList(dict, metaDict.getLoadDataAttrs()));
        dict.setTableObjectAttrs(createFieldList(dict,metaDict.getTableObjectAttrs()));
        dict.setInterceptors(createInterceptors(metaDict.getInterceptors()));

        Map<String, NsiConfigAttr> result = new HashMap<String, NsiConfigAttr>();
        if (null != metaDict.getOwns())
            for (Entry<String, MetaOwn> q : metaDict.getOwns().entrySet()) {
                result.put(q.getKey(),
                        checkAttrExists(dict, q.getValue().getAttr()));
            }
        dict.setOwns(result);

        dictMap.put(dictName, dict);
    }

    private NsiConfigAttr checkAttrExists(NsiConfigDict dict, String name) {
        NsiConfigAttr attr = dict.getAttr(name);
        if(attr == null) {
            throwDictException(dict, "attr not exists", name);
        }
        return attr;
    }

    private List<NsiConfigAttr> createFieldList(NsiConfigDict dict, List<String> attrNames) {
        List<NsiConfigAttr> result = new ArrayList<>();
        if(attrNames != null) {
            Set<String> attrNameSet = new TreeSet<>(attrNames);
            if(attrNameSet.size() != attrNames.size()) {
                throwDictException(dict, "attr names dubbed", attrNames.toString());
            }
            for (String attrName : attrNames) {
                result.add(checkAttrExists(dict, attrName));
            }
        }
        return result;
    }

    private List<String> createInterceptors(List<String> interceptors) {
        List<String> result = new ArrayList<>();
        if(interceptors != null) {
            result.addAll(interceptors);
        }
        return result;
    }

    private NsiConfigAttr createAttr(NsiConfigDict dict, MetaAttr metaAttr) {
        if(metaAttr == null) {
            return null;
        }
        NsiConfigAttr attr = new NsiConfigAttr(metaAttr);
        preCheckAttr(dict, attr);
        Set<String> fieldSet = new HashSet<>(metaAttr.getFields());
        if(fieldSet.size() == 0) {
            throwDictException(dict, "fields not set", metaAttr.getName());
        }
        if(fieldSet.size() != metaAttr.getFields().size()) {
            throwDictException(dict, "fields dubbed", metaAttr.getName(), metaAttr.getFields().toString());
        }

        for (String fieldName : metaAttr.getFields()) {
            NsiConfigField field = dict.getField(fieldName);
            if(field == null) {
                throwDictException(dict, "attr refs to unknown field", attr.getName(), fieldName);
            }
            attr.addField(field);
        }
        return attr;
    }

    private void preCheckDict(NsiConfigDict dict) {
        if(!NAME_MATCHER.matchesAllOf(dict.getName())) {
            throwDictException(dict, "invalid name");
        }
    }

    private void preCheckAttr(NsiConfigDict dict, NsiConfigAttr attr) {
        if(attr == null) {
            return;
        }
        if(!NAME_MATCHER.matchesAllOf(attr.getName())) {
            throwDictException(dict, "invalid attr name", attr.getName());
        }
        if(attr.getType()==null) {
            throwDictException(dict, "empty attr type", attr.getName(), attr.getType());
        }
        switch (attr.getType()) {
        case REF:
            if(Strings.isNullOrEmpty(attr.getRefDictName())) {
                throwDictException(dict, "ref dict is empty", attr.getName(), attr.getRefDict());
            }
            break;
        case VALUE:
            break;
        default:
            throwDictException(dict, "invalid attr type", attr.getName(), attr.getType());
        }
        preCheckClobField(dict, attr);
    }

    private void preCheckClobField(NsiConfigDict dict, NsiConfigAttr attr) {
        int fCount = attr.getFields().size();
        if (fCount > 1) {
            for (NsiConfigField field : attr.getFields()) {
                if (field.getType().equals(MetaFieldType.CLOB)) {
                    throwDictException(dict, "Field of Clob type must be one for attribute", attr.getName(), field.getName(), fCount);
                }
            }
        }
    }
    private void throwDictException(NsiConfigDict dict, String message ) {
        throw new NsiConfigException(Joiner.on(": ").join(message,dict.getName()));
    }

    private void throwDictException(NsiConfigDict dict, String message, Object ... args ) {
        throw new NsiConfigException(Joiner.on(": ")
                .join(message,Joiner.on(", ").skipNulls().join(dict.getName(),null,args)));
    }

    private void preCheckField(NsiConfigDict dict, MetaField field) {
        if(!NAME_MATCHER.matchesAllOf(field.getName())) {
            throwDictException(dict, "invalid field name", field.getName());
        }
        if(field.getType()==null) {
            throwDictException(dict, "empty field type", field.getName(), field.getType());
        }
        switch (field.getType()) {
        case BOOLEAN:
            if(field.getSize() == null) {
                throwDictException(dict, "empty field size", field.getName());
            }
            break;
        case DATE_TIME:
            break;
        case NUMBER:
            if(field.getSize() == null) {
                throwDictException(dict, "empty field size", field.getName());
            }
            break;
        case VARCHAR:
        case CHAR:
            if(field.getSize() == null) {
                throwDictException(dict, "empty field size", field.getName());
            }
            if(field.getSize() > 2000) {
                throwDictException(dict, "field size too big", field.getName());
            }
            break;
        case CLOB:
            if(field.getSize() == null) {
                throwDictException(dict, "empty field size", field.getName());
            }
            if (field.getSize() > Integer.MAX_VALUE) {
                throwDictException(dict, "clob field size too big", field.getName());
            }
            break;
        default:
            throwDictException(dict, "invalid field type", field.getName(), field.getType());
        }
        if(field.getSize() != null && field.getSize() <= 0 && field.getType()!=MetaFieldType.DATE_TIME) {
            throwDictException(dict, "invalid field size", field.getName(), field.getSize().toString());
        }
    }


    public void postCheck() {
        postSetMainDict();

        for (NsiConfigDict dict : dictMap.values()) {
            postCheckDict(dict);
        }

        if(params != null && params.getLastUserDict() != null) {
            NsiConfigDict lastUserDict = getDict(params.getLastUserDict());
            if(lastUserDict == null) {
                throw new NsiConfigException("Invalid lastUserDict in params: " + params.getLastUserDict());
            }
            for ( NsiConfigDict dict : dictMap.values()) {
                NsiConfigAttr lastUserAttr = dict.getLastUserAttr();
                if(lastUserAttr != null) {
                    lastUserAttr.setType(MetaAttrType.REF);
                    lastUserAttr.setRefDict(lastUserDict);
                    lastUserAttr.setHidden(false);
                }
            }
        }

    }

    private void postSetMainDict() {
        for ( NsiConfigDict dict : dictMap.values()) {
            if(dict.getMainDictName() != null) {
                NsiConfigDict mainDict = getDict(dict.getMainDictName());
                if(mainDict == null) {
                    throwDictException(dict, "main dict not found", dict.getMainDictName());
                }
                if(mainDict.getTable() == null) {
                    throwDictException(dict, "main dict to be given a table", dict.getMainDictName());
                }
                dict.setMainDict(mainDict);
            }
        }
    }

    @Override
    public NsiConfigDict getDict(String name) {
        return dictMap.get(name);
    }

    private void postCheckDict(NsiConfigDict dict) {
        checkOneFieldAttr(dict, "deleteMarkAttr", dict.getDeleteMarkAttr(), MetaFieldType.BOOLEAN, MetaAttrType.VALUE);
        checkOneFieldAttr(dict, "isGroupAttr", dict.getIsGroupAttr(), MetaFieldType.BOOLEAN, MetaAttrType.VALUE);
        checkOneFieldAttr(dict, "lastChangeAttr", dict.getLastChangeAttr(), MetaFieldType.DATE_TIME, MetaAttrType.VALUE);
        checkOneFieldAttr(dict, "lastUserAttr", dict.getLastUserAttr(), MetaFieldType.NUMBER, null);
        // если задан ownerAttr, то обязательно должен быть задан idAttr
        if (null != dict.getOwnerAttr() && null == dict.getIdAttr()){
            throwDictException(dict, "ownerAttr set, but dict has not idAttr");
        }
        for (NsiConfigAttr attr : dict.getAttrs()) {
            if(attr.getType() == MetaAttrType.REF) {
                attr.setRefDict(getDict(attr.getRefDictName()));
                checkRefAttrFields(dict, attr);
            }
        }
        checkRefAttrFields(dict, dict.getOwnerAttr());
        checkRefAttrFields(dict, dict.getParentAttr());
    }


    private void checkRefAttrFields(NsiConfigDict dict, NsiConfigAttr refAttr) {
        if(refAttr == null) {
            return;
        }

        if(refAttr.getRefDict()==null) {
            throwDictException(dict, "ref dict is empty", refAttr.getName());
        }
        NsiConfigDict idDict = getDict(refAttr.getRefDictName());
        if(idDict == null) {
            throwDictException(dict, "ref attr unknown ref dict", refAttr.getName(), refAttr.getRefDictName());
        }

        NsiConfigAttr idAttr = idDict.getIdAttr();
        // проверяем что сущность на которую ссылается атрибут имеет ид атрибут
        if(idAttr == null) {
            throwDictException(dict, "ref dict must have id attr", idDict.getName());
        }

        // проверяем что для idDict задана таблица или MAIN запрос
        if(idDict.getTable()==null)
            if(idDict.getMainDict() == null || idDict.getSourceQuery(NsiQuery.MAIN_QUERY)==null) {
            throwDictException(dict, "proxy ref dict must have main dict and MAIN query", refAttr.getName());
        }


        if(refAttr.getFields().size() != idAttr.getFields().size()) {
            throwDictException(dict, "ref attr fields count not match id attr fields", refAttr.getName());
        }

        for(int i=0;i<refAttr.getFields().size();i++) {
            NsiConfigField refField = refAttr.getFields().get(i);
            NsiConfigField idField = idAttr.getFields().get(i);
            checkFieldsMatch(dict, refAttr.getName(), refField, idField);
        }
    }

    private void checkFieldsMatch(NsiConfigDict dict, String dictAttr, NsiConfigField refField, NsiConfigField idField) {
        if(!refField.getType().equals(idField.getType())) {
            throwDictException(dict, "field type not match", dictAttr, refField.getName(), idField.getName());
        }
        if(refField.getSize() < idField.getSize()) {
            throwDictException(dict, "field size not match", dictAttr, refField.getName(), idField.getName());
        }
    }

    private void checkOneFieldAttr(NsiConfigDict dict, String dictAttr, NsiConfigAttr attr,
            MetaFieldType type, MetaAttrType attrType ) {
        if(attr == null) {
            return;
        }
        if(attrType != null && attrType != attr.getType()) {
            throwDictException(dict, "attr type not match", dictAttr, attr.getName(), attrType);
        }
        if(attr.getFields().size() > 1) {
            throwDictException(dict, "more than one field", dictAttr, attr.getName());
        }
        NsiConfigField field = attr.getFields().get(0);
        if(field.getType() != type) {
            throwDictException(dict, "invalid field type", dictAttr, attr.getName(), type);
        }

    }

    @Override
    public Collection<NsiConfigDict> getDicts() {
        return dictMap.values();
    }

}