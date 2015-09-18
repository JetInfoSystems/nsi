package jet.isur.nsi.generator.data;



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


public class JsonDataParser {

    private static final Logger log = LoggerFactory.getLogger(JsonDataParser.class);
    
    private final String TYPE = "type";
    private final String FIELDS = "fields";
    private final String LIST = "list";
    private final String VALUES = "values";
    private final String SEPARATOR = "separator";
    private final String ENUM = "enum";
    private final String ROWSDATA = "rowsdata";
    
    protected JsonParser parser = new JsonParser();
    
    protected final Gson gson;
    
    public JsonDataParser() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Collection.class, new CollectionDeserializer());
        gson = builder.create();
    }

    public DataObject parse(File ddf, DataObject dataObject) throws IOException {
        String input;
        String filename;
        try (FileInputStream inputStream = new FileInputStream(ddf)) {
            input = IOUtils.toString(inputStream);
            filename = ddf.getCanonicalPath();
        }
        
        JsonObject mainObject = parser.parse(input).getAsJsonObject();
        if (mainObject == null || !mainObject.isJsonObject()) {
            log.error("parse ['{}'] -> failed, not found Json Object", filename);
            return null;
        }
        log.debug("get main json obj -> ok ['{}'] ", mainObject);
        
        String dictName = getFirstJsonObjectKey(mainObject);
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
        
        dataObject.setDictName(dictName);
        
        dataObject.setObj(obj);
        
        return parseCommonData(dataObject);
    }
    
    protected DataObject parseCommonData(DataObject dataObject) {
        String dictName = dataObject.getDictName();
        JsonObject obj = dataObject.getObj();
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
        final String type = obj.get(TYPE).getAsString().trim().toLowerCase();
        log.info("get type -> ok ['{}','{}']", dictName, type);

        dataObject.setType(DataObject.DataObjectType.valueOf(type));
        switch (dataObject.getType()) {
            case row :
                return parseRowType(fields, dataObject);
            case simple :
            default :
                return parseSimpleType(fields, dataObject);
        }
    }
    
    protected DataObject parseSimpleType(Collection<String> fields, DataObject dataObject) {
        int maxCount = 0;
        JsonObject obj = dataObject.getObj();
        for ( String field : fields) {
            JsonElement elem = obj.get(field);
            if (elem == null || elem.isJsonNull()) {
                log.error("Field element ['{}','{}'] -> not found ['{}']", field, dataObject.getDictName());
                return null;
            }
            JsonObject fieldObj = elem.getAsJsonObject();
            if (fieldObj == null || fieldObj.isJsonNull()) {
                log.error("Field object ['{}','{}'] -> not found", field, dataObject.getDictName());
                return null;
            }
            
            elem = fieldObj.get(key(VALUES, LIST));
            if (elem == null || elem.isJsonNull() || !elem.isJsonArray()) {
                log.error("Field values ['{}','{}'] -> not found or isn't json array", field, dataObject.getDictName());
                return null;
            }
            JsonArray valuesJsonArray = elem.getAsJsonArray();
            Collection<String> fieldValues = gson.fromJson(valuesJsonArray, ArrayList.class);
            
            dataObject.getFields().put(field, fieldValues);
            
            int count = fieldValues.size();
            maxCount = count > maxCount ? count: maxCount;
        }
        dataObject.setRowCount(maxCount);
        log.info("parse dict data of Simple type ['{}'] -> ok", dataObject.getDictName());
        return dataObject;
    }
    
    protected DataObject parseRowType(Collection<String> fields, DataObject dataObject) {
        JsonObject obj = dataObject.getObj();
        
        String fieldsSeparator = "|";
        JsonElement elem = obj.get(SEPARATOR);
        if (elem != null && !elem.isJsonNull() && elem.isJsonPrimitive() ) {
            fieldsSeparator = elem.getAsString();
        }
        
        elem = obj.get(ROWSDATA);
        if (elem == null || elem.isJsonNull() || !elem.isJsonArray()) {
            log.error("Rows data['{}'] -> not found or isn't json array", dataObject.getDictName());
            return null;
        }
        JsonArray valuesJsonArray = elem.getAsJsonArray();
        Collection<String> rowsList = gson.fromJson(valuesJsonArray, ArrayList.class);

        return parseRows(dataObject, fields, rowsList, fieldsSeparator);
    }
    
    protected DataObject parseRows(DataObject dataObject, Collection<String> fields, Collection<String> rowsList, String fieldsSeparator) {
        dataObject.setRowCount(rowsList.size());
        for (String row : rowsList) {
            Collection<String> values = parseOneRow(dataObject.getDictName(), row, fieldsSeparator, fields.size());
            Iterator<String> valIter = values.iterator();
            for (String field : fields) {
                Collection <String> fieldValues = dataObject.getFields().get(field);
                if (fieldValues == null) {
                    fieldValues = new ArrayList<>(rowsList.size());
                }
                fieldValues.add(valIter.next());
               
                dataObject.getFields().put(field, fieldValues);
            }
        }
        log.debug("parse rows ['{}'] -> ok '{}'", dataObject.getDictName(), dataObject.getFields());
        return dataObject;
    }
    
    protected Collection<String> parseOneRow (String dictName, String row, String fieldsSeparator, int size) {
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

    protected String getFirstJsonObjectKey(JsonObject obj){
        return obj.entrySet().iterator().next().getKey();
    }
    
    protected String key(String ... args) {
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
