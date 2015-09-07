package jet.isur.nsi.generator.dictdata;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;


public class JsonDictdataParser {

    private static final Logger log = LoggerFactory.getLogger(JsonDictdataParser.class);

    public enum DictdataObjectType {
        simple,row;
    }
    
    private final String TYPE = "type";
    private final String FIELDS = "fields";
    private final String LIST = "list";
    private final String VALUES = "values";
    private final String SEPARATOR = "separator";
    private final String ENUM = "enum";
    private final String PARENT = "parent";
    private final String IDENT_BY = "identBy";
    private final String ROWSDATA = "rowsdata";
    
    JsonParser parser = new JsonParser();

    public DictDataObject parse(File ddf) throws IOException {
        String input;
        String filename;
        try (FileInputStream inputStream = new FileInputStream(ddf)) {
            input = IOUtils.toString(inputStream);
            filename = ddf.getCanonicalPath();
        }
        
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Collection.class, new CollectionDeserializer());
        Gson gson = builder.create();
        
        JsonObject mainObject = parser.parse(input).getAsJsonObject();
        if (mainObject == null || !mainObject.isJsonObject()) {
            log.error("parse ['{}'] -> failed, not found Json Object", filename);
            return null;
        }
        log.debug("get main json obj -> ok ['{}'] ", mainObject);
        
        String dictName = getJsonObjectKey(mainObject);
        log.info("get dict name -> ok ['{}'] ", dictName);
        
        JsonElement elem = mainObject.get(dictName);
        if (elem == null || !elem.isJsonObject()) {
            log.error("get dict data ['{}'] -> failed", dictName);
            return null;
        }
        JsonObject obj = elem.getAsJsonObject();
        if (obj == null || !obj.isJsonObject()) {
            log.error("get dict data obj ['{}'] -> failed", dictName);
            return null;
        }
        log.debug("get dict data obj ['{}'] -> ok ['{}']", dictName, obj);
        
