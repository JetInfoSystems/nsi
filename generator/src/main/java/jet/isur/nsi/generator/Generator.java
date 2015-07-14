package jet.isur.nsi.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {

    private static final Logger log = LoggerFactory.getLogger(Generator.class);

    /**
     * конфигурация с метаданными
     */
    private final NsiConfig config;
    /**
     * appender - добавляет данные непосредственно в базу
     */
    private final DBAppender appender;
    /**
     * количество по-умолчанию создаваемых в таблицах строк (если не определен список значений)
     */
    private int defCount = 100;

    private String defString = "test ";

    /**
     * список таблиц, для которых применяется именование в зависимости от привязки к типу
     */
    private List<String> customNaming = Arrays.asList("EVENT", "ORG_OBJ");

    private Map<NsiConfigDict, List<Long>> dictsIds = new HashMap<>();
    private final Map<NsiConfigDict, Integer> dictCount;

    public Generator(NsiConfig config, DBAppender appender) {
        this.config = config;
        this.dictCount = new HashMap<>();
        this.appender = appender;
    }

    /**
     * Устанавливает количество создаваемых записей по-умолчанию
     */
    public void setDefCount(int defCount) {
        this.defCount = defCount;
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
        } else if (dictCount.containsKey(dict)) {
            return dictCount.get(dict);
        } else {
            return defCount;
        }
    }

    public void addData(String dictName){
        NsiConfigDict dict = config.getDict(dictName);
        addData(dictName, getDictCount(dict));
    }

    public void addData(String dictName, int count) {
        dictCount.put(config.getDict(dictName), count);
    }

    public void appendData() {
        DictDependencyGraph dictGraph = getGraph();
        List<NsiConfigDict> dictList = dictGraph.sort();
        for (NsiConfigDict dict : dictList) {
            addData(dict, getDictCount(dict));
        }
    }

    private DictDependencyGraph getGraph() {
        DictDependencyGraph dictGraph = DictDependencyGraph.build(config, dictCount.keySet());
        return dictGraph;
    }

    /**
     * Генерация и добавление данных в справочник
     * @param dict описание справочника
     * @param count количество записей
     */
    private void addData(NsiConfigDict dict, int count){

        List<DictRow> curDataList = appender.getData(dict);

        NsiQuery query = new NsiQuery(config, dict).addAttrs();
        if(curDataList.size() < count) {
            List<DictRow> newDataList = new ArrayList<>(count);
            for (int i=curDataList.size(); i < count; i++) {
                newDataList.add(genDictRow(query, i));
            }
            newDataList = appender.addData(dict, newDataList);
            curDataList.addAll(newDataList);
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
                                String name = row.getAttrs().get("NAME").getFirstValue()+" "+number;
                                drb.attr("DESCRIPTION", name);
                                drb.attr("NAME", name);
                                filledAttrs.add("DESCRIPTION");
                                filledAttrs.add("NAME");
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
                            drb.attr(attrName, defString + number);
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
