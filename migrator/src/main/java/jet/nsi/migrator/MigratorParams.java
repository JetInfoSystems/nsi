package jet.nsi.migrator;

import java.io.File;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

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

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private String key(String ... args) {
        return Joiner.on('.').skipNulls().join(args);
    }

    public File getMetadataPath() {
        return new File(getProperty(METADATA_PATH, "/opt/nsi/database/metadata"));
    }

    public String getPlatformMigrator(String ident) {
        return getProperty(key(DB,ident,"platformMigrator"), "jet.nsi.migrator.platform.oracle.OraclePlatformMigrator");
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
        String name = getProperty(key(DB,ident,TABLESPACE,NAME), null);
        Preconditions.checkNotNull(name, "Не задано название табличного пространства для ident=%s", ident);
        return getUsername(ident) + "_" + name;
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

    public String getLogPrefix() {
        return getProperty(key(DB,"liqubase","logPrefix"), "NSI_");
    }

    public Properties getProperties() {
        return properties;
    }
}
