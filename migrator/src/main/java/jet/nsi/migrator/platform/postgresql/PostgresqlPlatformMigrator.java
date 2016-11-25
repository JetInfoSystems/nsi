package jet.nsi.migrator.platform.postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.exception.DataAccessException;
import org.postgresql.util.PSQLException;

import com.jolbox.bonecp.BoneCPDataSource;

import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.common.migrator.config.MigratorParams;
import jet.nsi.common.platform.postgresql.PostgresqlNsiPlatform;
import jet.nsi.migrator.MigratorException;
import jet.nsi.migrator.hibernate.NsiImplicitNamingStrategyImpl;
import jet.nsi.migrator.liquibase.LiqubaseAction;
import jet.nsi.migrator.platform.DefaultPlatformMigrator;
import jet.nsi.migrator.platform.DictToHbmSerializer;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class PostgresqlPlatformMigrator extends DefaultPlatformMigrator {
    
    private final String UNDEFINED_TABLE_ERROR_CODE = "42P01";
    //private final PostgresqlFtsModule ftsModule;
    
    public PostgresqlPlatformMigrator(MigratorParams params) {
        super(new PostgresqlNsiPlatform(), params);
        //this.ftsModule = new PostgresqlFtsModule(platformSqlDao);
    }

    @Override
    public StandardServiceRegistry buildStandardServiceRegistry(DataSource dataSource) {
        final BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
        final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder( bsr );

        Properties props = new Properties();
        // TODO: используем params
        props.put(AvailableSettings.DIALECT, NsiPostresqlDialect.class.getName());
        props.put(AvailableSettings.DATASOURCE, dataSource);
        ssrBuilder.applySettings( props );

        return ssrBuilder.build();
    }

    @Override
    public DictToHbmSerializer getDictToHbmSerializer() {
        return new PostgresqlDictToHbmSerializer(params.getUseSequenceAsDefaultValueForId(params.getDbIdent()));
    }

    @Override
    public Connection createAdminConnection(String name, Properties properties)
            throws SQLException {
        Properties connectProperties = new Properties();
        connectProperties.put("user", properties.getProperty("db." + name + ".sys.username"));
        connectProperties.put("password", properties.getProperty("db." + name + ".sys.password"));
        return DriverManager.getConnection (properties.getProperty("db." + name + ".url"), connectProperties );
    }

    @Override
    public void createTablespace(Connection connection, String name,
            String dataFileName, String dataFileSize, String dataFileAutoSize,
            String dataFileMaxSize) {
        createTablespace(connection, name, dataFileName);
    }
    
    
    @Override
    public void dropTablespace(Connection connection, String name){
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" drop tablespace ").append(name).toString());
    }

    @Override
    public void createUser(Connection connection, String name, String password,
            String defaultTablespace, String tempTablespace) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" create user ").append(name)
                .append(" password '").append(password).append("'").toString());
        
        setTablespaceOwner(connection, defaultTablespace, name);
        createSchema(connection, name, name);
        grantUser(connection, name);
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
                    .length(field.getSize()).precision(field.getSize(),field.getPrecision()));
        }
        if(createTableColumnStep != null) {
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
        }
        catch(DataAccessException e) {
            Throwable cause = e.getCause();
            if(cause instanceof PSQLException) {
                throwIfNot((PSQLException)cause, UNDEFINED_TABLE_ERROR_CODE);
            } else {
                throw e;
            }
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
        try {
            platformSqlDao.getQueryBuilder(connection).dropSequence(name).execute();
        } catch(DataAccessException e) {
            Throwable cause = e.getCause();
            if(cause instanceof PSQLException) {
                throwIfNot((PSQLException)cause, UNDEFINED_TABLE_ERROR_CODE);
            } else {
                throw e;
            }
        }
    }
    
   
    
    @Override
    public void createFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        // ftsModule.createFullSearchIndex(dict, field, connection);
        throw new MigratorException("createFullSearchIndex: Not supported full search yet");
    }

    @Override
    public void dropFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        // ftsModule.dropFullSearchIndex(dict, field, connection);
        throw new MigratorException("dropFullSearchIndex: Not supported full search yet");
    }

    @Override
    public void recreateFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        try {
            createFullSearchIndex(dict, field, connection);
        }
        catch(Exception e) {
            dropFullSearchIndex(dict, field, connection);
            createFullSearchIndex(dict, field, connection);
        }
    }

    @Override
    public void grantUser(Connection connection, String name) {
    }

    

    @Override
    public DataSource createDataSource(String name, Properties properties) {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("org.postgresql.Driver");
        dataSource.setJdbcUrl(properties.getProperty("db." + name + ".url"));
        dataSource.setUsername(properties.getProperty("db." + name + ".username"));
        dataSource.setPassword(properties.getProperty("db." + name + ".password"));
        dataSource.setConnectionTimeoutInMs(15000);
        dataSource.setConnectionTestStatement("select 1");
        dataSource.setMaxConnectionsPerPartition(Integer.parseInt(properties.getProperty("db." + name + ".size", "20")));
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
//        try {
//            ftsModule.updateFtsIndexesAfterPostproc(connection, model);
//        } catch (Exception e) {
//            throw new MigratorException("onUpdateAfterPostproc", e);
//        }
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

        Database db = new PostgresDatabase();
        db.setConnection(new JdbcConnection(c));

        db.setDatabaseChangeLogTableName(liquibaseAction.getName() + "_LOG");
        db.setDatabaseChangeLogLockTableName(liquibaseAction.getName() + "_LOCK");
        db.setOutputDefaultCatalog(false);

        Liquibase l = new Liquibase(liquibaseAction.getFile(), new ClassLoaderResourceAccessor(), db);
        // TODO: set parameters l.setChangeLogParameter("key","value");
        return l;
    }
    
    public void setDatabaseDefaultTablespace(Connection connection, String name, String defaultTablespace)
            throws SQLException {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("alter database ").append(name)
                .append(" set tablespace ").append(defaultTablespace).toString());
    }
    
    public void createTablespace(Connection connection, String name, String dataFilePath) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("create tablespace ").append(name)
                .append(" location '").append(dataFilePath).append("' ").toString());
    }
    
    public void setTablespaceOwner(Connection connection, String name, String user) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("alter tablespace ").append(name)
                .append(" owner to ").append(user).toString());
    }
    
    public void setSchemaOwner(Connection connection, String name, String user)
            throws SQLException {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("alter schema").append(name).append(" owner to ").append(user).toString());
    }
    
    @Override
    public void createSchema(Connection connection, String name, String user){
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("create schema if not exists ").append(name).append(" authorization ").append(user).toString());
    }
    
    @Override
    public void dropSchema(Connection connection, String name) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("drop schema if exists ").append(name).append(" cascade").toString());
    }
    
    protected static void throwIfNot(PSQLException e, String errorCode) {
        if(!e.getSQLState().equals(errorCode)) {
            throw new RuntimeException(e);
        }
    }
}
