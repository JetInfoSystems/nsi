package jet.isur.nsi.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.data.builder.DictRowBuilder;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.generator.dictdata.DictData;
import jet.isur.nsi.generator.dictdata.DictDataContent;
import jet.isur.nsi.generator.dictdata.DictDataObject;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class Generator {

    private static final Logger log = LoggerFactory.getLogger(Generator.class);

    /**
     * конфигурация с метаданными
     */
    private final NsiConfig config;
    
    private final DictDataContent dictdataContent;
    /**
     * appender - добавляет данные непосредственно в базу
     */
    private final DBAppender appender;

    private String defString = "test ";

    /**
     * список таблиц, для которых применяется именование в зависимости от привязки к типу
     */
    private List<String> customNaming = Arrays.asList("EVENT", "ORG_OBJ");

    private Map<NsiConfigDict, List<Long>> dictsIds = new HashMap<>();

    private final GeneratorParams params;

    public Generator(NsiConfig config, DictDataContent dictdataContent, DBAppender appender, GeneratorParams params) {
        this.config = config;
        this.dictdataContent = dictdataContent;
        this.appender = appender;
        this.params = params;
    }

    /**
     * Устанавливает значение по-умолчанию для строковых полей.
     */
    public void setDefString(String defString) {
        this.defString = defString;
    }

    private int getDictCount(NsiConfigDict dict) {
        if (StaticContent.checkPredefinedNames(dict.getName())) {
            return StaticContent.getPredefinedSize(dict.getName());
        } else {
            return params.getDictCount(dict);
        }
    }

    public Map<NsiConfigDict,List<Long>> appendData() {
        DictDependencyGraph dictGraph = getGraph();
        List<NsiConfigDict> dictList = dictGraph.sort();
        
        log.info("Generated Graph ['{}']", getDictListAsString(dictList));
        for (NsiConfigDict dict : dictList) {
            if (dict.getName().startsWith("FIAS")) {
                log.info("Skipping FIAS dicts");
            } else {
                addData(dict, getDictCount(dict));
            }
        }
        return dictsIds;
    }

    private DictDependencyGraph getGraph() {
        List<NsiConfigDict> dicts = new ArrayList<>();
        for (String dictName : params.getDictList()) {
            dicts.add(config.getDict(dictName));
        }
        DictDependencyGraph dictGraph = DictDependencyGraph.build(config, dicts);
        return dictGraph;
    }

    private String getDictListAsString(List<NsiConfigDict> dictList) {
        String listStr = "";
        for (NsiConfigDict dict : dictList) {
            listStr += dict.getName() + ", ";
        }
        return listStr.substring(0, listStr.lastIndexOf(','));
    }
    /**
     * Генерация и добавление данных в справочник
     * @param dict описание справочника
     * @param count количество записей
     */
    private void addData(NsiConfigDict dict, int count){
        
        log.info("addData start for ['{}']", dict.getName());
        
        List<DictRow> curDataList = appender.getData(dict);
        
        NsiQuery query = new NsiQuery(config, dict).addAttrs();

        DictDataObject ddObj = dictdataContent.getDictdataObjsMap().get(dict.getName());
        if (ddObj != null) {
            curDataList.addAll(reloadDictData(query, dict, ddObj, curDataList));
        } else {
            
            if(curDataList.size() < count) {
                List<DictRow> newDataList = new ArrayList<>(count);
                for (int i=curDataList.size(); i < count; i++) {
                    newDataList.add(genDictRow(query, i));
                }
                newDataList = appender.addData(dict, newDataList);
                curDataList.addAll(newDataList);
            }
        }
        DictRowBuilder reader = new DictRowBuilder(query);
        if(!dictsIds.containsKey(dict)) {
            dictsIds.put(dict, new ArrayList<Long>(curDataList.size()));
        }
        List<Long> ids = dictsIds.get(dict);
        for (DictRow dictRow : curDataList) {
            reader.setPrototype(dictRow);
            ids.add(reader.getLongIdAttr());
        }
        dictsIds.put(dict, ids);
    }
    
    private List<DictRow> reloadDictData(NsiQuery query, NsiConfigDict dict, DictDataObject ddObj, List<DictRow> curDataList) {
        List<DictRow> updateDataList = new ArrayList<>();
        Set<Integer> mergedIdxs = updateEqualByRefAttrs(query, dict, ddObj, curDataList, updateDataList);
        
        List<DictRow> newDataList = new ArrayList<>();
        for(int i = 0; i < ddObj.getRowCount(); i ++) {
            if(mergedIdxs.contains(i)) { //Merged not need to add
                continue;
            }
            
            DictRow newRow = genDictdataContentRow(query, ddObj, i, null);
            if (newRow != null) {
                newDataList.add(newRow);
            }
        }
        
//        // TODO: add fill parents
//        Collection<DictRow> allData = new ArrayList<>(updateDataList);
//        allData.addAll(newDataList);
//        
        
        appender.updateData(dict, updateDataList);
        appender.addData(dict, newDataList);
        
        updateDataList.addAll(newDataList);
        return updateDataList;
    }
    
    private Set<Integer> updateEqualByRefAttrs(NsiQuery query, NsiConfigDict dict, DictDataObject ddObj, List<DictRow> curDataList, List<DictRow> updateDataList) {
        Map<String, Collection<String>> ddFields = ddObj.getFields();
        
        List<NsiConfigAttr> refDictAttrs = dict.getRefObjectAttrs();
        List<String> refDictAttrNames = new ArrayList<>();
        for (NsiConfigAttr refDictAttr : refDictAttrs) {
            refDictAttrNames.add(refDictAttr.getName());
        }
        Set<String> ks = new HashSet<>(ddFields.keySet());
        ks.retainAll(refDictAttrNames);
        
        Set<Integer> mergedIndexes = new HashSet<>();
        for (DictRow cdr : curDataList) {
            Map<String, Integer> eqValueIdxs = new HashMap<>();
            for (String key: ks) {
                int idx = 0;
                for (String val : ddFields.get(key)) {
                   if(val.equals(cdr.getAttrs().get(key).getValues().get(0))){
                       eqValueIdxs.put(key, idx);
                   }
                   idx++;
                }
            }
            Set<Integer> s = new HashSet<>(eqValueIdxs.values());
            if(s.size() == 1) {
                int idx = s.iterator().next();
                DictRow toUdate = genDictdataContentRow(query, ddObj, idx, cdr);
                if ( toUdate != null) {
                    updateDataList.add(toUdate);
                    mergedIndexes.add(idx);
                }
            }
        }
        curDataList.removeAll(updateDataList);
        return mergedIndexes;
    }

    /**
     * Создание заполненной строки для фиксированных справочных данных
     * Ссылка допускается только на этот же справочник (иерархия)
     * @param query
     * @return
     */
    private DictRow genDictdataContentRow(NsiQuery query, DictDataObject ddObj, int idx, DictRow old) {
        DictRowBuilder drb = new DictRowBuilder(query);
        Map<String, Collection<String>> ddFields = ddObj.getFields();
        Set<String> ddFieldNames = ddFields.keySet();

        for(NsiQueryAttr attr:query.getAttrs()) {
            NsiConfigAttr attrConfig = attr.getAttr();
            String attrName = attrConfig.getName();
            
            if (attrConfig.getType() == MetaAttrType.REF) {
                // Only PARENT ref could be skipped 
                // if(!attrConfig.getRefDictName().equals(ddObj.getDictName())) {
                log.debug("parent ref skipped, will be filled later ['{}']", ddObj.getDictName());
                Long id = null;
                drb.attr(attrName, id);
                continue;
            }
            List<String> values = new ArrayList<>();
            String value = null;
            if (ddFields.get(attrName) != null) {
                values.addAll(ddFields.get(attrName));
                value = values.get(idx);
            }
            
            List<NsiConfigField> fields = attr.getAttr().getFields();
            NsiConfigField field = fields.get(0);
            MetaFieldType type = field.getType();
            if (field.getEnumValues() != null) {
                if (ddFieldNames.contains(attrName)) {
                    Set<String> enums = new HashSet<>(field.getEnumValues().keySet());
                    if (enums.contains(value)) {
                        drb.attr(attrName, value);
                    } else {
                        log.error("Bad value for enum attr ['{}','{}']", ddObj.getDictName(), attrName);
                        return null;
                    }
                } else if (old != null) {
                    drb.attr(attrName, old.getAttrs().get(attrName).getValues().get(0));
                } else {
                    List<String> enums = new ArrayList<>(field.getEnumValues().keySet());
                    int index = RandomUtils.getInt(enums.size());
                    drb.attr(attrName, enums.get(index));
                }
            } else {
                switch (type) {
                case BOOLEAN:
                    if (ddFieldNames.contains(attrName)){
                        if (value != null && value.equals("Y")) {
                            drb.attr(attrName, Boolean.TRUE);
                        } else {
                            drb.attr(attrName, Boolean.FALSE);
                        }
                    } else if (old != null) {
                        drb.attr(attrName, old.getAttrs().get(attrName).getValues().get(0));
                    } else {
                        if ("IS_DELETED".equals(field.getName())) {
                            drb.attr(attrName, Boolean.FALSE);
                        } else {
                            drb.attr(attrName, RandomUtils.getBoolean());
                        }
                    }
                    break;
                case DATE_TIME:
                    if (old != null) {
                        drb.attr(attrName, old.getAttrs().get(attrName).getValues().get(0));
                    } else {
                        drb.attr(attrName, RandomUtils.getDateTime());
                    }
                    break;
                case NUMBER:
                    if (ddFieldNames.contains(attrName)){
                        drb.attr(attrName, value);
                    } else if (old != null) {
                        drb.attr(attrName, old.getAttrs().get(attrName).getValues().get(0));
                    } else {
                        drb.attr(attrName, RandomUtils.getInt(100) * 1L);
                    }
                    break;
                case CHAR:
                case VARCHAR:
                    if (ddFieldNames.contains(attrName)){
                        drb.attr(attrName, value);
                    } else if (old != null) {
                        drb.attr(attrName, old.getAttrs().get(attrName).getValues().get(0));
                    } else {
                        
                        String fieldName = query.getDict().getName()+"."+field.getName();
                        String val = StaticContent.getString(fieldName, idx);
                        if (val == null) {
                            val = DynamicContent.getString(fieldName);
                        }
                        if (val == null) {
                            Joiner joiner = Joiner.on(" ").skipNulls();
                            drb.attr(attrName, joiner.join(attr.getAttr().getCaption(), val));
                        } else {
                            drb.attr(attrName, val);
                        }
                    }
                    break;
                }
            }
        }
        drb
            .deleteMarkAttr(false)
            .idAttr(null)
            .lastUserAttr(null)
            .lastChangeAttr(new DateTime().withMillisOfSecond(0));
        return drb.build();
    }
    
    private void addParents(NsiQuery query, NsiConfigDict dict, DictDataObject ddObj, List<DictRow> updateList, List<DictRow> newList){
        Collection<String> ddParentNames = ddObj.getFields().get("parent");
        
        
    }

    /**
     * Создание заполненной строки
     */
    private DictRow genDictRow(NsiQuery query, int number) {
        DictRowBuilder drb = new DictRowBuilder(query);

        for(NsiQueryAttr attr:query.getAttrs()) {
            NsiConfigAttr attrConfig = attr.getAttr();
            String attrName = attrConfig.getName();

            // если атрибут - ссылка, то заполняем из ранее созданного
            if (attrConfig.getType() == MetaAttrType.REF) {
                NsiConfigDict refDict = attrConfig.getRefDict();
                List<Long> ids = dictsIds.get(refDict);
                if(ids != null) {
                    if (ids.size() > 0) {
                        Long id = ids.get(number % ids.size());
                        drb.attr(attrName, id);
                        /*
                        if (refDict.getName().endsWith("_TYPE")) {
                            if (customNaming.contains(dictName)) {
                                String name = row.getAttrs().get(dictName + "_NAME").getFirstValue()+" "+number;
                                drb.attr("DESCRIPTION", name);
                                drb.attr(dictName + "_NAME", name);
                                filledAttrs.add("DESCRIPTION");
                                filledAttrs.add(dictName + "_NAME");
                            }
                        }
                        */
                    } else {
                        drb.attr(attrName, (Long) null);
                    }
                } else {
                    drb.attr(attrName, (Long) null);
                }
            } else {
                /*
                 * если атрибут - значение, заполняем атрибут в зависимости от типа
                 */
                List<NsiConfigField> fields = attr.getAttr().getFields();
                NsiConfigField field = fields.get(0);
                MetaFieldType type = field.getType();
                if (field.getEnumValues() != null) {
                    List<String> enums = new ArrayList<>(field.getEnumValues().keySet());
                    int index = RandomUtils.getInt(enums.size());
                    drb.attr(attrName, enums.get(index));
                } else {
                    switch (type) {
                    case BOOLEAN:
                        if ("IS_DELETED".equals(field.getName())) {
                            drb.attr(attrName, Boolean.FALSE);
                        } else {
                            drb.attr(attrName, RandomUtils.getBoolean());
                        }
                        break;
                    case DATE_TIME:
                        drb.attr(attrName, RandomUtils.getDateTime());
                        break;
                    case NUMBER:
                        drb.attr(attrName, RandomUtils.getInt(100) * 1L);
                        break;
                    case CHAR:
                    case VARCHAR:
                        String fieldName = query.getDict().getName()+"."+field.getName();
                        String value = StaticContent.getString(fieldName, number);
                        if (value == null) {
                            value = DynamicContent.getString(fieldName);
                        }
                        if (value == null) {
                            Joiner joiner = Joiner.on(" ").skipNulls();
                            String str = joiner.join(attr.getAttr().getCaption(), number);
                            if(field.getSize() < str.length()) {
                                drb.attr(attrName, String.valueOf(number));
                            } else {
                                drb.attr(attrName, String.valueOf(str));
                            }
                        } else {
                            drb.attr(attrName, value);
                        }
                        break;
                    }
                }
            }
        }
        drb
            .deleteMarkAttr(false)
            .idAttr(null)
            .lastUserAttr(null)
            .lastChangeAttr(new DateTime().withMillisOfSecond(0));
        return drb.build();
    }

    public void cleanData() {
        DictDependencyGraph graph = getGraph();
        List<NsiConfigDict> dictList = graph.sort();
        Collections.reverse(dictList);
        for (NsiConfigDict dict : dictList) {
            appender.cleanData(dict);
        }
    }

}
