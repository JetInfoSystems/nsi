package jet.isur.nsi.common.utils;

import java.io.File;

import jet.isur.nsi.common.config.impl.NsiLocalGitConfigManagerImpl;
import jet.isur.nsi.common.config.impl.NsiYamlMetaDictReaderImpl;

public class ConfigUtils {
    public static NsiLocalGitConfigManagerImpl buildConfigManager(String metadataPath) {
        NsiLocalGitConfigManagerImpl configManager = new NsiLocalGitConfigManagerImpl(
                new File(metadataPath),
                new NsiYamlMetaDictReaderImpl());
        return configManager;
    }


}
