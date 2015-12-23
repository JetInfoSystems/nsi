package jet.isur.nsi.migrator.platform.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import com.jolbox.bonecp.BoneCPDataSource;

import jet.isur.nsi.api.platform.NsiPlatform;
import jet.isur.nsi.common.platform.oracle.OracleNsiPlatform;
import jet.isur.nsi.migrator.platform.DefaultPlatformMigrator;
import jet.isur.nsi.migrator.platform.DictToHbmSerializer;

public class OraclePlatformMigrator extends DefaultPlatformMigrator {

    private final NsiPlatform platform;
    
    public OraclePlatformMigrator() {
        platform = new OracleNsiPlatform();
        setPlatformSqlDao(platform.getPlatformSqlDao());
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


}
