package jet.nsi.generator.dictdata;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jet.nsi.generator.data.JsonDataParser;

public class JsonDictDataParser extends JsonDataParser {
    
    private static final Logger log = LoggerFactory.getLogger(JsonDictDataParser.class);
    
    private final String PARENT = "parent";
    private final String IDENT_BY = "identBy";
    
    public DictDataObject parse(File ddf) throws IOException {
        DictDataObject dataObject = new DictDataObject();
        super.parse(ddf, dataObject);
        
        dataObject = parseParent(dataObject);
        return dataObject;
    }
    
    private DictDataObject parseParent(DictDataObject dictdataObject){
        JsonObject obj = dictdataObject.getObj();
        
        String parentFieldName = null;
        
        JsonElement elem = obj.get(key(PARENT, IDENT_BY));
        if (elem != null && !elem.isJsonNull() && elem.isJsonPrimitive() ) {
            parentFieldName = elem.getAsString();
        }
        dictdataObject.setParentFieldName(parentFieldName);
        return dictdataObject;
    }

}
