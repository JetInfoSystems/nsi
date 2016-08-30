package jet.nsi.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import jet.nsi.api.data.NsiConfigDict;

public class GeneratorParams {

    public static final String LIST = "list";
    public static final String COUNT = "count";
    public static final String TYPE = "type";
    public static final String DICT = "dict";
    public static final String APPEND = "append";
    public static final String DEFAULT = "default";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String METADATA_PATH = "metadataPath";
    public static final String DICTDATA_PATH = "dictdataPath";
    public static final String PLUGINDATA_PATH = "plugindataPath";


    private final Properties properties;

    public GeneratorParams(Properties properties) {
        this.properties = properties;
    }

    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private String key(String ... args) {
        return Joiner.on('.').skipNulls().join(args);
    }

    public boolean getDictAppend(NsiConfigDict dict) {
        return Boolean.parseBoolean(getProperty(key(DICT,dict.getName(),APPEND),
                getProperty(key(DICT,DEFAULT,APPEND),"false")));
    }

    public int getDictCount(NsiConfigDict dict) {
        return Integer.parseInt(getProperty(key(DICT,dict.getName(),COUNT),
                getProperty(key(DICT,DEFAULT,COUNT),"10")));
    }


    public List<String> getDictList() {
        ArrayList<String> result = new ArrayList<>();
        for (String v : Splitter.on(',').split(getProperty(key(DICT,LIST), ""))) {
            result.add(v);
        }
        return result;
    }

    public File getMetadataPath() {
        return new File(getProperty(METADATA_PATH, "/opt/nsi/database/metadata"));
    }
    
    public File getDictdataPath() {
        return new File(getProperty(DICTDATA_PATH, "./dictdata"));
    }
    
    public File getPlugindataPath() {
        return new File(getProperty(PLUGINDATA_PATH, "./gplugindata"));
    }
}
