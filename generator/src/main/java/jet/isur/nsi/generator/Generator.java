package jet.isur.nsi.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class Generator {

    Logger log = LoggerFactory.getLogger(Generator.class);

    /**
     * конфигурация с метаданными
     */
    private NsiConfig config;
    /**
     * appender - добавляет данные непосредственно в базу
     */
    private DBAppender appender;
    /**
     * количество по-умолчанию создаваемых в таблицах строк (если не определен список значений)
     */
    private int defCount = 100;

    private String defString = "test ";

    /**
     * карта для задания количество создаваемых записей в конкретных таблицах
     */
    private Map<String, Integer> customCount = new HashMap<String, Integer>();

    /**
     * добавленные в базу данные с помощью данного экземпляра генератора 
     */
    private Map<String, List<DictRow>> addedData= new HashMap<String, List<DictRow>>();

    /**
     * список таблиц, для которых применяется именование в зависимости от привязки к типу
     */
    private List<String> customNaming = Arrays.asList("EVENT", "ORG_OBJ");

    /**
     * список таблиц для максимального заполнения базы
     */
    public List<String> fillDatabaseTables = new ArrayList<>(Arrays.asList("ORG_OBJ","EVENT_PARAM","MSG_INSTRUCTION_ORG","MSG_EMP"));

    /**
     * сопоставляем имена таблиц со списком имен таблиц, которые на неё ссылаются
     */
    private Map<String, List<String>> inLinksMap;
    /**
     * сопоставляем имена таблиц со списком имен таблиц, на которые она ссылается
     */
    private Map<String, List<String>> outLinksMap;


    public Generator(DataSource dataSource, NsiConfig config) {
        this.config = config;
        appender = new DBAppender(dataSource, config);
        prepareLinksMaps();
    }

    /**
     * Устанавливает количество создаваемых записей по-умолчанию
     */
    public void setDefCount(int defCount) {
        this.defCount = defCount;
    }

    /**
     * добавляет количество создаваемых записей для конкретной таблицы
     */
    public void setCustomCount(String tableName, int count) {
        this.customCount.put(tableName, count);
    }

    /**
     * Устанавливает значение по-умолчанию для строковых полей.
     */
    public void setDefЫекштп(String defString) {
        this.defString = defString;
    }

    /**
     * Генерация и добавление данных в справочник
     * @param dict описание справочника
     */
    public void addData(String dictName){
        //System.out.println("!!! addData "+dictName);
        if (StaticContent.checkPredefinedNames(dictName)) {
            addData(dictName, StaticContent.getPredefinedSize(dictName));
        } else if (customCount.containsKey(dictName)) {
            addData(dictName, customCount.get(dictName));
        } else {
            addData(dictName, defCount);
        }
    }

    /**
     * Генерация и добавление данных в справочник
     * @param dict описание справочника
     * @param count количество записей
     */
    private void addData(String dictName, int count){
        //System.out.println("!!! addData "+dictName+"  "+count);

        List<DictRow> dataList = new ArrayList<>(count);

        NsiConfigDict configDict = config.getDict(dictName);
        NsiQuery query = new NsiQuery(config, configDict).addAttrs();

        if (outLinksMap.get(dictName) != null) {
            for (String link : outLinksMap.get(dictName)) {
                if ("EMP_PHONE".equals(link)) {
                    // проблема с перекрестным внешним ключамем на EMP
                    continue;
                }
                if (addedData.get(link) == null) {
                    addedData.put(link, new ArrayList<DictRow>());
                    addData(link);
                }
            }
        }

        DictRow row = null;
        for (int i=0; i<count; i++) {
            row = getDictRowBuilder(query, i).build();
            dataList.add(row);
        }

        dataList = appender.addData(dictName, dataList);
        List<DictRow> added = addedData.get(dictName);
        if (added == null ) {
            addedData.put(dictName, dataList);
        } else {
            added.addAll(dataList);
        }
    }

    /**
     * Создание заполненной строки
     */
    private DictRowBuilder getDictRowBuilder(NsiQuery query, int number) {
        DictRowBuilder drb = new DictRowBuilder(query);

        String dictName = query.getDict().getName();
        List<String> filledAttrs = new ArrayList<String>();
        for(NsiQueryAttr attr:query.getAttrs()) {
            NsiConfigAttr attrConfig = attr.getAttr();
            String attrName = attrConfig.getName();

            /*
             * если атрибут заполнен, пропускаем
             */
            if (filledAttrs.contains(attrName)) {
                continue;
            }

            /*
             * если атрибут - ссылка, то заполняем из ранее созданного
             */
            if (attrConfig.getType() == MetaAttrType.REF) {
                String refDict = attrConfig.getRefDictName();
                List<DictRow> added = addedData.get(refDict);
                if (added != null) {
                    if (added.size() > 0) {
                        int addedIndex = number % added.size();
                        DictRow row = added.get(addedIndex);
                        drb.attr(attrName, row.getAttrs().get("ID").getFirstValue());

                        if (refDict.endsWith("_TYPE")) {
                            if (customNaming.contains(dictName)) {
                                String name = row.getAttrs().get("NAME").getFirstValue()+" "+number;
                                drb.attr("DESCRIPTION", name);
                                drb.attr("NAME", name);
                                filledAttrs.add("DESCRIPTION");
                                filledAttrs.add("NAME");
                            }
                        }
                    } else {
                        drb.attr(attrName, (Long) null);
                    }
                } else {
                    drb.attr(attrName, (Long)null);
                }
            } else {
                /*
                 * если атрибут - значение, заполняем атрибут в зависимости от типа
                 */
                List<NsiConfigField> fields = attr.getAttr().getFields();
                NsiConfigField field = fields.get(0);
                MetaFieldType type = field.getType();
                if (field.getEnumValues() != null) {
                    List<String> enums = new ArrayList<>(field.getEnumValues()
                            .keySet());
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
        drb.deleteMarkAttr(false).idAttr(null).lastUserAttr(null)
            .lastChangeAttr(new DateTime().withMillisOfSecond(0));
        return drb;
    }

    private void prepareLinksMaps() {
        if (inLinksMap != null && outLinksMap != null) {
            return;
        }
        inLinksMap = new TreeMap<>();
        outLinksMap = new TreeMap<>();
        Collection<NsiConfigDict> dicts = config.getDicts();
        for (NsiConfigDict dict : dicts) {
            inLinksMap.put(dict.getName(), new ArrayList<String>());
            outLinksMap.put(dict.getName(), new ArrayList<String>());
        }
        inLinksMap.put("NOT FOUND", new ArrayList<String>());
        for (NsiConfigDict dict : dicts) {
            String dictName = dict.getName();
            List<NsiConfigField> fields = dict.getFields();
            List<String> outList = outLinksMap.get(dictName);
            for (NsiConfigField field : fields) {
                String name = field.getName();
                if (name.endsWith("_ID")) {
                    String table = name.substring(0, name.length()-3);
                    List<String> list = inLinksMap.get(table);
                    if (list != null) {
                        list.add(dictName);
                        outList.add(table);
                    }

                }
            }
        }
    }


    //private static List<String> cleaned = new ArrayList<>();
    public void cleanData(String dictName){
        cleanData(dictName, new ArrayList<String>());
    }

    private void cleanData(String dictName, List<String> cleaned){

        boolean success = appender.cleanData(dictName);
        cleaned.add(dictName);

        if (outLinksMap.get(dictName) != null) {
            for (String link : outLinksMap.get(dictName)) {
                if (!cleaned.contains(link)) {
                    cleanData(link, cleaned);
                }
            }
        }

        if (!success) {
            appender.cleanData(dictName);
        }
    }


    public void addFillDatabaseTables(String tableName) {
        if (config.getDict(tableName) != null && !fillDatabaseTables.contains(tableName)) {
            fillDatabaseTables.add(tableName);
        }
    }

    public void fillDatabase() {
        for (String tableName:fillDatabaseTables) {
            addData(tableName);
        }
    }

    public void cleanDatabase() {
        for (String tableName:fillDatabaseTables) {
            cleanData(tableName);
        }
    }
}
