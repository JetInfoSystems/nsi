package jet.nsi.common.config.impl;

import java.io.File;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.NsiConfigManagerFactory;
import jet.nsi.api.data.NsiConfigParams;

public class NsiConfigManagerFactoryImpl implements NsiConfigManagerFactory {

    @Override
    public NsiConfigManager create(File configPath) {
        NsiConfigParams configParams = new NsiConfigParams();
        return new NsiLocalGitConfigManagerImpl(configPath, new NsiYamlMetaDictReaderImpl(), new NsiYamlMetaDictWriterImpl(), configParams );
    }

    @Override
    public NsiConfigManager create(File configPath, NsiConfigParams configParams) {
        return new NsiLocalGitConfigManagerImpl(configPath, new NsiYamlMetaDictReaderImpl(), new NsiYamlMetaDictWriterImpl(), configParams);
    }

}
