package jet.nsi.migrator.postgresql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;
import org.jooq.exception.DataAccessException;
import org.junit.Ignore;
import org.junit.Test;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.nsi.migrator.Migrator;
import jet.nsi.migrator.MigratorParams;
import jet.nsi.migrator.hibernate.RecActionsTargetImpl;
import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.migrator.platform.oracle.OracleFtsModule;
import jet.nsi.migrator.platform.oracle.OraclePlatformMigrator;
import jet.nsi.migrator.platform.postgresql.PostgresqlPlatformMigrator;
import jet.nsi.testkit.test.BaseSqlTest;
import junit.framework.Assert;

public class PostgresqlMigratorToolsTest extends BaseSqlTest{

    private static final String DB_IDENT = "nsi.postgresql95";
    private static final String TEST_NSI_PREFIX = "TEST_NSI_";
    private MigratorParams params;
    private PlatformMigrator platformMigrator;
    //private PostgresqlFtsModule ftsModule;

    public PostgresqlMigratorToolsTest() {
        super(DB_IDENT);
    }
    
    @Override
    public void setup() throws Exception {
        platformMigrator = new PostgresqlPlatformMigrator();
        platform = platformMigrator.getPlatform();
        super.setup();
        getConfiguration();
        properties.setProperty("db.liqubase.logPrefix", TEST_NSI_PREFIX);
        properties.setProperty("liquibaseChangelogBasePath", "with_empty_liquibase_changelogs");
        params = new MigratorParams(properties);
        Assert.assertEquals(TEST_NSI_PREFIX, params.getLogPrefix());
        //ftsModule = new OracleFtsModule(platformSqlDao);
    }

    public void setupMigrator(String metadataPath) {
        File configPath = new File(metadataPath);
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
    }


    private void getConfiguration() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("project."+ dbIdent+ ".properties");
        Properties props = new Properties();
        props.load(in);
    }

    @Test
    public void tablespaceTest() throws SQLException {
        String tempName = "t" + DateTime.now().getMillis();
        properties.put("db."+ DB_IDENT + ".tablespace.name", tempName);
        try(Connection connection = platformMigrator.createAdminConnection(DB_IDENT, properties)) {
            platformMigrator.createTablespace(connection,
                    params.getTablespace(DB_IDENT),
                    params.getDataFilePath(DB_IDENT), "1M", "1M", "10M");
            platformMigrator.dropTablespace(connection, params.getTablespace(DB_IDENT));
        }
    }
    
    
    @Test
    public void userTest() throws SQLException {
        String tempName = "t" + DateTime.now().getMillis();
        properties.put("db."+ DB_IDENT +".tablespace.name", tempName);
        properties.put("db."+ DB_IDENT +".username", tempName);
        try(Connection connection = platformMigrator.createAdminConnection(DB_IDENT, properties)) {
            platformMigrator.createTablespace(connection,
                    params.getTablespace(DB_IDENT),
                    params.getDataFilePath(DB_IDENT), "1M", "1M", "10M");
            try {
                
                platformMigrator.createUser(connection,
                        params.getUsername(DB_IDENT),
                        params.getPassword(DB_IDENT),
                        params.getTablespace(DB_IDENT),
                        params.getTempTablespace(DB_IDENT));
                
            } finally {
                try {
                    platformMigrator.dropTablespace(connection, params.getTablespace(DB_IDENT));
                    
                    
                } finally {
                    platformMigrator.dropUser(connection, params.getUsername(DB_IDENT));
                }
            }
        }
    }
    
    @Test
    public void createUserProfileTest() throws Exception {
        setupMigrator("src/test/resources/metadata/user_profile");
        
        NsiConfigDict userProfileDict = config.getDict("USER_PROFILE");
        
        Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
        RecActionsTargetImpl rec = new RecActionsTargetImpl();
        migrator.addTarget( rec );
        migrator.update("v1");
        
        String login = String.valueOf(System.nanoTime());
        try (Connection con = dataSource.getConnection()) {
            Long id = null;
            try {
                id = platformMigrator.createUserProfile(con, login);
                Assert.assertNotNull(id);
                Assert.assertNull(platformMigrator.createUserProfile(con, login));
            } finally {
                platformMigrator.removeUserProfile(con, id);
            }
        }
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(userProfileDict, connection);
            platformMigrator.dropSeq(userProfileDict, connection);
        }
    }
    
    
}
