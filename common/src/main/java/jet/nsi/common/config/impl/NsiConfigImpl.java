
package jet.nsi.common.config.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiConfigParams;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.model.MetaAttr;
import jet.nsi.api.model.MetaAttrType;
import jet.nsi.api.model.MetaDict;
import jet.nsi.api.model.MetaField;
import jet.nsi.api.model.MetaFieldType;
import jet.nsi.api.model.MetaOwn;

public class NsiConfigImpl implements NsiConfig {

    private static final CharMatcher NAME_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT.or(new OneCharMatcher('_'));

    /**
     * Хранение преобразованных метаданных справочников, для построения запросов
     * */
    private Map<String, NsiConfigDict> dictMap = new ConcurrentSkipListMap<>();
    
    /**
    * Хранение метаданных справочников для редактирования метаданных
    * */
    private Map<String, MetaDict> metaDictMap = new ConcurrentSkipListMap<>();
    /**
     * Относительные пути хранения метаданных справочников
     * */
    private Map<String, Path> metaDictPaths = new ConcurrentSkipListMap<>();
    

    private final NsiConfigParams params;

    public NsiConfigImpl(NsiConfigParams params) {
        this.params = params;
    }

    @Override
    public NsiConfigDict getDict(String name) {
        return dictMap.get(name);
    }

    @Override
    public MetaDict getMetaDict(String name) {
        return metaDictMap.get(name);
    }

    @Override
    public Path getMetaDictPath(String name) {
        return metaDictPaths.get(name);
    }

    @Override
    public Collection<NsiConfigDict> getDicts() {
        return dictMap.values();
    }
    @Override
    public Collection<NsiConfigDict> getDicts(Set<String> labels) {
        Preconditions.checkNotNull(labels, "labels must be not null");
        
        Set<NsiConfigDict> result = new HashSet<>();
        for(NsiConfigDict dict : dictMap.values()) {
            for (String label : labels) {
                if (dict.getLabels().contains(label)) {
                    result.add(dict);
                }
            }
        }
        return result;
    }

    @Override
    public Collection<MetaDict> getMetaDicts() {
        return metaDictMap.values();
    }

    @Override
    public Collection<MetaDict> getMetaDicts(Set<String> labels) {
        Preconditions.checkNotNull(labels, "labels must be not null");
        
        Set<MetaDict> result = new HashSet<>();
        for(MetaDict dict : metaDictMap.values()) {
            for (String label : labels) {
                if (dict.getLabels().contains(label)) {
                    result.add(dict);
                }
            }
        }
        return result;
    }

    public void savePath(String dictName, Path filePath) {
        metaDictPaths.put(dictName, filePath);
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
            addDictField(dict, metaField);
        }

        // обрабатываем атрибуты
        for (MetaAttr metaAttr : metaDict.getAttrs()) {
            addDictAttr(dict, metaAttr);
        }

