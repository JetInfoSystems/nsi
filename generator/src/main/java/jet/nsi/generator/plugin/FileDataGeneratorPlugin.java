/*
package jet.nsi.generator.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.common.data.DictDependencyGraph;
import jet.nsi.generator.DBAppender;
import jet.nsi.generator.GeneratorParams;
import jet.nsi.generator.data.DataFiles;
import jet.nsi.generator.data.Reference;
import jet.nsi.generator.dictdata.DictDataContent;
import jet.nsi.generator.helpers.GeneratorDictRowHelper;
import jet.nsi.generator.helpers.GeneratorHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileDataGeneratorPlugin implements GeneratorPlugin {
    private static final Logger log = LoggerFactory.getLogger(FileDataGeneratorPlugin.class);

    */
/**
     * appender - добавляет данные непосредственно в базу
     *//*

    private DBAppender appender;

    private GeneratorHelper genHelper;

    private Map<String, PluginDataObject> plugindataObjsMap = new HashMap<>();
    private Map<String, Map<String, Long>> referencesData = new HashMap<>();

    private final GeneratorDictRowHelper dictRowHelper = new GeneratorDictRowHelper();

    protected Collection<String> dictNames;

    public void setDictNames(Collection<String> dictNames) {
        this.dictNames = dictNames;
    }

    @Override
    public void execute(NsiConfig config, DataSource dataSource, GeneratorParams params) throws Exception {
        appender = new DBAppender(dataSource, config);
        genHelper = new GeneratorHelper(config, params);

        DictDataContent dictdataContent = new DictDataContent(config, appender, params);
        Map<NsiConfigDict, List<Long>> dictsIds = new HashMap<>();
        dictdataContent.addAllRootDicts(dictsIds);

        loadPluginData(params.getPlugindataPath());

        DictDependencyGraph dictGraph = genHelper.getGraph(dictNames);
        List<NsiConfigDict> dictList = dictGraph.sort();
        log.info("Generated Graph ['{}']", genHelper.getDictListAsString(dictList));
        for (NsiConfigDict dict : dictList) {
           if(plugindataObjsMap.get(dict.getName()) != null) {
               generateDict(config, dict.getName());
           }
        }
    }

    protected void generateDict(NsiConfig config, String dictName) {
        PluginDataObject obj = plugindataObjsMap.get(dictName);
        Map<String, Collection<String>> ddFields = obj.getFields();

        fillReferencesData(config, obj.getRefMap());

        NsiConfigDict dict = config.getDict(dictName);
        NsiQuery query = dict.query().addStdAttrs();
        for (String fieldName : ddFields.keySet()) {
            String attrName = fieldName;
            if (fieldName.startsWith(PluginDataObject.REF_PREFIX)) {
                attrName = obj.getRefMap().get(fieldName).getRefAttrName();
            }
            query.addAttr(attrName);
        }

        List<DictRow> newDataList = new ArrayList<>(obj.getRowCount());
        for (int i = 0; i < obj.getRowCount(); i++) {
            newDataList.add(dictRowHelper.genDictRow(query, obj, referencesData, i));
        }
        appender.addData(dict, newDataList);
    }

    protected void fillReferencesData(NsiConfig config, Map<String, Reference> refInfoMap) {
        for (String refName : refInfoMap.keySet()) {
            if(referencesData.get(refName) == null) { // Читаем данные из базы, только если еще не читали
                referencesData.put(refName, getReferenceData(config, refName, refInfoMap.get(refName)));
            }
        }
    }

    protected Map<String, Long> getReferenceData(NsiConfig config, String refName, Reference refInfo) {
        log.debug("get reference data for ['{}', '{}']", refName, refInfo.getDictName());
        NsiConfigDict refDict = config.getDict(refInfo.getDictName());
        List<DictRow> refDictDataList = appender.getData(refDict);

        Map<String, Long> nameToId = new HashMap<>();
        for (DictRow row : refDictDataList) {
            Long id = row.getIdAttrLong();
            String name = row.getString(refInfo.getFieldName());
            Long existId = nameToId.get(name);
            if (existId == null || existId < id) {
                nameToId.put(name, id);
            }
        }
        return nameToId;
    }

    protected void loadPluginData (File dataFilesPath) throws FileNotFoundException, IOException {
        DataFiles files = new PluginDataFiles(dataFilesPath, dictNames);
        JsonPluginDataParser jddp = new JsonPluginDataParser();
        for (File df : files.getFiles()) {
            PluginDataObject dataObj = jddp.parse(df);
            if (dataObj != null) {
                plugindataObjsMap.put(dataObj.getDictName(), dataObj);
            }
        }
    }

}
*/
