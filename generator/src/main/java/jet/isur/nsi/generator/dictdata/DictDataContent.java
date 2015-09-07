package jet.isur.nsi.generator.dictdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictDataContent {
    private static final Logger log = LoggerFactory.getLogger(DictDataContent.class);
    
    private Map<String, DictDataObject> dictdataObjsMap = new HashMap<String, DictDataObject>();
    
    public void loadDictData (DictDataFiles files) throws FileNotFoundException, IOException {
        JsonDictdataParser jddp = new JsonDictdataParser();
        for (File ddf : files.getFiles()) {
            DictDataObject dictdataObj = jddp.parse(ddf);
            if (dictdataObj != null) {
                dictdataObjsMap.put(dictdataObj.getDictName(), dictdataObj);
            }
        }
    }

    public Map<String, DictDataObject> getDictdataObjsMap() {
        return dictdataObjsMap;
    }
}
