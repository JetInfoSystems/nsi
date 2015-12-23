package jet.isur.nsi.migrator.platform;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.boot.registry.StandardServiceRegistry;

public interface PlatformMigrator {
    StandardServiceRegistry buildStandardServiceRegistry(DataSource dataSource);
    
    DictToHbmSerializer getDictToHbmSerializer();
    
    DataSource createDataSource(String name, Properties properties);
    
    Connection createAdminConnection(String name, Properties properties) throws SQLException;
    
    void createTablespace(Connection connection, String name,String dataFileName,
            String dataFileSize, String dataFileAutoSize, String dataFileMaxSize);

    void dropTablespace(Connection connection, String name) throws SQLException;

    void createUser(Connection connection, String name,String password,
            String defaultTablespace, String tempTablespace) throws SQLException;
    
    void grantUser(Connection connection, String name) throws SQLException;

    void dropUser(Connection connection, String name) throws SQLException;

    Long createUserProfile(Connection con, String login) throws SQLException;

    void removeUserProfile(Connection con, Long id) throws SQLException;

}
