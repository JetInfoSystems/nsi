package jet.nsi.migrator.oracle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;
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
import jet.nsi.testkit.test.BaseSqlTest;
import jet.nsi.testkit.utils.OraclePlatformDaoUtils;
import junit.framework.Assert;

public class OracleMigratorTest extends BaseSqlTest{

    private static final String DB_IDENT = "nsi.oracle";
    private MigratorParams params;
    private PlatformMigrator platformMigrator;
    private OracleFtsModule ftsModule;
    
    public OracleMigratorTest() {
        super(DB_IDENT, new OraclePlatformDaoUtils());
    }

    @Override
    public void setup() throws Exception {
        platformMigrator = new OraclePlatformMigrator();
        platform = platformMigrator.getPlatform();
        super.setup();
        getConfiguration();
        properties.setProperty("db.liqubase.logPrefix", "TEST_NSI_");
        params = new MigratorParams(properties);
        Assert.assertEquals("TEST_NSI_", params.getLogPrefix());
        ftsModule = new OracleFtsModule(platformSqlDao);
    }

    public void setupMigrator(String metadataPath) {
        File configPath = new File(metadataPath);
        NsiConfigManager manager = new NsiConfigManagerFactoryImpl().create(configPath);
        config = manager.getConfig();
    }

    private void getConfiguration() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("project."+ dbIdent +".properties");
        Properties props = new Properties();
        props.load(in);
    }

    @Test
    public void migratorTest() throws Exception {
        setupMigrator("src/test/resources/metadata/migrator");

        NsiConfigDict dict1 = config.getDict("dict1");
        NsiConfigDict dict2 = config.getDict("dict2");
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict2, connection);
            platformMigrator.dropTable(dict1, connection);
            platformMigrator.dropSeq(dict2, connection);
            platformMigrator.dropSeq(dict1, connection);

            platformMigrator.dropSeq("SEQ_POSTPROC1", connection);

            platformMigrator.dropTable("TEST_NSI_PREPARE_LOG", connection);
            platformMigrator.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.update("v1");

            List<String> actions = rec.getActions();
            Assert.assertEquals(4, actions.size());
            Assert.assertEquals("create table table1 (id number(19,0) not null, f1 varchar2(100 char), is_deleted char(1 char), last_change date, last_user number(19,0), VERSION number(6,0), primary key (id))", actions.get(0));
            Assert.assertEquals("create table table2 (id number(19,0) not null, dict1_id number(19,0), is_deleted char(1 char), last_change date, last_user number(19,0), name char(100 char), VERSION number(6,0), primary key (id))", actions.get(1));
            Assert.assertEquals("alter table table2 add constraint fk_table2_FE52C689 foreign key (dict1_id) references table1", actions.get(2));
            Assert.assertEquals("create sequence seq_table2 start with 1 increment by 1", actions.get(3));
        }

        // check SEQ_POSTPROC1
        try(Connection connection = dataSource.getConnection()) {
            platformSqlDao.executeSql(connection, "select SEQ_POSTPROC1.nextval from dual");
        }

        try(Connection connection = dataSource.getConnection()) {
            platformSqlDao.executeSql(connection, "ALTER TABLE TABLE1 DROP COLUMN F1");
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.update("v2");

            List<String> actions = rec.getActions();
            Assert.assertEquals(1, actions.size());
            Assert.assertEquals("alter table table1 add f1 varchar2(100 char)",actions.get(0));
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.rollback("v2");

            List<String> actions = rec.getActions();
            Assert.assertEquals(0, actions.size());
        }

        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict2, connection);
            platformMigrator.dropTable(dict1, connection);
            platformMigrator.dropSeq(dict2, connection);
            platformMigrator.dropSeq(dict1, connection);

            platformMigrator.dropSeq("SEQ_POSTPROC1", connection);

            platformMigrator.dropTable("TEST_NSI_PREPARE_LOG", connection);
            platformMigrator.dropTable("TEST_NSI_POSTPROC_LOG", connection);
        }

    }

    @Test
    public void tablespaceTest() throws SQLException {
        String tempName = "t" + DateTime.now().getMillis();
        properties.put("db."+ DB_IDENT + ".tablespace.name", tempName);
        try(Connection connection = platformMigrator.createAdminConnection(DB_IDENT, properties)) {
            platformMigrator.createTablespace(connection,
                    params.getTablespace(DB_IDENT),
                    params.getDataFileName(DB_IDENT), "1M", "1M", "10M");
            platformMigrator.dropTablespace(connection, params.getTablespace(DB_IDENT));
        }
    }

    @Test
    public void userTest() throws SQLException {
        String tempName = "t" + DateTime.now().getMillis();
        properties.put("db."+ DB_IDENT + ".tablespace.name", tempName);
        properties.put("db."+ DB_IDENT + ".username", tempName);
        try(Connection connection = platformMigrator.createAdminConnection(DB_IDENT, properties)) {
            platformMigrator.createTablespace(connection,
                    params.getTablespace(DB_IDENT),
                    params.getDataFileName(DB_IDENT), "1M", "1M", "10M");
            try {
                platformMigrator.createUser(connection,
                        params.getUsername(DB_IDENT),
                        params.getPassword(DB_IDENT),
                        params.getTablespace(DB_IDENT),
                        params.getTempTablespace(DB_IDENT));
                platformMigrator.dropUser(connection, params.getUsername(DB_IDENT));
            } finally {
                platformMigrator.dropTablespace(connection, params.getTablespace(DB_IDENT));
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

    @Test
    @Ignore("Must be fixed by AFS-31")
    public void changeColumnSizeTest() throws Exception {
        setupMigrator("src/test/resources/metadata/changeColumnSize/create");
        NsiConfigDict testSize = config.getDict("test_size");
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(testSize, connection);
            platformMigrator.dropSeq(testSize, connection);
        }

        RecActionsTargetImpl rec = new RecActionsTargetImpl();

        Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v1");

        setupMigrator("src/test/resources/metadata/changeColumnSize/alter");
        migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v1");

        List<String> actions = rec.getActions();
        log.info(actions.toString());
        Assert.assertEquals(3, actions.size());
        Assert.assertEquals("alter table test_size modify test char(4 char)", actions.get(2));

        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(testSize, connection);
            platformMigrator.dropSeq(testSize, connection);
        }
    }

    @Test
    public void versionColumnSizeTest() throws Exception {
        setupMigrator("src/test/resources/metadata/number");
        NsiConfigDict testSize = config.getDict("dict1");
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(testSize, connection);
        }
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropSeq(testSize, connection);
        }

        RecActionsTargetImpl rec = new RecActionsTargetImpl();

        Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v1");

        List<String> actions = rec.getActions();
        log.info(actions.toString());
        Assert.assertEquals(2, actions.size());
        Assert.assertEquals("create table dict1 (id number(19,0) not null, v number(4,0), VERSION number(6,0), primary key (id))", actions.get(0));

        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(testSize, connection);
        }
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropSeq(testSize, connection);
        }
    }
    
    @Test
    public void checkTypesTest() throws Exception {
        setupMigrator("src/test/resources/metadata/check_types");
        
        NsiConfigDict dict1 = config.getDict("dict1");
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict1, connection);
        }
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropSeq(dict1, connection);
        }

        RecActionsTargetImpl rec = new RecActionsTargetImpl();

        setupMigrator("src/test/resources/metadata/check_types");
        Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v1");

        List<String> actions = rec.getActions();
        log.info("DUMP");

        Assert.assertEquals(actions.toString(), 2, actions.size());
        Assert.assertEquals("create table dict1 (id number(19,0) not null, clobField clob, f1 number(20,8), VERSION number(6,0), primary key (id))", actions.get(0));

        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict1, connection);
        }
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropSeq(dict1, connection);
        }

    }

    @Test
    public void checkFtsTest() throws Exception {
        
        setupMigrator("src/test/resources/metadata/fts");
        NsiConfigDict dict1 = config.getDict("dict1");
        
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict1, connection);
        }
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropSeq(dict1, connection);
        }

        RecActionsTargetImpl rec = new RecActionsTargetImpl();

        Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.addTarget( rec );
        migrator.update("v1");

        try(Connection connection = dataSource.getConnection()) {
            Map<String, String> indexMap = ftsModule.getFtsDatabaseIndexes(connection, dict1);
            Assert.assertEquals(1, indexMap.size());
            Assert.assertTrue(indexMap.containsKey("DESCRIPTION"));
        }
        
        // меняем метаданные 
        dict1.getField("name").setEnableFts(true);
        migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.update("v2");

        try(Connection connection = dataSource.getConnection()) {
            Map<String, String> indexMap = ftsModule.getFtsDatabaseIndexes(connection, dict1);
            Assert.assertEquals(2, indexMap.size());
            Assert.assertTrue(indexMap.containsKey("DESCRIPTION"));
            Assert.assertTrue(indexMap.containsKey("NAME"));
        }

        // меняем метаданные 
        dict1.getField("name").setEnableFts(false);
        migrator = new Migrator(config, dataSource, params, platformMigrator );
        migrator.update("v3");

        try(Connection connection = dataSource.getConnection()) {
            Map<String, String> indexMap = ftsModule.getFtsDatabaseIndexes(connection, dict1);
            Assert.assertEquals(1, indexMap.size());
            Assert.assertTrue(indexMap.containsKey("DESCRIPTION"));
        }

        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropTable(dict1, connection);
        }
        try(Connection connection = dataSource.getConnection()) {
            platformMigrator.dropSeq(dict1, connection);
        }

    }

}
