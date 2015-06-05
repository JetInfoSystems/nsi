package jet.isur.nsi.common.config.impl;

import java.io.File;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.NsiConfigManagerFactory;

public class NsiConfigManagerFactoryImpl implements NsiConfigManagerFactory {

    @Override
    public NsiConfigManager create(File configPath) {
        return new NsiLocalGitConfigManagerImpl(configPath, new NsiYamlMetaDictReaderImpl());
    }

}
