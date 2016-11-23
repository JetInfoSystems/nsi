package jet.nsi.migrator.platform;

import jet.nsi.common.config.MigratorParams;

public class PlatformMigratorFactory {

    public static PlatformMigrator create(MigratorParams params, String ident) throws Exception {
        Class<?> clasz = Thread.currentThread().getContextClassLoader().loadClass(params.getPlatformMigrator(ident));
        return (PlatformMigrator)clasz.newInstance();
    }
}
