package jet.isur.nsi.generator.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jet.isur.nsi.generator.data.JsonDataParser;
import jet.isur.nsi.generator.data.Reference;

public class JsonPluginDataParser extends JsonDataParser {
    
    private static final Logger log = LoggerFactory.getLogger(JsonPluginDataParser.class);
    
    private final String PARENT = "parent";
    private final String REF_ATRR_NAME = "ref.attr.name";
    
    public PluginDataObject parse(File ddf) throws IOException {
        PluginDataObject dataObject = new PluginDataObject();
        super.parse(ddf, dataObject);
        
        dataObject = parseReferencesInfo(dataObject);
        return dataObject;
    }
    
    private PluginDataObject parseReferencesInfo(PluginDataObject dataObject){
        JsonObject obj = dataObject.getObj();
        
        Collection<String> fieldNames = dataObject.getFields().keySet();
        for (String fieldName : fieldNames) {
            if (fieldName.startsWith(PluginDataObject.REF_PREFIX)) {
                JsonElement elem = obj.get(fieldName);
                if (elem == null || elem.isJsonNull() || !elem.isJsonObject() ) {
                    log.warn("Reference info ['{}', '{}'] -> not found, use default reference naming", dataObject.getDictName());
                    dataObject.getRefMap().put(fieldName, getDefaultRefereceInfo(fieldName));
                } else {
                    dataObject.getRefMap().put(fieldName, parseCustomRefereceInfo(fieldName, elem.getAsJsonObject()));
                }
            }
        }
        
        return dataObject;
    }
    
    private String getDefaultReferenceAttrName(String refDictName) {
        return refDictName + "_ID";
    }
    private String getDefaultReferenceTargetAttrName(String refDictName) {
        return refDictName + "_NAME";
    }

    private Reference getDefaultRefereceInfo(String fieldName) {
        int l = PluginDataObject.REF_PREFIX.length();
        String targetFieldName= fieldName.substring(l);
        String targetDictName = fieldName.substring(l, fieldName.indexOf("_NAME"));
        String targetRefAttrName = getDefaultReferenceAttrName(targetDictName);
        log.debug("get default tagetDictName: " + targetDictName);
        log.debug("get default tagetFieldName: " + targetFieldName);
        Reference refInfo = new Reference();
        refInfo.setDictName(targetDictName);
        refInfo.setFieldName(targetFieldName);
        refInfo.setRefAttrName(targetRefAttrName);
        return refInfo;
    }
    
    private Reference parseCustomRefereceInfo(String fieldName, JsonObject obj) {
        Reference refInfo = new Reference();
        
        for ( Iterator<Entry<String, JsonElement>> iterator = obj.entrySet().iterator(); iterator.hasNext();) {
            String key = iterator.next().getKey();
            if (key.equals(REF_ATRR_NAME)) {
                JsonElement elem = obj.get(key);
                if (elem == null || elem.isJsonNull() || !elem.isJsonPrimitive()) {
                    log.warn("Reference info ['{}', '{}'] -> parse ref attr name error, use default reference attribute naming");
                    continue;
                } else {
                    refInfo.setRefAttrName(elem.getAsString());
                }
            } else {
                refInfo.setDictName(key);
                JsonElement elem = obj.get(key);
                if (elem == null || elem.isJsonNull() || !elem.isJsonPrimitive()) {
                    log.warn("Reference info ['{}', '{}'] -> parse target field name error, use default target field naming");
                    continue;
                } else {
                    refInfo.setFieldName(elem.getAsString());
                }
            }
        }
        if (refInfo.getRefAttrName() == null) {
            refInfo.setRefAttrName(getDefaultReferenceAttrName(refInfo.getDictName()));
        }
        if (refInfo.getFieldName() == null) {
            refInfo.setFieldName(getDefaultReferenceTargetAttrName(refInfo.getDictName()));
        }
        log.debug("parse custom reference attr name: " + refInfo.getRefAttrName());
        log.debug("parse custom reference dict name: " + refInfo.getDictName());
        log.debug("parse custom reference target field name: " + refInfo.getFieldName());
        
        return refInfo;
    }
}
