package jet.nsi.generator.dictdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.common.data.DictDependencyGraph;
import jet.nsi.generator.DBAppender;
import jet.nsi.generator.GeneratorParams;
import jet.nsi.generator.data.DataFiles;
import jet.nsi.generator.helpers.GeneratorDictRowHelper;
import jet.nsi.generator.helpers.GeneratorHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictDataContent {
    private static final Logger log = LoggerFactory.getLogger(DictDataContent.class);

    private final NsiConfig config;
    private final DBAppender appender;
    private final GeneratorParams params;
    private final GeneratorHelper genHelper;
    private final GeneratorDictRowHelper dictRowHelper;

    private Map<String, DictDataObject> dictdataObjsMap = new HashMap<String, DictDataObject>();

    public DictDataContent(NsiConfig config, DBAppender appender, GeneratorParams params) {
        this.config = config;
        this.appender = appender;
        this.params = params;

        this.genHelper = new GeneratorHelper(config, params);
        this.dictRowHelper = new GeneratorDictRowHelper();
    }

    public Map<String, DictDataObject> getDictdataObjsMap() {
        return dictdataObjsMap;
    }

    /**
     * Реализует загрузку/обновление всех корневых справочников
     * из файлов каталога с данными справочника в json формате
     * каталог указывается в конфигурации запуска (параметр dictdataPath)
     * @param dictsIds - отображение конфигурации Nsi-справочник -> идентификаторы строк данных справочника.
     *                   Заполняется/обновляется в процессе загурузки/обновления корневых справочников
     * @return множество имен справочников, которые были загружены/обновлены
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Set<String> addAllRootDicts(Map <NsiConfigDict, List<Long>> dictsIds) throws FileNotFoundException, IOException{
        DictDataFiles dictdataFiles = new DictDataFiles(params.getDictdataPath());

        loadDictData(dictdataFiles);

        DictDependencyGraph dictGraph = genHelper.getGraph(dictdataObjsMap.keySet());
        List<NsiConfigDict> dictList = dictGraph.sort();

        log.info("Generated Graph ['{}']", genHelper.getDictListAsString(dictList));

        Set<String> loadedDictList = new HashSet<>(dictList.size());
        for (NsiConfigDict dict : dictList) {
           List<Long> ids = addRootDictData(dict);
           if (ids != null ) {
               loadedDictList.add(dict.getName());
               genHelper.addIds(dict, ids, dictsIds);
           }
        }
        return loadedDictList;
    }

    private List<Long> addRootDictData(NsiConfigDict dict) {
        log.info("addRootDictData start for ['{}']", dict.getName());

        List<DictRow> curDataList = appender.getData(dict);

        NsiQuery query = dict.query().addAttrs();

        DictDataObject ddObj = dictdataObjsMap.get(dict.getName());
        if (ddObj == null) {
            log.warn("addRootDictData ['{}'] -> source data not found", dict.getName());
            return null;
        }

        curDataList.addAll(reloadDictData(query, dict, ddObj, curDataList));
        List<Long> ids = genHelper.getIds(curDataList);
        if (ids == null || ids.isEmpty()) {
             log.warn("addRootDictData ['{}'] -> no data loaded", dict.getName());
             return null;
        }

        log.info("addRootDictData['{}'] -> ok, but without parents yet", dict.getName());

        addParents(query, dict, ddObj, curDataList, ids);
        return ids;
    }

    private List<DictRow> reloadDictData(NsiQuery query, NsiConfigDict dict, DictDataObject ddObj, List<DictRow> curDataList) {
        List<DictRow> updateDataList = new ArrayList<>();
        Set<Integer> mergedIdxs = updateEqualByRefAttrs(query, dict, ddObj, curDataList, updateDataList);

        List<DictRow> newDataList = new ArrayList<>();
        for(int i = 0; i < ddObj.getRowCount(); i ++) {
            if(mergedIdxs.contains(i)) { //Merged not need to add
                continue;
            }

            DictRow newRow = dictRowHelper.genDictdataContentRow(query, ddObj, i, null);
            if (newRow != null) {
                newDataList.add(newRow);
            }
        }
        appender.updateData(dict, updateDataList);
        appender.addData(dict, newDataList);

        updateDataList.addAll(newDataList);
        return updateDataList;
    }

    private Set<Integer> updateEqualByRefAttrs(NsiQuery query, NsiConfigDict dict, DictDataObject ddObj, List<DictRow> curDataList,
            List<DictRow> updateDataList) {
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
                DictRow toUdate = dictRowHelper.genDictdataContentRow(query, ddObj, idx, cdr);
                if ( toUdate != null) {
                    updateDataList.add(toUdate);
                    mergedIndexes.add(idx);
                }
            }
        }
        curDataList.removeAll(updateDataList);
        return mergedIndexes;
    }


    private void loadDictData (DataFiles files) throws FileNotFoundException, IOException {
        JsonDictDataParser jddp = new JsonDictDataParser();
        for (File ddf : files.getFiles()) {
            DictDataObject dictdataObj = jddp.parse(ddf);
            if (dictdataObj != null) {
                dictdataObjsMap.put(dictdataObj.getDictName(), dictdataObj);
            }
        }
    }

    private void addParents(NsiQuery query, NsiConfigDict dict, DictDataObject ddObj, List<DictRow> curDataList, List<Long> ids) {
        Collection<String> ddParentNames = ddObj.getFields().get("parent");


    }

}
