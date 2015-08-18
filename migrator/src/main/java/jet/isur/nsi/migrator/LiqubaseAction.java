package jet.isur.nsi.migrator;

import java.sql.Connection;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class LiqubaseAction {

    private final String name;
    private final String file;

    public LiqubaseAction(String name, String file) {
        this.name = name;
        this.file = file;
    }

    private Liquibase createLiquibase(Connection c) throws LiquibaseException {

        Database db = new OracleDatabase();
        db.setConnection(new JdbcConnection(c));

        db.setDatabaseChangeLogTableName(name + "_LOG");
        db.setDatabaseChangeLogLockTableName(name + "_LOCK");
        db.setOutputDefaultCatalog(false);

        Liquibase l = new Liquibase(file, new ClassLoaderResourceAccessor(), db);
        // TODO: set parameters l.setChangeLogParameter("key","value");
        return l;
    }


    public void update(Connection c, String tag) {
        try {
            Liquibase l = createLiquibase(c);
            l.update((String) null);
            l.tag(tag);
        } catch (LiquibaseException e) {
            throw new MigratorException("update: " + name, e);
        }

    }

    public void rollback(Connection c, String tag) {
        try {
            Liquibase l = createLiquibase(c);
            l.rollback(tag, (String) null);
        } catch (LiquibaseException e) {
            throw new MigratorException("rollback: " + name, e);
        }

    }

    public void tag(Connection c, String tag) {
        try {
            Liquibase l = createLiquibase(c);
            l.tag(tag);
        } catch (LiquibaseException e) {
            throw new MigratorException("tag: " + name, e);
        }

    }

}
