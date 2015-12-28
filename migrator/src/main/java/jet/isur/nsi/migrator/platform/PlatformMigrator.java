package jet.isur.nsi.migrator.platform;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.platform.NsiPlatform;

public interface PlatformMigrator {
    NsiPlatform getPlatform();
    
    StandardServiceRegistry buildStandardServiceRegistry(DataSource dataSource);
    
    DataSource createDataSource(String name, Properties properties);
    
    Connection createAdminConnection(String name, Properties properties) throws SQLException;
    
    void createTablespace(Connection connection, String name,String dataFileName,
            String dataFileSize, String dataFileAutoSize, String dataFileMaxSize);

    void dropTablespace(Connection connection, String name) throws SQLException;

    void createUser(Connection connection, String name,String password,
            String defaultTablespace, String tempTablespace) throws SQLException;
    
    void grantUser(Connection connection, String name) throws SQLException;

    void dropUser(Connection connection, String name) throws SQLException;

    void createTable(NsiConfigDict dict, Connection connection);
    
    void recreateTable(NsiConfigDict dict, Connection connection);
    
    void dropTable(NsiConfigDict dict, Connection connection);
    
    void dropTable(String name, Connection connection);

    void createSeq(NsiConfigDict dict, Connection connection);

    void recreateSeq(NsiConfigDict dict, Connection connection);

    void dropSeq(NsiConfigDict dict, Connection connection);
    
    void dropSeq(String name, Connection connection);

    void createFullSearchIndex(NsiConfigDict dict, String field, Connection connection);
    
    void dropFullSearchIndex(NsiConfigDict dict, String field, Connection connection);

    void recreateFullSearchIndex(NsiConfigDict dict, String field, Connection connection);

    Long createUserProfile(Connection con, String login) throws SQLException;

    void removeUserProfile(Connection con, Long id) throws SQLException;

    void onUpdateBeforePrepare(NsiConfig config);
    
    void onUpdateBeforePrepare(NsiConfigDict model);
    
    void onUpdateAfterPrepare(NsiConfigDict model);
    
    void onUpdateAfterPrepare(NsiConfig config);

    DictToHbmSerializer getDictToHbmSerializer();
    
    void updateMetadataSources(MetadataSources metadataSources, NsiConfig config);
    
    void updateMetadataSources(MetadataSources metadataSources, NsiConfigDict model);
    
    void onUpdateBeforePostproc(NsiConfig config);
    
    void onUpdateBeforePostproc(NsiConfigDict model);
    
    void onUpdateAfterPostproc(NsiConfigDict model);
    
    void onUpdateAfterPostproc(NsiConfig config);

}
