package jet.nsi.migrator.platform;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;

import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.common.migrator.config.MigratorParams;
import jet.nsi.migrator.liquibase.LiqubaseAction;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.hibernate.mapping.Table;

public interface PlatformMigrator {
    NsiPlatform getPlatform();
    
    StandardServiceRegistry buildStandardServiceRegistry(DataSource dataSource);
    
    DataSource getDataSource();

    Connection createAdminConnection(String name, Properties properties) throws SQLException;
    
    void createTablespace(Connection connection, String name,String dataFileName,
            String dataFileSize, String dataFileAutoSize, String dataFileMaxSize);

    void dropTablespace(Connection connection, String name);

    void createUser(Connection connection, String name,String password,
            String defaultTablespace, String tempTablespace);
    
    void dropUser(Connection connection, String name);
    
    void createSchema(Connection connection, String name, String user);
    
    void dropSchema(Connection connection, String name);
    
    void grantUser(Connection connection, String name);

    void createTable(NsiConfigDict dict, Connection connection);
    
    void recreateTable(NsiConfigDict dict, Connection connection);
    
    void dropTable(NsiConfigDict dict, Connection connection);
    
    void dropTable(String name, Connection connection);

    void createSeq(NsiConfigDict dict, Connection connection);

    void recreateSeq(NsiConfigDict dict, Connection connection);

    void dropSeq(NsiConfigDict dict, Connection connection);
    
    void dropSeq(String name, Connection connection);

    void createIndex(NsiConfigDict dict, String field, Connection connection);
    
    void dropIndex(NsiConfigDict dict, String field, Connection connection);
    
    void dropIndex(String name, Connection connection);
    
    void createFullSearchIndex(NsiConfigDict dict, String field, Connection connection);
    
    void dropFullSearchIndex(NsiConfigDict dict, String field, Connection connection);

    void recreateFullSearchIndex(NsiConfigDict dict, String field, Connection connection);

    Long createUserProfile(Connection con, String login);

    void removeUserProfile(Connection con, Long id);

    void onUpdateBeforePrepare(Connection connection, NsiConfig config);
    
    void onUpdateBeforePrepare(Connection connection, NsiConfigDict model);
    
    void onUpdateAfterPrepare(Connection connection, NsiConfigDict model);
    
    void onUpdateAfterPrepare(Connection connection, NsiConfig config);

    DictToHbmSerializer getDictToHbmSerializer();
    
    void updateMetadataSources(MetadataSources metadataSources, NsiConfig config);
    
    void updateMetadataSources(MetadataSources metadataSources, NsiConfigDict model);
    
    void onUpdateBeforePostproc(Connection connection, NsiConfig config);
    
    void onUpdateBeforePostproc(Connection connection, NsiConfigDict model);
    
    void onUpdateAfterPostproc(Connection connection, NsiConfigDict model);
    
    void onUpdateAfterPostproc(Connection connection, NsiConfig config);
    
    Liquibase createLiquibase(Connection c, LiqubaseAction liquibaseAction) throws LiquibaseException;

    void doLiquibaseUpdate(String name, String file, String tag, String action, String logPrefix, DataSource dataSource);

    void setPrimaryKey(Table table);

    MigratorParams getParams();

    boolean isColumnEditable();

    boolean isSupportForeignKey();
    boolean isSupportRollback();

    boolean isNeedToInitializeSequence();
}
