package jet.nsi.migrator.platform;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import jet.nsi.migrator.MigratorException;
import org.hibernate.mapping.Table;
import org.jooq.DSLContext;

import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.platform.PlatformSqlDao;
import jet.nsi.common.migrator.config.MigratorParams;
import jet.nsi.migrator.liquibase.LiqubaseAction;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import javax.sql.DataSource;

public abstract class DefaultPlatformMigrator implements PlatformMigrator {

    private final NsiPlatform platform;
    protected final PlatformSqlDao platformSqlDao;
    protected final DataSource dataSource;

    protected final MigratorParams params;

    @Override
    public boolean isSupportRollback() {
        return true;
    }

    @Override
    public boolean isSupportForeignKey() {
        return true;
    }

    @Override
    public boolean isNeedToInitializeSequence(){
        return true;
    }

    @Override
    public MigratorParams getParams() {
        return params;
    }

    @Override
    public boolean isColumnEditable() {
        return true;
    }

    public DefaultPlatformMigrator(NsiPlatform platform, MigratorParams params) {
        this.platform = platform;
        this.platformSqlDao = platform.getPlatformSqlDao();
        this.params = params;
        this.dataSource = createDataSource(platform.getPlatformName(), params.getProperties());
    }


    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    protected abstract DataSource createDataSource(String name, Properties properties) ;

    @Override
    public void doLiquibaseUpdate(String name, String file, String tag, String action, String logPrefix, DataSource dataSource) {
        LiqubaseAction la = new LiqubaseAction(composeName(logPrefix,name), file, this);
        try(Connection connection = dataSource.getConnection()) {
            la.update(connection, tag);
        } catch (SQLException e) {
            throw new MigratorException(action, e);
        }
    }

    private String composeName(String logPrefix, String name) { //todo copypast
        return logPrefix == null ? name : logPrefix + name;
    }

    @Override
    public NsiPlatform getPlatform() {
        return platform;
    }
    
    @Override
    public void removeUserProfile(Connection con, Long id) {
        DSLContext dsl = platformSqlDao.getQueryBuilder(con);
        dsl.delete(table("USER_PROFILE"))
                    .where(field("id", Long.class).eq(id)).execute();
    }

    protected int countUserProfile(Connection con, String login) {
        DSLContext dsl = platformSqlDao.getQueryBuilder(con);
        return dsl.selectCount().from(table("USER_PROFILE").as("u"))
            .where(field("u.login").eq(login)).fetchOne(0, int.class);
    }

    @Override
    public Long createUserProfile(Connection con, String login) {
        int count = countUserProfile(con, login);

        if(count > 0) {
            return null;
        }

        DSLContext dsl = platformSqlDao.getQueryBuilder(con);
        Long id = dsl.nextval("SEQ_USER_PROFILE").longValue();
        dsl.insertInto(table("USER_PROFILE"),
            field("ID"), field("IS_DELETED"), field("LOGIN"), field("STATE"))
            .values(id, "N", login, "1")
            .execute();

        return id;
    }

    @Override
    public void recreateTable(NsiConfigDict dict, Connection connection) {
        try {
            createTable(dict, connection);
        }
        catch(Exception e) {
            dropTable(dict, connection);
            createTable(dict, connection);
        }
    } 

    @Override
    public void recreateSeq(NsiConfigDict dict, Connection connection) {
        try {
            createSeq(dict, connection);
        }
        catch (Exception e) {
            dropSeq(dict,connection);
            createSeq(dict, connection);
        }
    }
    
    @Override
    public Liquibase createLiquibase(Connection c, LiqubaseAction liquibaseAction) throws LiquibaseException {

        Database db = new UnsupportedDatabase();
        db.setConnection(new JdbcConnection(c));

        db.setDatabaseChangeLogTableName(liquibaseAction.getName() + "_LOG");
        db.setDatabaseChangeLogLockTableName(liquibaseAction.getName() + "_LOCK");
        db.setOutputDefaultCatalog(false);

        Liquibase l = new Liquibase(liquibaseAction.getFile(), new ClassLoaderResourceAccessor(), db);
        // TODO: set parameters l.setChangeLogParameter("key","value");
        return l;
    }

    @Override
    public void setPrimaryKey(Table table) {
    }
}
