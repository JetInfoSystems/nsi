package jet.nsi.migrator.platform;

import jet.nsi.common.migrator.config.MigratorParams;

public class PlatformMigratorFactory {

    public static PlatformMigrator create(MigratorParams params, String ident) throws Exception {
        Class<?> clasz = Thread.currentThread().getContextClassLoader().loadClass(params.getPlatformMigrator(ident));
        
        PlatformMigrator  migrator = (PlatformMigrator)clasz.newInstance();
        migrator.setParams(params);
        return migrator;
    }
}