        // обрабатываем служебные атрибуты
        if(metaDict.isAutoIdAttr()) {
            MetaField metaField = createAutoIdField();
            addDictField(dict, metaField);
            MetaAttr metaAttr = createAutoIdAttr(metaField);
            dict.setIdAttr(addDictAttr(dict, metaAttr));
        } else if(metaDict.getIdAttr() != null) {
            dict.setIdAttr(checkAttrExists(dict, metaDict.getIdAttr()));
            dict.getIdAttr().setReadonly(true);
        }
        if(metaDict.isAutoIsGroupAttr()) {
            MetaField metaField = createAutoIsGroupField();
            addDictField(dict, metaField);
            MetaAttr metaAttr = createAutoIsGroupAttr(metaField);
            dict.setIsGroupAttr(addDictAttr(dict, metaAttr));
        } else if(metaDict.getIsGroupAttr()!=null) {
            dict.setIsGroupAttr(checkAttrExists(dict,metaDict.getIsGroupAttr()));
        }
        if(metaDict.isAutoLastUserAttr()) {
            MetaField metaField = createAutoLastUserField();
            addDictField(dict, metaField);
            MetaAttr metaAttr = createAutoLastUserAttr(metaField);
            dict.setLastUserAttr(addDictAttr(dict, metaAttr));
        } else if(metaDict.getLastUserAttr()!=null) {
            dict.setLastUserAttr(checkAttrExists(dict,metaDict.getLastUserAttr()));
            dict.getLastUserAttr().setReadonly(true);
        }
        if(metaDict.isAutoLastChangeAttr()) {
            MetaField metaField = createAutoLastChangeField();
            addDictField(dict, metaField);
            MetaAttr metaAttr = createAutoLastChangeAttr(metaField);
            dict.setLastChangeAttr(addDictAttr(dict, metaAttr));
        } else if(metaDict.getLastChangeAttr()!=null) {
            dict.setLastChangeAttr(checkAttrExists(dict,metaDict.getLastChangeAttr()));
            dict.getLastChangeAttr().setReadonly(true);
        }
        if(metaDict.isAutoParentAttr()) {
            MetaField metaField = createAutoParentField();
            addDictField(dict, metaField);
            MetaAttr metaAttr = createAutoParentAttr(metaDict, metaField);
            dict.setParentAttr(addDictAttr(dict, metaAttr));
        } else if(metaDict.getParentAttr()!=null) {
            dict.setParentAttr(checkAttrExists(dict,metaDict.getParentAttr()));
            dict.getParentAttr().setReadonly(true);
        }
        if(metaDict.getOwnerAttr()!=null) {
            dict.setOwnerAttr(checkAttrExists(dict,metaDict.getOwnerAttr()));
            dict.getOwnerAttr().setReadonly(true);
            dict.getOwnerAttr().setRequired(true);
        }
        if(metaDict.isAutoDeleteMarkAttr()) {
            MetaField metaField = createAutoDeleteMarkField();
            addDictField(dict, metaField);
            MetaAttr metaAttr = createAutoDeleteMarkAttr(metaField);
            dict.setDeleteMarkAttr(addDictAttr(dict, metaAttr));
        } else if(metaDict.getDeleteMarkAttr()!=null) {
            dict.setDeleteMarkAttr(checkAttrExists(dict,metaDict.getDeleteMarkAttr()));
            dict.getDeleteMarkAttr().setReadonly(true);
        }
        if(metaDict.getUniqueAttr() != null) {
            dict.setUniqueAttr(checkAttrExists(dict, metaDict.getUniqueAttr()));
            for(NsiConfigAttr cAttr : dict.getUniqueAttr()) {
                cAttr.setRequired(true);
            }
        }
        if(metaDict.isAutoVersion() && dict.getTable() != null) {
            MetaField metaField = createAutoVersionField();
            addDictField(dict, metaField);
            MetaAttr metaAttr = createAutoVersionAttr(metaField);
            dict.setVersionAttr(addDictAttr(dict, metaAttr));
        } else if(metaDict.getVersionAttr() != null) {
            dict.setVersionAttr(checkAttrExists(dict, metaDict.getVersionAttr()));
            dict.getVersionAttr().setRequired(false);
            dict.getVersionAttr().setReadonly(true);
        }
        
        // обрабатываем списки атрибутов
        //dict.getCaptionAttrs().addAll(createFieldList(dict,metaDict.getCaptionAttrs()));
        dict.setRefObjectAttrs(createAttrList(dict,metaDict.getRefObjectAttrs()));
        dict.setLoadDataAttrs(createAttrList(dict, metaDict.getLoadDataAttrs()));
        dict.setTableObjectAttrs(createAttrList(dict,metaDict.getTableObjectAttrs()));
        dict.setInterceptors(createInterceptors(metaDict.getInterceptors()));
        dict.setLabels(createLabels(metaDict.getLabels()));
        dict.setMergeExternalAttrs(createAttrList(dict,metaDict.getMergeExternalAttrs()));
        
