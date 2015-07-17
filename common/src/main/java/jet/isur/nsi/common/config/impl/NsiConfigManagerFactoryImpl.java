package jet.isur.nsi.common.config.impl;

import java.io.File;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.NsiConfigManagerFactory;
import jet.isur.nsi.api.data.NsiConfigParams;

public class NsiConfigManagerFactoryImpl implements NsiConfigManagerFactory {

    @Override
    public NsiConfigManager create(File configPath) {
        NsiConfigParams configParams = new NsiConfigParams();
        return new NsiLocalGitConfigManagerImpl(configPath, new NsiYamlMetaDictReaderImpl(), configParams );
    }

    @Override
    public NsiConfigManager create(File configPath, NsiConfigParams configParams) {
        return new NsiLocalGitConfigManagerImpl(configPath, new NsiYamlMetaDictReaderImpl(), configParams);
    }

}
