package jet.isur.nsi.migrator.platform.oracle;

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

import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.data.NsiConfigField;
import jet.isur.nsi.common.platform.oracle.OracleNsiPlatform;
import jet.isur.nsi.migrator.platform.DefaultPlatformMigrator;
import jet.isur.nsi.migrator.platform.DictToHbmSerializer;

public class OraclePlatformMigrator extends DefaultPlatformMigrator {

    public OraclePlatformMigrator() {
        super(new OracleNsiPlatform());
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
    public void dropTablespace(Connection connection, String name)
            throws SQLException {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" drop tablespace ").append(name).append(" including contents and datafiles").toString());
    }

    @Override
    public void createUser(Connection connection, String name, String password,
            String defaultTablespace, String tempTablespace)
                    throws SQLException {
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
                throwIfNot((SQLSyntaxErrorException)cause, 942);
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
                throwIfNot((SQLSyntaxErrorException)cause, 2289);
            } else {
                throw e;
            }
        }
    }
    
    @Override
    public void createFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        String table = dict.getTable();
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("CREATE INDEX ")
                .append("fti_").append(table).append("_").append(field)
                .append(" ON ")
                .append(table)
                .append("(").append(field).append(")")
                .append(" INDEXTYPE IS CTXSYS.CONTEXT ")
                .append("PARAMETERS ('filter ctxsys.null_filter lexer isur sync(on commit)')")
                .toString());
    }

    @Override
    public void dropFullSearchIndex(NsiConfigDict dict, String field,
            Connection connection) {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append("DROP INDEX ")
                .append("fti_").append(dict.getTable()).append("_").append(field).toString());
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
            throws SQLException {
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
    public void dropUser(Connection connection, String name)
            throws SQLException {
        platformSqlDao.executeSql(connection, new StringBuilder()
                .append(" DROP USER  ").append(name).append(" CASCADE").toString());
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
    public void onUpdateBeforePrepare(NsiConfig config) {
    }

    @Override
    public void onUpdateBeforePrepare(NsiConfigDict model) {
    }

    @Override
    public void onUpdateAfterPrepare(NsiConfigDict model) {
    }

    @Override
    public void onUpdateAfterPrepare(NsiConfig config) {
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
    public void onUpdateBeforePostproc(NsiConfig config) {
    }

    @Override
    public void onUpdateBeforePostproc(NsiConfigDict model) {
    }

    @Override
    public void onUpdateAfterPostproc(NsiConfigDict model) {
        updateFtsIndexesAfterPostproc(model);
    }

    private void updateFtsIndexesAfterPostproc(NsiConfigDict model) {
        // получаем сведения о полнотекстовых индексах 
        // формируем список требуемых полнотекстовых индексов 
        // проходим по списку имеющихсяв базеданных полнотекстовых индексов на таблице 
        // удаляем отсутствующие в метаданных 
        // проходим по списку полей отмеченных для полнотекстового поиска
        // создаем индексы отсутствующие в бд 
    }

    @Override
    public void onUpdateAfterPostproc(NsiConfig config) {
    }

}