        Map<String, NsiConfigAttr> result = new HashMap<String, NsiConfigAttr>();
        if (null != metaDict.getOwns())
            for (Entry<String, MetaOwn> q : metaDict.getOwns().entrySet()) {
                result.put(q.getKey(),
                        checkAttrExists(dict, q.getValue().getAttr()));
            }
        dict.setOwns(result);

        dictMap.put(dictName, dict);
        metaDictMap.put(dictName, metaDict);
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

    public void removeDict(String dictName) {
        dictMap.remove(dictName);
        metaDictMap.remove(dictName);
        metaDictPaths.remove(dictName);
    }

    public void updateDict(MetaDict metaDict) {
        if (dictMap.containsKey(metaDict.getName())) {
            //add dict does many checks, so it is easy way
            removeDict(metaDict.getName());
        }
        addDict(metaDict);
    }

    private NsiConfigAttr addDictAttr(NsiConfigDict dict, MetaAttr metaAttr) {
        String attrName = metaAttr.getName();
        if(dict.getAttrNameMap().containsKey(attrName)) {
            throwDictException(dict, "attr already exists", attrName);
        }
        NsiConfigAttr result = createAttr(dict, metaAttr);
        dict.addAttr(result);
        return result;
    }

    private MetaAttr createAutoVersionAttr(MetaField metaField) {
        MetaAttr result = new MetaAttr();
        result.setName(params.getDefaultVersionName());
        result.setCaption("Версия");
        result.setType(MetaAttrType.VALUE);
        result.setFields(Arrays.asList(metaField.getName()));
        result.setReadonly(true);
        return result;
    }

    private MetaAttr createAutoDeleteMarkAttr(MetaField metaField) {
        MetaAttr result = new MetaAttr();
        result.setName(params.getDefaultDeleteMarkName());
        result.setCaption("Удален");
        result.setType(MetaAttrType.VALUE);
        result.setFields(Arrays.asList(metaField.getName()));
        result.setReadonly(true);
        return result;
    }

    private MetaAttr createAutoIdAttr(MetaField metaField) {
        MetaAttr result = new MetaAttr();
        result.setName(params.getDefaultIdName());
        result.setCaption("Идентификатор");
        result.setType(MetaAttrType.VALUE);
        result.setFields(Arrays.asList(metaField.getName()));
        //result.setReadonly(true);
        return result;
    }

    private MetaAttr createAutoIsGroupAttr(MetaField metaField) {
        MetaAttr result = new MetaAttr();
        result.setName(params.getDefaultIsGroupName());
        result.setCaption("Группа");
        result.setType(MetaAttrType.VALUE);
        result.setFields(Arrays.asList(metaField.getName()));
        return result;
    }

    private MetaAttr createAutoParentAttr(MetaDict dict, MetaField metaField) {
        MetaAttr result = new MetaAttr();
        result.setName(params.getDefaultParentName());
        result.setCaption("Родитель");
        result.setType(MetaAttrType.REF);
        result.setRefDict(dict.getName());
        result.setFields(Arrays.asList(metaField.getName()));
        result.setHidden(true);
        return result;
    }

    private MetaAttr createAutoLastChangeAttr(MetaField metaField) {
        MetaAttr result = new MetaAttr();
        result.setName(params.getDefaultLastChangeName());
        result.setCaption("Дата, время последнего изменения");
        result.setType(MetaAttrType.VALUE);
        result.setFields(Arrays.asList(metaField.getName()));
        result.setReadonly(true);
        return result;
    }

    private MetaAttr createAutoLastUserAttr(MetaField metaField) {
        MetaAttr result = new MetaAttr();
        result.setName(params.getDefaultLastUserName());
        result.setCaption("Автор последнего изменения");
        result.setType(MetaAttrType.VALUE);
        result.setFields(Arrays.asList(metaField.getName()));
        result.setReadonly(true);
        return result;
    }

