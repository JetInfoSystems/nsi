package jet.isur.nsi.api;

import java.io.File;

public interface NsiConfigManagerFactory {
    public NsiConfigManager create(File configPath);
}
