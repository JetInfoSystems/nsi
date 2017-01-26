package jet.nsi.migrator.platform;

import jet.nsi.common.migrator.config.MigratorParams;

import java.lang.reflect.Constructor;

public class PlatformMigratorFactory {
//todo убрать его. Сделать стандартный список
    public static PlatformMigrator create(MigratorParams params, String ident) throws Exception {
        Class<?> clasz = Thread.currentThread().getContextClassLoader().loadClass(params.getPlatformMigrator(ident));
        Constructor<?> constr = clasz.getConstructor(MigratorParams.class);
        PlatformMigrator  migrator = (PlatformMigrator) constr.newInstance(params);
//        PlatformMigrator  migrator = (PlatformMigrator)clasz.newInstance();
//        migrator.setParams();
        return migrator;
    }
}
