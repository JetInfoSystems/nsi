package jet.nsi.api;

import java.io.File;

import jet.nsi.api.data.NsiConfigParams;

public interface NsiConfigManagerFactory {
    public NsiConfigManager create(File configPath);

    public NsiConfigManager create(File configPath, NsiConfigParams configParams);
}