    private void addDictField(NsiConfigDict dict, MetaField metaField) {
        preCheckField(dict, metaField);
        checkFieldExists(dict, metaField);
        dict.addField(new NsiConfigField(metaField));
    }

    private void checkFieldExists(NsiConfigDict dict, MetaField metaField) {
        String fieldName = metaField.getName();
        if(dict.getFieldNameMap().containsKey(fieldName)) {
            throwDictException(dict, "field already exists", fieldName);
        }
    }

    private MetaField createAutoVersionField() {
        MetaField result = new MetaField();
        result.setName(params.getDefaultVersionName());
        result.setSize(params.getDefaultVersionSize());
        result.setType(params.getDefaultVersionType());
        return result;
    }

    private MetaField createAutoDeleteMarkField() {
        MetaField result = new MetaField();
        result.setName(params.getDefaultDeleteMarkName());
        result.setSize(params.getDefaultDeleteMarkSize());
        result.setType(params.getDefaultDeleteMarkType());
        return result;
    }

    private MetaField createAutoIdField() {
        MetaField result = new MetaField();
        result.setName(params.getDefaultIdName());
        result.setSize(params.getDefaultIdSize());
        result.setType(params.getDefaultIdType());
        return result;
    }

    private MetaField createAutoIsGroupField() {
        MetaField result = new MetaField();
        result.setName(params.getDefaultIsGroupName());
        result.setSize(params.getDefaultIsGroupSize());
        result.setType(params.getDefaultIsGroupType());
        return result;
    }

    private MetaField createAutoParentField() {
        MetaField result = new MetaField();
        result.setName(params.getDefaultParentName());
        result.setSize(params.getDefaultIdSize());
        result.setType(params.getDefaultIdType());
        return result;
    }

    private MetaField createAutoLastChangeField() {
        MetaField result = new MetaField();
        result.setName(params.getDefaultLastChangeName());
        result.setSize(params.getDefaultLastChangeSize());
        result.setType(params.getDefaultLastChangeType());
        return result;
    }

    private MetaField createAutoLastUserField() {
        MetaField result = new MetaField();
        result.setName(params.getDefaultLastUserName());
        result.setSize(params.getDefaultLastUserSize());
        result.setType(params.getDefaultLastUserType());
        return result;
    }

    private NsiConfigAttr checkAttrExists(NsiConfigDict dict, String name) {
        NsiConfigAttr attr = dict.getAttr(name);
        if(attr == null) {
            throwDictException(dict, "attr not exists", name);
        }
        return attr;
    }

    private List<NsiConfigAttr> checkAttrExists(NsiConfigDict dict, List<String> names) {
        List<NsiConfigAttr> res = new ArrayList<>();
        for(String name : names) {
            NsiConfigAttr attr = dict.getAttr(name);
            if(attr == null) {
                throwDictException(dict, "attr not exists", name);
            }
            res.add(attr);
        }
        return res;
    }
    
    private List<NsiConfigAttr> createAttrList(NsiConfigDict dict, List<String> attrNames) {
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
    
    private Set<String> createLabels(List<String> labels) {
       Set<String> result = new TreeSet<>();
        if(labels != null) {
            result.addAll(labels);
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

    private void throwDictException(String dictName, String message ) {
        throw new NsiConfigException(Joiner.on(": ").join(message,dictName));
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
        
        // автоматически добавляем versionAttr для прокси моделей если mainDict имеет versionAttr
        if(dict.getVersionAttr() == null && dict.getMainDict() != null && dict.getMainDict().getVersionAttr() != null) {
            NsiConfigField mainVersionField = dict.getMainDict().getVersionAttr().getFields().get(0);
            MetaField metaField = new MetaField();
            metaField.setName(mainVersionField.getName());
            metaField.setSize(mainVersionField.getSize());
            metaField.setType(mainVersionField.getType());
            addDictField(dict, metaField);
            MetaAttr metaAttr = createAutoVersionAttr(metaField);
            dict.setVersionAttr(addDictAttr(dict, metaAttr));
        }
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
}