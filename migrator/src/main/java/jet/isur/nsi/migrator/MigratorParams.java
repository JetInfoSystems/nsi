package jet.isur.nsi.migrator;

import java.io.File;
import java.util.Properties;

import com.google.common.base.Joiner;

public class MigratorParams {

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
}
