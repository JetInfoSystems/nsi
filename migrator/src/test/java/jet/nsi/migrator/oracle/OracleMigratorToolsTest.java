package jet.nsi.migrator.oracle;

import static jet.nsi.common.migrator.config.MigratorParams.key;
import static jet.nsi.common.migrator.config.MigratorParams.DB;
import static jet.nsi.common.migrator.config.MigratorParams.LIQUIBASE;
import static jet.nsi.common.migrator.config.MigratorParams.LOG_PREFIX;
import static jet.nsi.common.migrator.config.MigratorParams.NAME;
import static jet.nsi.common.migrator.config.MigratorParams.TABLESPACE;
import static jet.nsi.common.migrator.config.MigratorParams.USERNAME;


import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.common.config.impl.NsiConfigManagerFactoryImpl;
import jet.nsi.common.migrator.config.MigratorParams;
import jet.nsi.migrator.platform.PlatformMigrator;
import jet.nsi.migrator.platform.oracle.OraclePlatformMigrator;
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.OraclePlatformDaoUtils;
import junit.framework.Assert;
@Ignore // оракл нам не нужен. На поддержку тестов будем тратить лишнее время
public class OracleMigratorToolsTest extends BaseSqlTest{

    private static final String DB_IDENT = "nsi.oracle";
    private static final String TEST_NSI_PREFIX = "TEST_NSI_";
    
    private PlatformMigrator platformMigrator;
    
    public OracleMigratorToolsTest() {
        super(DB_IDENT, new OraclePlatformDaoUtils());
    }

    @Override
    public void setup() throws Exception {
        super.setup();

        Assert.assertEquals(TEST_NSI_PREFIX, params.getLogPrefix());

        params = new MigratorParams(properties);
    }
    
    @Override
    protected void initTestCustomProperties() {
        properties.setProperty(key(DB,LIQUIBASE,LOG_PREFIX), TEST_NSI_PREFIX);
    }

    @Override
    protected void initPlatformSpecific() {
        platformMigrator = new OraclePlatformMigrator(params);
//        platformMigrator.setParams(params);
        platform = platformMigrator.getPlatform();
    }

    public void setupMigrator(String metadataPath) {
        File configPath = new File(metadataPath);
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
    }

    @Test
    public void tablespaceTest() throws SQLException {
        String tempName = "t" + DateTime.now().getMillis();
        properties.put(key(DB,dbIdent,TABLESPACE,NAME), tempName);
        try(Connection connection = platformMigrator.createAdminConnection(dbIdent, properties)) {
            platformMigrator.createTablespace(connection,
                    params.getTablespace(dbIdent),
                    params.getDataFileName(dbIdent), "1M", "1M", "10M");
            platformMigrator.dropTablespace(connection, params.getTablespace(dbIdent));
        }
    }

    @Test
    public void userTest() throws SQLException {
        String tempName = "t" + DateTime.now().getMillis();
        properties.put(key(DB,dbIdent,TABLESPACE,NAME), tempName);
        properties.put(key(DB,dbIdent,USERNAME), tempName);
        try(Connection connection = platformMigrator.createAdminConnection(dbIdent, properties)) {
            platformMigrator.createTablespace(connection,
                    params.getTablespace(dbIdent),
                    params.getDataFileName(dbIdent), "1M", "1M", "10M");
            try {
                platformMigrator.createUser(connection,
                        params.getUsername(dbIdent),
                        params.getPassword(dbIdent),
                        params.getTablespace(dbIdent),
                        params.getTempTablespace(dbIdent));
                platformMigrator.dropUser(connection, params.getUsername(dbIdent));
            } finally {
                platformMigrator.dropTablespace(connection, params.getTablespace(dbIdent));
            }
        }
    }

    @Test
    public void createUserProfileTest() throws SQLException {
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
    }
}
