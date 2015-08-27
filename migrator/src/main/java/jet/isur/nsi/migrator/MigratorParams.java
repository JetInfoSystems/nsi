package jet.isur.nsi.migrator;

import java.io.File;
import java.util.Properties;

import com.google.common.base.Joiner;

public class MigratorParams {

    private static final String DATA_FILE = "dataFile";
    public static final String NAME = "name";
    public static final String TABLESPACE = "tablespace";
    public static final String DB = "db";


    public static final String METADATA_PATH = "metadataPath";


    private final Properties properties;

    public MigratorParams(Properties properties) {
        this.properties = properties;
    }

    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private String key(String ... args) {
        return Joiner.on('.').skipNulls().join(args);
    }

    public File getMetadataPath() {
        return new File(getProperty(METADATA_PATH, "/opt/isur/database/metadata"));
    }

    public String getUsername(String ident) {
        return getProperty(key(DB,ident,"username"), null);
    }

    public String getPassword(String ident) {
        return getProperty(key(DB,ident,"password"), null);
    }

    public String getGlobalName(String ident) {
        return getProperty(key(DB,ident,"global","name"), null);
    }

    public String getTablespace(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,NAME), getUsername(ident));
    }

    public String getTempTablespace(String ident) {
        return getProperty(key(DB,ident,"tempTablespace",NAME), "TEMP");
    }

    public String getDataFilePath(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,"dataFile","path"),
                "+DATA/" + getGlobalName(ident) + "/datafile/");
    }

    public String getDataFileName(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,"dataFile","name"), getTablespace(ident) + ".dbf");
    }

    public String getDataFileSize(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,"size"), "100M");
    }

    public String getDataFileAutoSize(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,"autoSize"), "100M");
    }

    public String getDataFileMaxSize(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,"maxSize"), "15360M");
    }
}