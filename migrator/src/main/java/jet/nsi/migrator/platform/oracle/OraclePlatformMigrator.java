package jet.nsi.migrator.platform.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
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

import com.jolbox.bonecp.BoneCPDataSource;

import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.common.config.MigratorParams;
import jet.nsi.common.platform.oracle.OracleNsiPlatform;
import jet.nsi.migrator.MigratorException;
import jet.nsi.migrator.hibernate.NsiImplicitNamingStrategyImpl;
import jet.nsi.migrator.liquibase.LiqubaseAction;
import jet.nsi.migrator.platform.DefaultPlatformMigrator;
import jet.nsi.migrator.platform.DictToHbmSerializer;
import jet.nsi.migrator.platform.PlatformException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class OraclePlatformMigrator extends DefaultPlatformMigrator {

    private final int ORA_SEQUENCE_DOES_NOT_EXISTS_ERROR_CODE = 2289;
    private final int ORA_TABLE_OR_VIEW_DOES_NOT_EXISTS_ERROR_CODE = 942;
    
    private final OracleFtsModule ftsModule;

    public OraclePlatformMigrator(MigratorParams params) {
        super(new OracleNsiPlatform(), params);
        ftsModule = new OracleFtsModule(platformSqlDao);
    }

    @Override
    public StandardServiceRegistry buildStandardServiceRegistry(
            DataSource dataSource) {
        final BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
        final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder( bsr );

        Properties props = new Properties();
        // TODO: используем params
        props.put(AvailableSettings.DIALECT, NsiOracleDialect.class.getName());
        props.put(AvailableSettings.DATASOURCE, dataSource);
        ssrBuilder.applySettings( props );

        return ssrBuilder.build();
    }

    @Override
    public DictToHbmSerializer getDictToHbmSerializer() {
        return new OracleDictToHbmSerializer();
    }

    @Override
    public Connection createAdminConnection(String name, Properties properties)
            throws SQLException {
        Properties connectProperties = new Properties();
        connectProperties.put("user", properties.getProperty("db." + name + ".sys.username") + " as sysdba");
        connectProperties.put("password", properties.getProperty("db." + name + ".sys.password"));
        return DriverManager.getConnection (properties.getProperty("db." + name + ".url"), connectProperties );
    }

    @Override
    public void createTablespace(Connection connection, String name,
            String dataFileName, String dataFileSize, String dataFileAutoSize,
            String dataFileMaxSize) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" create tablespace ").append(name)
                .append(" datafile '").append(dataFileName).append("' ")
                .append(" size ").append(dataFileSize).append(" reuse ")
                .append(" autoextend on next ").append(dataFileAutoSize)
                .append(" maxsize ").append(dataFileMaxSize).toString());
    }

    @Override
    public void dropTablespace(Connection connection, String name) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" drop tablespace ").append(name).append(" including contents and datafiles").toString());
    }

    @Override
    public void createUser(Connection connection, String name, String password,
            String defaultTablespace, String tempTablespace) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" create user ").append(name)
                .append(" IDENTIFIED BY \"").append(password).append("\" ")
                .append(" DEFAULT TABLESPACE ").append(defaultTablespace)
                .append(" TEMPORARY TABLESPACE ").append(tempTablespace).toString());
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" ALTER USER ").append(name)
                .append(" QUOTA UNLIMITED ON ").append(defaultTablespace).toString());
        grantUser(connection, name);
    }
    
    @Override
    public void dropUser(Connection connection, String name){
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" DROP USER  ").append(name).append(" CASCADE").toString());
    }
    
    @Override
    public void createSchema(Connection connection, String name, String user){
        throw new PlatformException("createSchema: unsupported for Oracle Platform");
    }
    
    @Override
    public void dropSchema(Connection connection, String name) {
        throw new PlatformException("createSchema: unsupported for Oracle Platform");
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
            if(cause instanceof SQLSyntaxErrorException) {
                throwIfNot((SQLSyntaxErrorException)cause, ORA_TABLE_OR_VIEW_DOES_NOT_EXISTS_ERROR_CODE);
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
        }
        catch(DataAccessException e) {
            Throwable cause = e.getCause();
            if(cause instanceof SQLSyntaxErrorException) {
                throwIfNot((SQLSyntaxErrorException)cause, ORA_SEQUENCE_DOES_NOT_EXISTS_ERROR_CODE);
            } else {
                throw e;
            }
        }
    }
    
   
    
    @Override
    public void createFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        ftsModule.createFullSearchIndex(dict, field, connection);
    }

    @Override
    public void dropFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        ftsModule.dropFullSearchIndex(dict, field, connection);
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
    public void grantUser(Connection connection, String name)
       //     throws SQLException { 
    {
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT RESOURCE TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT CONNECT TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT CREATE ANY VIEW TO ").append(name).toString());
        
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT CTXAPP TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT EXECUTE ON CTXSYS.CTX_CLS TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT EXECUTE ON CTXSYS.CTX_DDL TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT EXECUTE ON CTXSYS.CTX_DOC TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT EXECUTE ON CTXSYS.CTX_OUTPUT TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT EXECUTE ON CTXSYS.CTX_QUERY TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT EXECUTE ON CTXSYS.CTX_REPORT TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT EXECUTE ON CTXSYS.CTX_THES TO ").append(name).toString());
        platformSqlDao.executeSql(connection, new StringBuilder().append(" GRANT EXECUTE ON CTXSYS.CTX_ULEXER TO ").append(name).toString());
    }

    @Override
    public DataSource createDataSource(String name, Properties properties) {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
        dataSource.setJdbcUrl(properties.getProperty("db." + name + ".url"));
        dataSource.setUsername(properties.getProperty("db." + name + ".username"));
        dataSource.setPassword(properties.getProperty("db." + name + ".password"));
        dataSource.setConnectionTimeoutInMs(15000);
        dataSource.setConnectionTestStatement("select 1 from dual");
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
    public void updateMetadataSources(MetadataSources metadataSources,
            NsiConfig config) {
    }

    @Override
    public void updateMetadataSources(MetadataSources metadataSources,
            NsiConfigDict model) {
    }

    @Override
    public void onUpdateBeforePostproc(Connection connection, NsiConfig config) {
    }

    @Override
    public void onUpdateBeforePostproc(Connection connection, NsiConfigDict model) {
    }

    @Override
    public void onUpdateAfterPostproc(Connection connection, NsiConfigDict model) {
        try {
            ftsModule.updateFtsIndexesAfterPostproc(connection, model);
        } catch (Exception e) {
            throw new MigratorException("onUpdateAfterPostproc", e);
        }
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
    public Liquibase createLiquibase(Connection c, LiqubaseAction liquibaseAction) {

        Database db = new OracleDatabase();
        db.setConnection(new JdbcConnection(c));

        db.setDatabaseChangeLogTableName(liquibaseAction.getName() + "_LOG");
        db.setDatabaseChangeLogLockTableName(liquibaseAction.getName() + "_LOCK");
        db.setOutputDefaultCatalog(false);

        try {
            Liquibase l = new Liquibase(liquibaseAction.getFile(), new ClassLoaderResourceAccessor(), db);
            return l;
        } catch (LiquibaseException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("createLiquibase(OraclePlatform) -> failed" , e);
        }
        
    }
}
