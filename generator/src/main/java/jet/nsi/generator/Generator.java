/*
package jet.nsi.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.common.data.DictDependencyGraph;
import jet.nsi.generator.dictdata.DictDataContent;
import jet.nsi.generator.helpers.GeneratorDictRowHelper;
import jet.nsi.generator.helpers.GeneratorHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {

    private static final Logger log = LoggerFactory.getLogger(Generator.class);

    public static final String CMD_CLEAN_DATA = "cleanData";
    public static final String CMD_APPEND_DATA = "appendData";

    */
/**
     * конфигурация с метаданными
     *//*

    private final NsiConfig config;

    */
/**
     * appender - добавляет данные непосредственно в базу
     *//*

    private final DBAppender appender;

    private Map<NsiConfigDict, List<Long>> dictsIds = new HashMap<>();

    private final GeneratorParams params;

    private final GeneratorHelper genHelper;

    private final GeneratorDictRowHelper dictRowHelper = new GeneratorDictRowHelper();

    public Generator(NsiConfig config, DBAppender appender, GeneratorParams params) {
        this.config = config;
        this.appender = appender;
        this.params = params;

        this.genHelper = new GeneratorHelper(config, params);
    }

    public Map<NsiConfigDict,List<Long>> appendData() {
        Set<String> loadedDictList = new HashSet<>();
        DictDataContent dictDataContent = new DictDataContent(config, appender, params);
        try {
            loadedDictList = dictDataContent.addAllRootDicts(dictsIds);
        } catch (IOException e) {
            log.error("appendData -> failed to load root dicts", e);
            return null;
        }

        DictDependencyGraph dictGraph = genHelper.getGraph();
        List<NsiConfigDict> dictList = dictGraph.sort();

        log.info("appendData -> generated Graph ['{}']", genHelper.getDictListAsString(dictList));
        for (NsiConfigDict dict : dictList) {
            String dictName = dict.getName();
            if (loadedDictList.contains(dictName)) {
                log.info("appendData -> skipping already loaded root dict ['{}']", dictName);
            } else if (dictName.startsWith("FIAS")){
                log.info("appendData -> skipping FIAS dicts['{}']", dictName);
            } else{
                addData(dict, genHelper.getDictCount(dict));
            }
        }
        return dictsIds;
    }

    */
/**
     * Генерация и добавление данных в справочник
     * @param dict описание справочника
     * @param count количество записей
     *//*

    private void addData(NsiConfigDict dict, int count){

        log.info("addData ['{}'] -> starting", dict.getName());

        List<DictRow> curDataList = appender.getData(dict);

        NsiQuery query = dict.query().addAttrs();

        if(curDataList.size() < count) {
            List<DictRow> newDataList = new ArrayList<>(count);
            for (int i=curDataList.size(); i < count; i++) {
                newDataList.add(dictRowHelper.genDictRow(query, i, dictsIds));
            }
            newDataList = appender.addData(dict, newDataList);
            curDataList.addAll(newDataList);
            genHelper.addIds(dict, genHelper.getIds(curDataList), dictsIds);
        }
    }

    public void cleanData() {
        DictDependencyGraph graph = genHelper.getGraph();
        List<NsiConfigDict> dictList = graph.sort();
        Collections.reverse(dictList);
        for (NsiConfigDict dict : dictList) {
            if (dict.getName().startsWith("FIAS")) {
                log.info("cleanData -> skipping FIAS dict ['{}']", dict.getName());
            } else {
                appender.cleanData(dict);
            }
        }
    }

}
*/
