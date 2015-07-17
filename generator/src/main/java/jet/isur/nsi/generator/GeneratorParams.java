package jet.isur.nsi.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jet.isur.nsi.api.data.NsiConfigDict;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class GeneratorParams {

    public static final String ADD = "add";
    public static final String COUNT = "count";
    public static final String TYPE = "type";
    public static final String DICT = "dict";
    public static final String APPEND = "append";
    public static final String DEFAULT = "default";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String METADATA_PATH = "metadataPath";


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


    public List<String> getAdd() {
        ArrayList<String> result = new ArrayList<>();
        for (String v : Splitter.on(',').split(getProperty(ADD, ""))) {
            result.add(v);
        }
        return result;
    }

    public File getMetadataPath() {
        return new File(getProperty(METADATA_PATH, "/opt/isur/database/metadata"));
    }
}