        return parseDictData(gson, obj, dictName);
    }
    
    private DictDataObject parseDictData(Gson gson, JsonObject obj, String dictName) {
        JsonElement elem = obj.get(key(FIELDS,LIST));
        if (elem == null || elem.isJsonNull() || !elem.isJsonArray()) {
            log.error("fields list ['{}'] -> not fount or isn't array", dictName);
            return null;
        }
        JsonArray jsonFieldsList = elem.getAsJsonArray();
        log.debug("jsonFieldsList: ['{}']", jsonFieldsList.toString());
        Collection<String> fields = gson.fromJson(jsonFieldsList, ArrayList.class);
        log.info("get dict fields -> ok ['{}','{}']", dictName, fields);
        
        elem = obj.get(TYPE);
        if (elem == null || !elem.isJsonPrimitive()) {
            log.error("get type ['{}'] -> not found", dictName);
            return null;
        }
        final String type = obj.get(TYPE).getAsString();
        log.info("get type -> ok ['{}','{}']", dictName, type);

        DictDataObject dictdataObject = new DictDataObject();
        dictdataObject.setDictName(dictName);
        switch (DictdataObjectType.valueOf(type.trim().toLowerCase())) {
            case row :
                return parseRowType(gson, obj, fields, dictdataObject);
            case simple :
            default :
                return parseSimpleType(gson, obj, fields, dictdataObject);
        }
    }
    
    private DictDataObject parseSimpleType(Gson gson, JsonObject obj, Collection<String> fields, DictDataObject dictdataObject) {
        int maxCount = 0;
        for ( String field : fields) {
            JsonElement elem = obj.get(field);
            if (elem == null || elem.isJsonNull()) {
                log.error("Field element ['{}','{}'] -> not found ['{}']", field, dictdataObject.getDictName());
                return null;
            }
            JsonObject fieldObj = elem.getAsJsonObject();
            if (fieldObj == null || fieldObj.isJsonNull()) {
                log.error("Field object ['{}','{}'] -> not found", field, dictdataObject.getDictName());
                return null;
            }
            
            elem = fieldObj.get(key(VALUES, LIST));
            if (elem == null || elem.isJsonNull() || !elem.isJsonArray()) {
                log.error("Field values ['{}','{}'] -> not found or isn't json array", field, dictdataObject.getDictName());
                return null;
            }
            JsonArray valuesJsonArray = elem.getAsJsonArray();
            Collection<String> fieldValues = gson.fromJson(valuesJsonArray, ArrayList.class);
            
            dictdataObject.getFields().put(field, fieldValues);
            
            int count = fieldValues.size();
            maxCount = count > maxCount ? count: maxCount;
        }
        dictdataObject.setRowCount(maxCount);
        log.info("parse dict data of Simple type ['{}'] -> ok", dictdataObject.getDictName());
        return dictdataObject;
    }
    
    private DictDataObject parseRowType(Gson gson, JsonObject obj, Collection<String> fields, DictDataObject dictdataObject) {
        String parentFieldName = null;
        JsonElement elem = obj.get(key(PARENT, IDENT_BY));
        if (elem != null && !elem.isJsonNull() && elem.isJsonPrimitive() ) {
            parentFieldName = elem.getAsString();
        }
        dictdataObject.setParentFieldName(parentFieldName);
        
        String fieldsSeparator = "|";
        elem = obj.get(SEPARATOR);
        if (elem != null && !elem.isJsonNull() && elem.isJsonPrimitive() ) {
            fieldsSeparator = elem.getAsString();
        }
        
        elem = obj.get(ROWSDATA);
        if (elem == null || elem.isJsonNull() || !elem.isJsonArray()) {
            log.error("Rows data['{}'] -> not found or isn't json array", dictdataObject.getDictName());
            return null;
        }
        JsonArray valuesJsonArray = elem.getAsJsonArray();
        Collection<String> rowsList = gson.fromJson(valuesJsonArray, ArrayList.class);

        return parseRows(dictdataObject, fields, rowsList, fieldsSeparator);
    }
    
    private DictDataObject parseRows(DictDataObject dictdataObject, Collection<String> fields, Collection<String> rowsList, String fieldsSeparator) {
        dictdataObject.setRowCount(rowsList.size());
        for (String row : rowsList) {
            Collection<String> values = parseOneRow(dictdataObject.getDictName(), row, fieldsSeparator, fields.size());
            Iterator<String> valIter = values.iterator();
            for (String field : fields) {
                Collection <String> fieldValues = dictdataObject.getFields().get(field);
                if (fieldValues == null) {
                    fieldValues = new ArrayList<>(rowsList.size());
                }
                fieldValues.add(valIter.next());
               
                dictdataObject.getFields().put(field, fieldValues);
            }
        }
        log.debug("parse rows ['{}'] -> ok '{}'", dictdataObject.getDictName(), dictdataObject.getFields());
        return dictdataObject;
    }
    
    private Collection<String> parseOneRow (String dictName, String row, String fieldsSeparator, int size) {
        log.debug("parsing row -> ['{}']", row);
        Splitter splitter = Splitter.on(fieldsSeparator).trimResults();
        Collection<String> values = splitter.splitToList(row);
        Collection<String> retValues = new ArrayList<>(size);
        int idx = 0;
        for ( String value : values) {
            if (idx >= size) {
                log.warn("parse row ['{}': '{}']-> extra value ['{}'] found, more then declared fields, will be skipped", dictName, row, value);
                break;
            }
            if(value.equals("") || value.equalsIgnoreCase("null")) {
                retValues.add(null); 
            } else {
                retValues.add(value);
            }
            idx++;
        }
        for( int i = idx; i < size; i++) {
           retValues.add(null);
        }
        
        log.debug("parse one row -> ok ['{}']", retValues);
        return retValues;
    }

    private String getJsonObjectKey(JsonObject obj){
        return obj.entrySet().iterator().next().getKey();
    }
    
    private JsonElement getJsonMember(JsonObject obj, String memberName) {
        JsonElement elem = obj.get(memberName);
        if (elem == null) {
            
        }
        return elem;
    }
    
    private String key(String ... args) {
        return Joiner.on('.').skipNulls().join(args);
    }

    public class CollectionDeserializer implements JsonDeserializer<Collection<?>> {

        @Override
        public Collection<?> deserialize(JsonElement arrayElem, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Type type = ((ParameterizedType)typeOfT).getActualTypeArguments()[0];

            return parseAsArrayList(arrayElem, type);
        }

        /**
         * @param arrayElem
         * @param type
         * @return
         */
        @SuppressWarnings("unchecked")
        public <T> ArrayList<T> parseAsArrayList(JsonElement arrayElem, T type) {
            ArrayList<T> array = new ArrayList<T>();
            Gson gson = new Gson();

            Iterator<JsonElement> iterator = arrayElem.getAsJsonArray().iterator();

            while(iterator.hasNext()){
                JsonElement elem = (JsonElement)iterator.next();
                T obj = (T) gson.fromJson(elem, (Class<?>)type);
                array.add(obj);
            }

            return array;
        }

    }
}
