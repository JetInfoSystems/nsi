package jet.nsi.common.migrator.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import jet.nsi.api.data.ConvertUtils;

public class MigratorParams {
    /**
     * Дефолтный базовый путь для размещения файлов данных (под Oracle ASM)
     * */
    private static final String DEFAULT_ORACLE_ASM_DATA_FILE_BASE_PATH = "+DATA/";
    /**
     * Дефолтный путь храннения метаданных справочников nsi
     * */
    private static final String DEFAULT_METADATA_PATH = "/opt/nsi/database/metadata";
    /**
     * Дефолтный базовый путь, от которого определяется нахождение changelog миграций Liquibase 
     * */
    private static final String DEFAULT_LIQUIBASE_CHANGELOG_BASE_PATH = "";
    /**
     * Дефолтый префикс таблиц логов применения миграций Liquibase
     * */
    private static final String DEFAULT_LIQUIBASE_LOG_PREFIX = "NSI_";
    
    /**
     * Дефолтный класс имплементации интерфейса PlatformMigrator - платформозависимой части мигратора nsi
     * */
    private static final String DEFAULT_PLATFORM_MIGRATOR = "jet.nsi.migrator.platform.oracle.OraclePlatformMigrator";
    
    
    /**
     * Дефолтное имя temp табличного пространства (Oracle) 
     * */
    private static final String DEFAULT_ORACLE_TEMP_TABLESPACE = "TEMP";
    
    
    /**
     * Дефолтный размер увеличения размера табличного пространства (Oracle) 
     * */
    private static final String DEFAULT_ORACLE_TABLESPACE_AUTO_SIZE = "100M";
    /**
     * Дефолтный максимальный размер табличного пространства (Oracle) 
     * */
    private static final String DEFAULT_ORACLE_TABLESPACE_MAX_SIZE = "15360M";
    /**
     * Дефолтный размер табилчного пространства (Oracle)
     * */
    private static final String DEFAULT_ORACLE_TABLESPACE_SIZE = "100M";
    
    /*
     * Части ключей параметров
     * */
    // common
    public static final String BASE = "base";
    public static final String DB = "db";
    public static final String IDENT = "ident";
    public static final String GLOBAL = "global";
    public static final String NAME = "name";
    public static final String USERNAME = "usename";
    public static final String PASSWORD = "password";
    public static final String PATH = "path";

    // nsi metadata
    public static final String METADATA_PATH = "metadataPath";
    
    // platform
    public static final String PLATFORM_MIGRATOR = "platformMigrator";
    
    // liquibase
    public static final String CHANGE_LOG = "changelog";
    public static final String LIQUIBASE = "liquibase";
    public static final String LOG_PREFIX = "changelog";
    
    // Extra (mostly platform specific)
    /**
     * Используется для указания мигратору 
     * прописывать nextval значения из sequence в качестве 
     * default значения уникального ключа таблицы(ID),
     * если позволяет платформа (имплементировано в данный момент только под PostgreSQL)
     * */
    public static final String USE_SEQUENCE_AS_DEFAULT_VALUE_FOR_ID = "useSequenceAsDefaultValueForId";

    // tablespaces (Oracle)
    /*
     * Параметры используются для создания пользователя(схемы) в Oracle
     * */
    public static final String TABLESPACE = "tablespace";
    public static final String TEMP_TABLESPACE = "tempTablespace";
    
    // datafiles (Oracle)
    public static final String DATA_FILE = "dataFile";
    
    // tablespace sizing (Oracle)
    /*
     * Параметры используются для создания tablespace в Oracle
     * */
    public static final String AUTO_SIZE = "autoSize";
    public static final String MAX_SIZE = "maxSize";
    public static final String SIZE = "size";
    

    private final Properties properties;

    public MigratorParams(Properties properties) {
        this.properties = properties;
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public static final String key(String ... args) {
        return Joiner.on('.').skipNulls().join(args);
    }

    public File getMetadataPath() {
        return new File(getProperty(METADATA_PATH, DEFAULT_METADATA_PATH));
    }
    
    public String getChangelogBasePath() {
        return getProperty(key(LIQUIBASE,CHANGE_LOG,BASE,PATH), DEFAULT_LIQUIBASE_CHANGELOG_BASE_PATH);
    }
    

    public String getPlatformMigrator(String ident) {
        return getProperty(key(DB,ident,PLATFORM_MIGRATOR), DEFAULT_PLATFORM_MIGRATOR);
    }
    
    public String getUsername(String ident) {
        return getProperty(key(DB,ident,USERNAME), null);
    }

    public String getPassword(String ident) {
        return getProperty(key(DB,ident,PASSWORD), null);
    }

    public String getGlobalName(String ident) {
        return getProperty(key(DB,ident,GLOBAL,NAME), null);
    }

    public String getTablespace(String ident) {
        String name = getProperty(key(DB,ident,TABLESPACE,NAME), null);
        Preconditions.checkNotNull(name, "Не задано название табличного пространства для ident=%s", ident);
        return getUsername(ident) + "_" + name;
    }

    public String getTempTablespace(String ident) {
        return getProperty(key(DB,ident,TEMP_TABLESPACE,NAME), DEFAULT_ORACLE_TEMP_TABLESPACE);
    }

    public String getDataFileBasePath(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,BASE,PATH),DEFAULT_ORACLE_ASM_DATA_FILE_BASE_PATH);
    }

    public String getDataFilePath(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,PATH),
                getDataFileBasePath(ident) + getGlobalName(ident) + "/datafile/");
    }

    public String getDataFileName(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,NAME), getTablespace(ident) + ".dbf");
    }

    public String getDataFileSize(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,SIZE), DEFAULT_ORACLE_TABLESPACE_SIZE);
    }

    public String getDataFileAutoSize(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,AUTO_SIZE), DEFAULT_ORACLE_TABLESPACE_AUTO_SIZE);
    }

    public String getDataFileMaxSize(String ident) {
        return getProperty(key(DB,ident,TABLESPACE,DATA_FILE,MAX_SIZE), DEFAULT_ORACLE_TABLESPACE_MAX_SIZE);
    }

    public String getLogPrefix() {
        return getProperty(key(DB,LIQUIBASE,LOG_PREFIX), DEFAULT_LIQUIBASE_LOG_PREFIX);
    }
    
    public List<String> getDbList() {
        String dbIdent = getProperty(key("db.list"), null);
        Preconditions.checkNotNull(dbIdent, "Не задан список БД");
        return Arrays.stream(dbIdent.split(",")).map(String::trim).collect(Collectors.toList());
    }
    
    public boolean getUseSequenceAsDefaultValueForId(String ident) {
        String propValue = getProperty(key(DB,ident,USE_SEQUENCE_AS_DEFAULT_VALUE_FOR_ID), Boolean.FALSE.toString());
        if (Strings.isNullOrEmpty(propValue)) {
            return false;
        }
        return ConvertUtils.stringToBool(propValue);
    }

    public Properties getProperties() {
        return properties;
    }
}
