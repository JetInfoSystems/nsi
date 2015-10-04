package jet.isur.nsi.generator.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jet.isur.nsi.api.data.DictRow;
import jet.isur.nsi.api.data.DictRowBuilder;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.api.data.NsiQuery;
import jet.isur.nsi.api.data.NsiQueryAttr;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.MetaFieldType;
import jet.isur.nsi.generator.DynamicContent;
import jet.isur.nsi.generator.StaticContent;
import jet.isur.nsi.generator.dictdata.DictDataObject;
import jet.isur.nsi.generator.plugin.PluginDataObject;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class GeneratorDictRowHelper {

    private static final Logger log = LoggerFactory.getLogger(GeneratorDictRowHelper.class);

    /**
     * Создание заполненной строки для фиксированных справочных данных
     * Ссылка допускается только на этот же справочник (иерархия)
     * @param query
     * @return
     */
    public DictRow genDictdataContentRow(NsiQuery query, DictDataObject ddObj, int idx, DictRow old) {
        DictRowBuilder drb = query.getDict().builder();
        Map<String, Collection<String>> ddFields = ddObj.getFields();
        Set<String> ddFieldNames = ddFields.keySet();

        for(NsiQueryAttr attr:query.getAttrs()) {
            NsiConfigAttr attrConfig = attr.getAttr();
            String attrName = attrConfig.getName();

            if (attrConfig.getType() == MetaAttrType.REF) {
                // Only PARENT ref could be skipped
                // if(!attrConfig.getRefDictName().equals(ddObj.getDictName()))
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

            List<NsiConfigField> fields = attrConfig.getFields();
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
                        if (!"IS_DELETED".equals(field.getName())) {
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
        if(query.getDict().getDeleteMarkAttr() != null) {
            drb.deleteMarkAttr(false);
        }

        addCommonAttrIfExist(drb, query.getDict());
        return drb.build();
    }

    /**
     * Создание заполненной строки
     */
    public DictRow genDictRow(NsiQuery query,  int number, Map<NsiConfigDict, List<Long>> dictsIds) {
        DictRowBuilder drb = query.getDict().builder();

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
                        if (!"IS_DELETED".equals(field.getName())) {
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
        addCommonAttrIfExist(drb, query.getDict());
        return drb.build();
    }

    public DictRow genDictRow(NsiQuery query, PluginDataObject obj, Map<String, Map<String, Long>> referencesData, int idx) {
        DictRowBuilder drb = query.getDict().builder();

        Map<String, Collection<String>> ddFields = obj.getFields();

        for (String fieldName : ddFields.keySet()) {
            String attrName = fieldName;
            if (fieldName.startsWith(PluginDataObject.REF_PREFIX)) {
                attrName = obj.getRefMap().get(fieldName).getDictName() + "_ID";
            }

            List<String> values = new ArrayList<>();
            String value = null;
            if (ddFields.get(fieldName) != null) {
                values.addAll(ddFields.get(fieldName));
                value = values.get(idx);
            }
            NsiConfigAttr attrConfig = query.getAttr(attrName).getAttr();
            if (attrConfig.getType() == MetaAttrType.REF) {
                Long refId = null;
                if (value != null) {
                    refId = referencesData.get(fieldName).get(value);
                } else {
                    log.warn("Gen ReferenceId for Plugin data value['{}', '{}', '{}'] -> refernceId not found", obj.getDictName(), fieldName, value);
                }
                drb.attr(attrName, refId);
            } else {
                NsiConfigField field = attrConfig.getFields().get(0);
                MetaFieldType type = field.getType();
                if (field.getEnumValues() != null) {
                    Set<String> enums = new HashSet<>(field.getEnumValues().keySet());
                    if (enums.contains(value)) {
                        drb.attr(attrName, value);
                    } else {
                        log.error("Bad value for enum attr ['{}','{}', '{}']", obj.getDictName(), attrName, value);
                        return null;
                    }
                } else {
                    switch (type) {
                        case BOOLEAN:
                            if (value != null && value.equals("Y")) {
                                drb.attr(attrName, Boolean.TRUE);
                            } else {
                                drb.attr(attrName, Boolean.FALSE);
                            }
                            break;
                        case DATE_TIME: // TODO: пока что не предполагаем, что даты задаются из внешних файлов
                            drb.attr(attrName, RandomUtils.getDateTime());
                            break;
                        case NUMBER:
                            drb.attr(attrName, value);
                            break;
                        case CHAR:
                        case VARCHAR:
                            drb.attr(attrName, value);
                            break;
                    }
                }
            }
        }

        addCommonAttrIfExist(drb, query.getDict());

        return drb.build();

    }

    private void addCommonAttrIfExist(DictRowBuilder drb, NsiConfigDict dict) {
        if(dict.getDeleteMarkAttr() != null) {
            drb.deleteMarkAttr(false);
        }
        if(dict.getLastUserAttr() != null) {
            drb.lastUserAttr(null);
        }
        if (dict.getLastChangeAttr() != null) {
            drb.lastChangeAttr(new DateTime().withMillisOfSecond(0));
        }
        drb.idAttrNull();
    }
}
