package jet.nsi.migrator.platform.phoenix;

import com.jolbox.bonecp.BoneCPDataSource;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.common.migrator.config.MigratorParams;
import jet.nsi.common.platform.phoenix.PhoenixDialect;
//import jet.nsi.common.platform.phoenix.PhoenixJdbcDatabase;
import jet.nsi.common.platform.phoenix.PhoenixNsiPlatform;
import jet.nsi.common.platform.phoenix.PhoenixPrimaryKey;
import jet.nsi.migrator.MigratorException;
import jet.nsi.migrator.hibernate.NsiImplicitNamingStrategyImpl;
import jet.nsi.migrator.liquibase.LiqubaseAction;
import jet.nsi.migrator.platform.DefaultPlatformMigrator;
import jet.nsi.migrator.platform.DictToHbmSerializer;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;
import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PhoenixPlatformMigrator extends DefaultPlatformMigrator {

    public PhoenixPlatformMigrator(MigratorParams params) {
        super(new PhoenixNsiPlatform(), params);
    }

    @Override
    public boolean isSupportRollback() {
        return false;
    }

    @Override
    public boolean isSupportForeignKey() {
        return false;
    }

    @Override
    public boolean isNeedToInitializeSequence(){
        return false;
    }

    @Override
    public boolean isColumnEditable() {
        return false;
    }

    @Override
    public void setPrimaryKey(Table table) {
        PrimaryKey sourcePk = table.getPrimaryKey();
        PhoenixPrimaryKey pk = new PhoenixPrimaryKey(table);
        pk.setName(sourcePk.getName());
        pk.addColumns(sourcePk.getColumnIterator());
        table.setPrimaryKey(pk);
    }

    @Override
    public void doLiquibaseUpdate(String name, String file, String tag, String action, String logPrefix, DataSource dataSource) {
    }

    @Override
    public StandardServiceRegistry buildStandardServiceRegistry(DataSource dataSource) {
        final BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
        final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder(bsr);

        Properties props = new Properties();
        // TODO: используем params
        props.put(AvailableSettings.DIALECT, PhoenixDialect.class.getName());
        props.put(AvailableSettings.DATASOURCE, dataSource);
        ssrBuilder.applySettings(props);

        return ssrBuilder.build();
    }


    @Override
    public DictToHbmSerializer getDictToHbmSerializer() {
        return new PhoenixDictToHbmSerializer();
    }

    @Override
    public Connection createAdminConnection(String name, Properties properties)
            throws SQLException {
        Properties connectProperties = new Properties();
        connectProperties.put("user", properties.getProperty("db." + name + ".sys.username"));
        connectProperties.put("password", properties.getProperty("db." + name + ".sys.password"));
        return DriverManager.getConnection(properties.getProperty("db." + name + ".url"), connectProperties);
    }

    @Override
    public void createTablespace(Connection connection, String name,
                                 String dataFileName, String dataFileSize, String dataFileAutoSize,
                                 String dataFileMaxSize) {
        throw new MigratorException("createTablespace: Not supported full search yet");
    }


    @Override
    public void dropTablespace(Connection connection, String name) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" drop tablespace ").append(name).toString());
    }

    @Override
    public void createUser(Connection connection, String name, String password,
                           String defaultTablespace, String tempTablespace) {
        throw new MigratorException("createUser: Not supported full search yet");
    }

    @Override
    public void dropUser(Connection connection, String name) {
        dropSchema(connection, name);
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("drop user ").append(name).toString());
    }

    @Override
    public void createTable(NsiConfigDict dict, Connection connection) {
        CreateTableAsStep<?> createTableAsStep = platformSqlDao.getQueryBuilder(connection).createTable(dict.getTable());
        CreateTableColumnStep createTableColumnStep = null;
        for (NsiConfigField field : dict.getFields()) {
            createTableColumnStep = createTableAsStep.column(field.getName(), platformSqlDao.getDataType(field.getType())
                    .length(field.getSize()).precision(field.getSize(), field.getPrecision()));
        }
        if (createTableColumnStep != null) {
            createTableColumnStep.execute();
        } else {
            throw new NsiServiceException("no fields found");
        }
    }

    @Override
    public void dropTable(NsiConfigDict dict, Connection connection) {
        dropTable(dict.getTable(), connection);
    }

    @Override
    public void dropTable(String name, Connection connection) {
        try {
            platformSqlDao.getQueryBuilder(connection).dropTable(name).execute();
        } catch (DataAccessException e) {
            Throwable cause = e.getCause();
            throw e;


        }
    }

    @Override
    public void createSeq(NsiConfigDict dict, Connection connection) {
        platformSqlDao.getQueryBuilder(connection).createSequence(dict.getSeq()).execute();
    }

    @Override
    public void dropSeq(NsiConfigDict dict, Connection connection) {
        dropSeq(dict.getSeq(), connection);
    }

    @Override
    public void dropSeq(String name, Connection connection) {
        platformSqlDao.getQueryBuilder(connection).dropSequence(name).execute();
    }


    @Override
    public void createFullSearchIndex(NsiConfigDict dict, String field,
                                      Connection connection) {
        throw new MigratorException("createFullSearchIndex: Not supported full search yet");
    }

    @Override
    public void dropFullSearchIndex(NsiConfigDict dict, String field,
                                    Connection connection) {
        throw new MigratorException("dropFullSearchIndex: Not supported full search yet");
    }

    @Override
    public void recreateFullSearchIndex(NsiConfigDict dict, String field,
                                        Connection connection) {
        throw new MigratorException("dropFullSearchIndex: Not supported full search yet");
    }

    @Override
    public void grantUser(Connection connection, String name) {
    }


    @Override
    public DataSource createDataSource(String name, Properties properties) {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("org.apache.phoenix.jdbc.PhoenixDriver");
        dataSource.setJdbcUrl(properties.getProperty("db." + name + ".url"));
        dataSource.setConnectionTestStatement("select 1");
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }


    @Override
    public void onUpdateBeforePrepare(Connection connection, NsiConfig config) {
    }

    @Override
    public void onUpdateBeforePrepare(Connection connection, NsiConfigDict model) {
    }

    @Override
    public void onUpdateAfterPrepare(Connection connection, NsiConfigDict model) {
    }

    @Override
    public void onUpdateAfterPrepare(Connection connection, NsiConfig config) {
    }

    @Override
    public void updateMetadataSources(MetadataSources metadataSources, NsiConfig config) {
    }

    @Override
    public void updateMetadataSources(MetadataSources metadataSources, NsiConfigDict model) {
    }

    @Override
    public void onUpdateBeforePostproc(Connection connection, NsiConfig config) {
    }

    @Override
    public void onUpdateBeforePostproc(Connection connection, NsiConfigDict model) {
    }

    @Override
    public void onUpdateAfterPostproc(Connection connection, NsiConfigDict model) {
    }


    @Override
    public void onUpdateAfterPostproc(Connection connection, NsiConfig config) {
    }

    private String genIndexName(NsiConfigDict dict, String field) {
        return NsiImplicitNamingStrategyImpl.compose("IDX_", dict.getTable(), field, 30);
    }

    @Override
    public void createIndex(NsiConfigDict dict, String field,
                            Connection connection) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("CREATE INDEX ")
                .append(genIndexName(dict, field))
                .append(" ON ")
                .append(dict.getTable())
                .append("(").append(field).append(")")
                .toString());
    }

    @Override
    public void dropIndex(NsiConfigDict dict, String field,
                          Connection connection) {
        dropIndex(genIndexName(dict, field), connection);
    }

    @Override
    public void dropIndex(String name, Connection connection) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("DROP INDEX ").append(name).toString());
    }

    @Override
    public Liquibase createLiquibase(Connection c, LiqubaseAction liquibaseAction) throws LiquibaseException {
        throw new MigratorException("createLiquibase: Not supported by phoenix");

/*        Database db = new PhoenixJdbcDatabase();
        db.setConnection(new JdbcConnection(c));

        db.setDatabaseChangeLogTableName(liquibaseAction.getName() + "_LOG");
        db.setDatabaseChangeLogLockTableName(liquibaseAction.getName() + "_LOCK");
        db.setOutputDefaultCatalog(false);

        Liquibase l = new Liquibase(liquibaseAction.getFile(), new ClassLoaderResourceAccessor(), db);
        // TODO: set parameters l.setChangeLogParameter("key","value");
        return l;*/
    }


    @Override
    public void createSchema(Connection connection, String name, String user) {
        throw new MigratorException("createSchema: Not supported by phoenix");
    }

    @Override
    public void dropSchema(Connection connection, String name) {
        throw new MigratorException("dropSchema: Not supported by phoenix");
    }

}
