package jet.isur.nsi.api;

import java.io.File;

import jet.isur.nsi.api.data.NsiConfigParams;

public interface NsiConfigManagerFactory {
    public NsiConfigManager create(File configPath);

    public NsiConfigManager create(File configPath, NsiConfigParams configParams);
}
