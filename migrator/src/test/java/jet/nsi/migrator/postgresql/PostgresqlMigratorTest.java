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

public class PostgresqlMigratorTest extends BaseSqlTest{

    private static final String DB_IDENT = "nsi.postgresql95";
    private MigratorParams params;
    private PlatformMigrator platformMigrator;
    //private OracleFtsModule ftsModule;

    public PostgresqlMigratorTest() {
        super(DB_IDENT);
    }
    
    @Override
    public void setup() throws Exception {
        platformMigrator = new PostgresqlPlatformMigrator();
        platform = platformMigrator.getPlatform();
        super.setup();
        getConfiguration();
        properties.setProperty("db.liqubase.logPrefix", "TEST_NSI_");
        params = new MigratorParams(properties);
        Assert.assertEquals("TEST_NSI_", params.getLogPrefix());
        //ftsModule = new OracleFtsModule(platformSqlDao);
    }

    public void setupMigrator(String metadataPath) throws Exception {
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
        } catch (DataAccessException e) {
            String message = e.getMessage();
            if (message.startsWith("SQL [drop table table") && message.endsWith("does not exist")) {
                //do nothing
            } else {
                throw e;
            }
        }

        {
            Migrator migrator = new Migrator(config, dataSource, params, platformMigrator );
            RecActionsTargetImpl rec = new RecActionsTargetImpl();
            migrator.addTarget( rec );
            migrator.update("v1");

            List<String> actions = rec.getActions();
            Assert.assertEquals(4, actions.size());
            Assert.assertEquals("create table table1 (id int8 not null, f1 varchar(100), is_deleted char(1), last_change date, last_user int8, VERSION int8, primary key (id))", actions.get(0));
            Assert.assertEquals("create table table2 (id int8 not null, dict1_id int8, is_deleted char(1), last_change date, last_user int8, name char(100), VERSION int8, primary key (id))", actions.get(1));
            Assert.assertEquals("alter table table2 add constraint fk_table2_FE52C689 foreign key (dict1_id) references table1", actions.get(2));
            Assert.assertEquals("create sequence seq_table2 start 1 increment 1", actions.get(3));
        }

        // check SEQ_POSTPROC1
        try(Connection connection = dataSource.getConnection()) {
            platformSqlDao.executeSql(connection, "select nextval('SEQ_POSTPROC1')");
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
            Assert.assertEquals("alter table table1 add column f1 varchar(100)",actions.get(0));
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
    
}
