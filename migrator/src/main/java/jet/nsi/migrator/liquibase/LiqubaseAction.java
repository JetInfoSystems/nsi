package jet.nsi.migrator.liquibase;

import java.sql.Connection;

import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.migrator.MigratorException;
import jet.nsi.migrator.platform.PlatformMigrator;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class LiqubaseAction {

    private final String name;
    private final String file;
    private final PlatformMigrator platformMigrator;

    public LiqubaseAction(String name, String file, PlatformMigrator platformMigrator) {
        this.name = name;
        this.file = file;
        this.platformMigrator = platformMigrator;
    }


    public void update(Connection c, String tag) {
        try {
            Liquibase l = platformMigrator.createLiquibase(c, this);
            l.update((String) null);
            l.tag(tag);
        } catch (LiquibaseException e) {
            throw new MigratorException("update: " + name, e);
        }

    }

    public void rollback(Connection c, String tag) {
        try {
            Liquibase l = platformMigrator.createLiquibase(c, this);
            l.rollback(tag, (String) null);
        } catch (LiquibaseException e) {
            throw new MigratorException("rollback: " + name, e);
        }

    }

    public void tag(Connection c, String tag) {
        try {
            Liquibase l = platformMigrator.createLiquibase(c, this);
            l.tag(tag);
        } catch (LiquibaseException e) {
            throw new MigratorException("tag: " + name, e);
        }

    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

}
