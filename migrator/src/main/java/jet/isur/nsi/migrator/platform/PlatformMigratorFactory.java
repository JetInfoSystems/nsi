package jet.isur.nsi.migrator.platform;

import jet.isur.nsi.migrator.MigratorParams;

public class PlatformMigratorFactory {

    public static PlatformMigrator create(MigratorParams params, String ident) throws Exception {
        Class<?> clasz = Thread.currentThread().getContextClassLoader().loadClass(params.getPlatformMigrator(ident));
        return (PlatformMigrator)clasz.newInstance();
    }
}
